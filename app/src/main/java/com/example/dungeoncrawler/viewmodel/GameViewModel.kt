package com.example.dungeoncrawler.viewmodel

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dungeoncrawler.KilledBy
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.CharaScreenState
import com.example.dungeoncrawler.data.CharaStats
import com.example.dungeoncrawler.data.EnemyState
import com.example.dungeoncrawler.data.GameState
import com.example.dungeoncrawler.data.LevelObjectState
import com.example.dungeoncrawler.entity.Bomb
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.GroundType
import com.example.dungeoncrawler.entity.Level
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.MainChara
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.enemy.EnemyDamageDTO
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.entity.enemy.Ogre
import com.example.dungeoncrawler.entity.enemy.Plant
import com.example.dungeoncrawler.entity.enemy.Slime
import com.example.dungeoncrawler.entity.enemy.Wolf
import com.example.dungeoncrawler.entity.weapon.Arrow
import com.example.dungeoncrawler.entity.weapon.Bow
import com.example.dungeoncrawler.entity.weapon.Weapon
import com.example.dungeoncrawler.service.DataStoreManager
import com.example.dungeoncrawler.service.MediaPlayerService
import com.example.dungeoncrawler.service.ResultOfInteraction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

class ComposableGameViewModel(application: Application) : AndroidViewModel(application) {

    // TODO: UninitializedPropertyAccessException
    private lateinit var level: Level

    private var chara = MainChara()

    private val _charaScreenStateFlow =
        MutableStateFlow(
            CharaScreenState(
                direction = Direction.DOWN,
                nudge = false,
                jump = false,
                position = chara.position,
                flashRed = false,
                health = Settings.healthBaseValue,
                gold = 0,
                weaponId = "",
                cuirassId = "",
                fixated = false,
                bombAmount = 0
            )
        )
    val charaStateFlow = _charaScreenStateFlow.asStateFlow()

    private val _enemiesStateList = mutableStateListOf<EnemyState>()
    val enemiesStateList: List<EnemyState>
        get() = _enemiesStateList

    private val _objectsStateList = mutableStateListOf<LevelObjectState>()
    val objectsStateList: List<LevelObjectState>
        get() = _objectsStateList

    private val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState.InitGame(0))
    val gameState = _gameState.asStateFlow()

    private val _fieldLayoutState: MutableStateFlow<List<List<GroundType>>> =
        MutableStateFlow(emptyList())
    val fieldLayoutState = _fieldLayoutState.asStateFlow()

    val gamePaused = MutableStateFlow(false)

    private var dataStoreManager: DataStoreManager? = null
    lateinit var mediaPlayerService: MediaPlayerService

    private var enemyPositionChangeJob: Job? = null // TODO: better solution?
    private val enemyAttackJobs: MutableList<Job> = mutableListOf()

    var killedBy = KilledBy(EnemyEnum.SLIME)

    // ----------- INIT METHODS -------------

    fun initDataStoreManager(newManager: DataStoreManager) {
        dataStoreManager = newManager
    }

    fun initMediaPlayerService(newPlayerService: MediaPlayerService) {
        mediaPlayerService = newPlayerService
    }

    fun getHighscore(): Flow<Int> = dataStoreManager?.getHighscoreData() ?: flowOf(0)

    // ----------- ON BUTTON CLICKS -------------

    fun move(direction: Direction) {
        if (turn(direction)) {
            _charaScreenStateFlow.update {
                it.copy(direction = direction)
            }
            return
        }
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val movementVector = getMovementVector(direction)

        moveIfPossible(
            Coordinates(
                coordinates.x + movementVector.x,
                coordinates.y + movementVector.y
            ), coordinates
        )
    }

    fun interact() {
        nudgeChara()

        val coordinates = when (chara.direction) {
            Direction.UP -> Coordinates(chara.position.x, chara.position.y - 1)
            Direction.DOWN -> Coordinates(chara.position.x, chara.position.y + 1)
            Direction.LEFT -> Coordinates(chara.position.x - 1, chara.position.y)
            Direction.RIGHT -> Coordinates(chara.position.x + 1, chara.position.y)
        }
        // TODO: needed?
        if (coordinates.x < 0 || coordinates.y < 0) {
            return
        }

        val levelObjectList = level.fieldHelperService.field[coordinates.x][coordinates.y]
        if (levelObjectList.isEmpty() && level.movableEntitiesList.none { it.position == coordinates }) {
            if (chara.weapon is Bow) {
                if (!isArrowOnField()) {
                    throwArrow(coordinates, chara.direction)
                    addNewGameObjectsToObjectsList()
                }
            }
            return
        }
        val enemy =
            level.movableEntitiesList.find { it.type == LevelObjectType.ENEMY && it.position == coordinates }
        if (enemy != null) {
            attack(enemy as BasicEnemy)
            return
        }
        onInteractWithObject(coordinates)
    }

    private fun onInteractWithObject(coordinates: Coordinates) {
        viewModelScope.launch {
            level.fieldHelperService.interactWithAndRemoveLevelObject(coordinates, level.levelCount)
                .takeWhile { it != ResultOfInteraction.InteractionFinished }
                .collect {
                    when (val resultOfInteraction = it) {
                        ResultOfInteraction.NextLevel -> nextLevel()
                        ResultOfInteraction.InteractionFinished -> {}
                        is ResultOfInteraction.AddLevelObject -> addLevelObject(resultOfInteraction.levelObject)
                        is ResultOfInteraction.RemoveLevelObject -> _objectsStateList.removeIf { gameObject -> gameObject.id == resultOfInteraction.id }
                        is ResultOfInteraction.TakeWeapon -> takeWeapon(
                            resultOfInteraction.weapon,
                            coordinates
                        )

                        is ResultOfInteraction.TakeArmor -> takeArmor(
                            resultOfInteraction.armor,
                            coordinates
                        )

                        is ResultOfInteraction.Heal -> heal(resultOfInteraction.heal)
                        is ResultOfInteraction.Reward -> getReward(resultOfInteraction.amount)
                        ResultOfInteraction.TakeBomb -> takeBomb()
                    }
                }
        }
    }

    fun placeBomb() {
        Log.e("test", "placeBomb")
        val coordinates = when (chara.direction) {
            Direction.UP -> Coordinates(chara.position.x, chara.position.y - 1)
            Direction.DOWN -> Coordinates(chara.position.x, chara.position.y + 1)
            Direction.LEFT -> Coordinates(chara.position.x - 1, chara.position.y)
            Direction.RIGHT -> Coordinates(chara.position.x + 1, chara.position.y)
        }
        // TODO: needed?
        if (coordinates.x < 0 || coordinates.y < 0) {
            return
        }
        val levelObjectList = level.fieldHelperService.field[coordinates.x][coordinates.y]

        if (chara.bombAmount > 0 && levelObjectList.none { !it.type.isSteppableObject() }) {
            val bomb = Bomb("bomb${level.fieldHelperService.itemCounter}", true)
            val resultOfInteraction =
                level.fieldHelperService.placeLevelObject(bomb, coordinates)
            val bombState = (resultOfInteraction as ResultOfInteraction.AddLevelObject).levelObject
            addLevelObject(bombState)
            chara.bombAmount--
            _charaScreenStateFlow.update {
                it.copy(bombAmount = chara.bombAmount)
            }

            val runnableCode = Runnable {
                viewModelScope.launch {
                    level.fieldHelperService.bombExplode(coordinates).collect { coordinates ->
                        val bombExplosionLevelObjectList =
                            level.fieldHelperService.field[coordinates.x][coordinates.y]
                        val iterator = bombExplosionLevelObjectList.iterator()
                        while (iterator.hasNext()) {
                            val levelObject = iterator.next()
                            if (levelObject.type == LevelObjectType.MAIN_CHARA) {
                                attackChara(Settings.bombDamage)
                            } else if (levelObject.type == LevelObjectType.WALL) {
                                iterator.remove()
                            }
                        }
                        _objectsStateList.removeIf {
                            it.type == LevelObjectType.WALL && it.position == coordinates
                        }
                        level.movableEntitiesList.filter { it.position == coordinates && it.type == LevelObjectType.ENEMY }
                            .forEach {
                                enemyTakeDamage(it as BasicEnemy, Settings.bombDamage)
                            }
                    }
                    val resultOfRemove =
                        level.fieldHelperService.removeLevelObject(bomb, coordinates)
                    _objectsStateList.removeIf { gameObject -> gameObject.id == (resultOfRemove as ResultOfInteraction.RemoveLevelObject).id }
                }

            }
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(runnableCode, Settings.bombTimer)
        }
    }

    // ----------- BUTTON CLICK HELPERS -------------

    private fun getMovementVector(direction: Direction) =
        when (direction) {
            Direction.UP -> Coordinates(0, -1)
            Direction.DOWN -> Coordinates(0, 1)
            Direction.LEFT -> Coordinates(-1, 0)
            Direction.RIGHT -> Coordinates(1, 0)
        }

    private fun moveIfPossible(
        newCoordinates: Coordinates,
        coordinates: Coordinates
    ) {
        if (!movePossible(newCoordinates)) {
            return
        }

        jumpAnimation()

        level.fieldHelperService.field[coordinates.x][coordinates.y].removeIf { it.id == chara.id }
        while (level.fieldHelperService.isItemAtPosition(newCoordinates)) {
            onInteractWithObject(newCoordinates)
        }
        level.fieldHelperService.field[newCoordinates.x][newCoordinates.y].add(chara)
        chara.position = newCoordinates
        _charaScreenStateFlow.update {
            it.copy(position = chara.position)
        }
    }

    private fun turn(direction: Direction): Boolean {
        if (chara.direction == direction) {
            return false
        }
        chara.direction = direction
        _charaScreenStateFlow.update {
            it.copy(direction = direction)
        }
        return true
    }

    private fun movePossible(coordinates: Coordinates): Boolean {
        if (level.charaFixated) {
            return false
        }

        if (level.movableEntitiesList.any { it.position == coordinates }) {
            return false
        }
        if (coordinates.x >= level.fieldHelperService.field.size || coordinates.x < 0) {
            return false
        }
        if (coordinates.y >= level.fieldHelperService.field[coordinates.x].size || coordinates.y < 0) {
            return false
        }
        val levelObjectList = level.fieldHelperService.field[coordinates.x][coordinates.y]
        if (levelObjectList.isNotEmpty() && levelObjectList.any { !it.type.isSteppableObject() }) {
            return false
        }
        if (level.movableEntitiesList.any { it.position == coordinates }) {
            return false
        }
        return true
    }

    private fun addLevelObject(levelObjectState: LevelObjectState) {
        _objectsStateList.add(levelObjectState)
        level.gameObjectIds.add(levelObjectState.id)
    }

    private fun getReward(amount: Int) {
        chara.gold += amount
        _charaScreenStateFlow.update {
            it.copy(gold = chara.gold)
        }
    }

    private fun heal(hpCure: Int) {
        chara.health = min(hpCure + chara.health, chara.maxHealth)
        _charaScreenStateFlow.update {
            it.copy(health = chara.health)
        }
    }

    private fun throwArrow(coordinates: Coordinates, direction: Direction) {
        level.throwArrow(coordinates, direction, ::attack)
        viewModelScope.launch {
            level.arrowFlow?.collect { arrow ->
                if (arrow.newPosition == Coordinates(-1, -1)) {
                    _objectsStateList.removeIf { it.id == arrow.id }
                }
                _objectsStateList.replaceAll {
                    if (it.id == arrow.id) {
                        it.copy(position = arrow.newPosition)
                    } else {
                        it
                    }
                }
            }
        }
    }

    private fun isArrowOnField(): Boolean {
        return listOf(
            "${Level.ARROW}_up",
            "${Level.ARROW}_up",
            "${Level.ARROW}_left",
            "${Level.ARROW}_right"
        ).any {
            findCoordinate(it) != Coordinates(-1, -1)
        }
    }

    private fun findCoordinate(id: String): Coordinates {
        val movableEntity = level.movableEntitiesList.firstOrNull { it.id == id }
        if (movableEntity != null) {
            return movableEntity.position
        }

        // TODO: check if part below is still needed
        // TODO: ConcurrentModificationException
        val field = level.fieldHelperService.field.toList()
        for (row in field.indices) {
            val index =
                field[row].indexOfFirst { it.indexOfFirst { levelObject -> levelObject.id == id } != -1 }
            if (index != -1) {
                return Coordinates(row, index)
            }
        }
        return Coordinates(-1, -1)
    }

    private fun enemyTakeDamage(attackedEnemy: BasicEnemy, damage: Int) {
        attackedEnemy.takeDamage(damage)
        _enemiesStateList.replaceAll {
            if (it.id == attackedEnemy.id) {
                val newHealthPercentage = attackedEnemy.health.toDouble() / attackedEnemy.maxHealth
                it.copy(healthPercentage = newHealthPercentage)
            } else {
                it
            }
        }
        flashEnemiesRed(attackedEnemy.id)
        if (attackedEnemy.health <= 0) {
            onEnemyDefeated(attackedEnemy)
        }
    }

    private fun attack(attackedEnemy: BasicEnemy) {
        val weaponBonus = chara.weapon?.attack ?: 0
        enemyTakeDamage(attackedEnemy, chara.baseAttack + weaponBonus)
    }

    // ----------- TAKE STUFF -------------

    private fun takeWeapon(weapon: Weapon, coordinates: Coordinates) {
        val oldWeapon = chara.weapon
        chara.putOnWeapon(weapon)
        _charaScreenStateFlow.update {
            it.copy(weaponId = weapon.id)
        }
        if (oldWeapon != null) {
            level.fieldHelperService.placeLevelObject(oldWeapon, coordinates)
        }
    }

    private fun takeArmor(armor: Armor, coordinates: Coordinates) {
        val oldArmor = chara.armor
        chara.putOnArmor(armor)
        _charaScreenStateFlow.update {
            it.copy(cuirassId = armor.id)
        }
        if (oldArmor != null) {
            level.fieldHelperService.placeLevelObject(oldArmor, coordinates)
        }
    }

    private fun takeBomb() {
        val bombAmount = Settings.bombCount
        _charaScreenStateFlow.update {
            chara.takeBomb(bombAmount)
            it.copy(bombAmount = chara.bombAmount)
        }
    }

    // ----------- ENEMY FUNCTIONS -------------

    private fun onEnemyDefeated(attackedEnemy: BasicEnemy) {
        if (attackedEnemy is Ogre) {
            level.endBossDefeated()
            addNewGameObjectsToObjectsList()
        } else {
            val coin = level.fieldHelperService.placeCoinManually(attackedEnemy.position)
            addLevelObject(
                LevelObjectState(
                    coin.id,
                    coin.type,
                    attackedEnemy.position,
                    Direction.DOWN
                )
            )
        }
        level.movableEntitiesList.removeIf { it.id == attackedEnemy.id }
        _enemiesStateList.removeIf { it.id == attackedEnemy.id }
        attackedEnemy.destroy()

    }

    private fun onEnemyAttack(
        damageDTO: EnemyDamageDTO
    ) {
        if (gameState.value == GameState.EndGameOnGameOver || gameState.value == GameState.EndGameOnVictory) {
            return
        }
        nudgeEnemy(damageDTO)

        takeDamage(damageDTO.damage, damageDTO.enemyType)
    }

    private fun takeDamage(damage: Int, enemyType: EnemyEnum) {
        val protection = chara.armor?.protection ?: 0
        chara.health -= max(0, (damage - (chara.baseDefense + protection)))
        _charaScreenStateFlow.update {
            it.copy(health = chara.health)
        }

        if (chara.health <= 0) {
            killedBy.enemyType = enemyType
            saveGold()
            saveHighscore()

            viewModelScope.launch {
                _gameState.emit(GameState.EndGameOnGameOver)
            }
        }

        flashCharaRed()
    }

    private fun attackChara(attackValue: Int) {
        takeDamage(attackValue, EnemyEnum.OGRE)
    }

    private fun throwPebble(coordinates: Coordinates, direction: Direction) {
        level.throwPebble(coordinates, direction, ::attackChara)
        viewModelScope.launch {
            level.pebbleFlow?.collect { pebble ->
                if (pebble.newPosition == Coordinates(-1, -1)) {
                    _objectsStateList.removeIf { it.id == pebble.id }
                    return@collect
                }
                if (_objectsStateList.none { it.id == pebble.id }) {
                    _objectsStateList.add(
                        LevelObjectState(
                            pebble.id,
                            LevelObjectType.ARROW,
                            pebble.newPosition,
                            pebble.newDirection
                        )
                    )
                    return@collect
                }
                _objectsStateList.replaceAll {
                    if (it.id == pebble.id) {
                        it.copy(position = pebble.newPosition)
                    } else {
                        it
                    }
                }
            }
        }
    }

    // ----------- SAVE STATE -------------

    private fun saveGold() {
        viewModelScope.launch {
            dataStoreManager?.saveGatheredGoldToDataStore(chara.gold)
        }
    }

    private fun saveHighscore() {
        viewModelScope.launch {
            dataStoreManager?.saveHighscoreToDataStore(chara.gold)
        }
    }

    // ----------- ANIMATIONS -------------

    private fun jumpAnimation() {
        _charaScreenStateFlow.update {
            it.copy(jump = true)
        }
        viewModelScope.launch {
            delay(Settings.animDuration)
            _charaScreenStateFlow.update {
                it.copy(jump = false)
            }
        }
    }

    private fun nudgeChara() {
        _charaScreenStateFlow.update {
            it.copy(nudge = true)
        }
        viewModelScope.launch {
            delay(Settings.animDuration)
            _charaScreenStateFlow.update {
                it.copy(nudge = false)
            }
        }
    }

    private fun nudgeEnemy(damageDTO: EnemyDamageDTO) {
        _enemiesStateList.replaceAll {
            if (it.id == damageDTO.id) {
                it.copy(nudge = true)
            } else {
                it
            }
        }
        viewModelScope.launch {
            delay(Settings.animDuration)
            _enemiesStateList.replaceAll {
                if (it.id == damageDTO.id) {
                    it.copy(nudge = false)
                } else {
                    it
                }
            }
        }
    }

    private fun flashEnemiesRed(id: String) {
        _enemiesStateList.replaceAll {
            if (it.id == id) {
                it.copy(flashRed = true)
            } else {
                it
            }
        }
        viewModelScope.launch {
            delay(Settings.animDuration)
            _enemiesStateList.replaceAll {
                if (it.id == id) {
                    it.copy(flashRed = false)
                } else {
                    it
                }
            }
        }
    }

    private fun flashCharaRed() {
        _charaScreenStateFlow.update {
            it.copy(flashRed = true)
        }
        viewModelScope.launch {
            delay(Settings.animDuration)
            _charaScreenStateFlow.update {
                it.copy(flashRed = false)
            }
        }
    }

    // ----------- LEVEL OBJECTS LIST OPERATIONS -------------

    private fun addNewGameObjectsToObjectsList() {
        level.gameObjectIds.forEach { id ->
            val coordinates = findCoordinate(id)
            if (coordinates != Coordinates(-1, -1)) {
                val newObject =
                    level.fieldHelperService.field[coordinates.x][coordinates.y].find { it.id == id }
                if (newObject != null) {
                    var direction = Direction.DOWN
                    if (newObject is Arrow) {
                        direction = newObject.direction
                    }
                    _objectsStateList.add(
                        LevelObjectState(
                            id,
                            newObject.type,
                            coordinates,
                            direction
                        )
                    )
                }
            }
        }
    }

    // ----------- CONTROL GAME STATE -------------

    fun onPause() {
        if (this::level.isInitialized) {
            level.fieldHelperService.gamePaused = true
            gamePaused.update {
                true
            }
            mediaPlayerService.pauseMediaPlayers()
        }
    }

    fun resumeGame() {
        if (this::level.isInitialized) {
            level.fieldHelperService.gamePaused = false
            gamePaused.update {
                false
            }
            mediaPlayerService.startMediaPlayerByLevelCount(level.levelCount)
        }
    }

    fun onGiveUp() {
        viewModelScope.launch {
            _gameState.emit(GameState.EndGameOnGiveUp)
        }
    }

    private fun nextLevel() {
        val levelCount = level.levelCount
        level.levelCount = levelCount + 1
        if (levelCount >= Settings.enemiesPerLevel.size) {
            saveGold()
            _gameState.update {
                GameState.EndGameOnVictory
            }
        } else {
            _gameState.update {
                GameState.NextLevel
            }
        }
    }

    fun reset(newGame: Boolean = true, context: Context) {
        if (this::level.isInitialized) {
            level.fieldHelperService.gamePaused = false
        }
        gamePaused.update { false }

        if (newGame) {
            onNewGame(context)
        } else {
            onNextLevel(context)
        }
        chara.position = findCoordinate(chara.id)
        _charaScreenStateFlow.update {
            it.copy(position = chara.position, health = chara.health, gold = chara.gold)
        }
        _enemiesStateList.clear()

        _fieldLayoutState.update {
            level.fieldLayout
        }

        setupEnemies()
        _objectsStateList.clear()
        addNewGameObjectsToObjectsList()
    }

    private fun onNewGame(context: Context) {
        chara = MainChara()
        level = Level(chara)
        level.initLevel(context)

        loadSaveData()
        _gameState.update {
            GameState.NextLevelReady(level.levelCount)
        }
    }

    private fun onNextLevel(context: Context) {
        level.initLevel(context)

        viewModelScope.launch {
            delay(100.milliseconds)
            // TODO whatever the fuck is wrong with this
            chara.position = findCoordinate(chara.id)
            _charaScreenStateFlow.update {
                it.copy(
                    position = findCoordinate(chara.id),
                    health = chara.health,
                    gold = chara.gold,
                    fixated = false
                )
            }
            delay(200.milliseconds)
            _gameState.emit(GameState.NextLevelReady(level.levelCount))
        }
    }

    private fun loadSaveData() {
        viewModelScope.launch {
            dataStoreManager?.getDataFromDataStoreGameScreen()?.collect {
                val charaStats = CharaStats(
                    health = it.health,
                    attack = it.attack,
                    defense = it.defense,
                    gold = it.gold
                )
                chara.setBaseValues(charaStats)
                _charaScreenStateFlow.update {
                    CharaScreenState(
                        direction = Direction.DOWN,
                        nudge = false,
                        jump = false,
                        position = chara.position,
                        flashRed = false,
                        health = chara.health,
                        gold = 0,
                        weaponId = "",
                        cuirassId = "",
                        fixated = false,
                        bombAmount = 0
                    )
                }
            }
        }
    }

    private fun setupEnemies() {
        level.movableEntitiesList.filter { it.type == LevelObjectType.ENEMY }.forEach {
            val enemyType = when (it as BasicEnemy) {
                is Slime -> EnemyEnum.SLIME
                is Wolf -> EnemyEnum.WOLF
                is Ogre -> EnemyEnum.OGRE
                is Plant -> EnemyEnum.PLANT
                else -> throw MissingEnemyTypeException("Enemy type not mapped for this enemy. Probably forgot to add here after adding new enemy.")
            }
            _enemiesStateList.add(
                EnemyState(
                    it.id,
                    nudge = false,
                    jump = false,
                    it.direction,
                    it.position,
                    enemyType,
                    flashRed = false,
                    visible = true,
                    loadsAttack = false,
                    healthPercentage = 1.0
                )
            )
        }
        stopAllEnemyCollectionJobs()
        setupEnemyPositionChangeCollector()
        setupEnemyCollector()
    }

    private fun stopAllEnemyCollectionJobs() {
        enemyPositionChangeJob?.cancel()
        enemyAttackJobs.forEach { it.cancel() }
        enemyAttackJobs.clear()
    }

    private fun setupEnemyCollector() {
        level.movableEntitiesList.filterIsInstance<BasicEnemy>().forEach {
            enemyAttackJobs.add(viewModelScope.launch {
                it.attackDamage.collect { dto ->
                    onEnemyAttack(dto)
                }
            })
            if (it is Ogre) {
                viewModelScope.launch {
                    it.pebbleFlow.collect { pebble ->
                        if (pebble != null) {
                            throwPebble(pebble.position, pebble.direction)
                        }
                    }
                }
            }
            if (it is Plant) {
                viewModelScope.launch {
                    it.fixateCharaFlow.collect { fixated ->
                        if (fixated) {
                            level.fixateChara()
                            _charaScreenStateFlow.update { chara ->
                                chara.copy(
                                    fixated = true
                                )
                            }
                            flashCharaRed()
                        } else {
                            level.releaseChara()
                            _charaScreenStateFlow.update { chara ->
                                chara.copy(
                                    fixated = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupEnemyPositionChangeCollector() {
        enemyPositionChangeJob = viewModelScope.launch {
            level.enemyPositionFlow.collect { changeDto ->
                _enemiesStateList.replaceAll {
                    if (it.id == changeDto.id) {
                        it.copy(
                            position = changeDto.newPosition,
                            direction = changeDto.newDirection,
                            loadsAttack = changeDto.loadAttack
                        )
                    } else {
                        it
                    }
                }
            }
        }
    }
}

class MissingEnemyTypeException(message: String) : Exception(message)