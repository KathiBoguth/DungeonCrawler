package com.example.dungeoncrawler.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.CharaScreenState
import com.example.dungeoncrawler.data.CharaStats
import com.example.dungeoncrawler.data.EnemyState
import com.example.dungeoncrawler.data.GameState
import com.example.dungeoncrawler.data.LevelObjectState
import com.example.dungeoncrawler.entity.Coin
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.Level
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.MainChara
import com.example.dungeoncrawler.entity.Potion
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.enemy.EnemyDamageDTO
import com.example.dungeoncrawler.entity.enemy.EnemyEnum
import com.example.dungeoncrawler.entity.enemy.Ogre
import com.example.dungeoncrawler.entity.enemy.Slime
import com.example.dungeoncrawler.entity.enemy.Wolf
import com.example.dungeoncrawler.entity.weapon.Arrow
import com.example.dungeoncrawler.entity.weapon.Bow
import com.example.dungeoncrawler.entity.weapon.Weapon
import com.example.dungeoncrawler.service.DataStoreManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                cuirassId = ""
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

    private var dataStoreManager: DataStoreManager? = null
    private lateinit var mediaPlayerDungeon: MediaPlayer
    private lateinit var mediaPlayerBoss: MediaPlayer

    private var enemyPositionChangeJob: Job? = null // TODO: better solution?
    private val enemyAttackJobs: MutableList<Job> = mutableListOf()

    fun initDataStoreManager(newManager: DataStoreManager) {
        dataStoreManager = newManager
    }

    fun setupMediaPlayer(context: Context) {
        mediaPlayerDungeon = MediaPlayer.create(context, R.raw.dungeon)
        mediaPlayerDungeon.isLooping = true
        mediaPlayerBoss = MediaPlayer.create(context, R.raw.boss)
        mediaPlayerBoss.isLooping = true
    }

    fun startMediaPlayerDungeon() {
        mediaPlayerDungeon.start()
    }

    fun pauseMediaPlayers() {
        if (mediaPlayerDungeon.isPlaying) {
            mediaPlayerDungeon.pause()
        }
        if (mediaPlayerBoss.isPlaying) {
            mediaPlayerBoss.pause()
        }
    }

    fun pauseMediaPlayerDungeon() {
        mediaPlayerDungeon.pause()
    }

    fun startMediaPlayerBoss() {
        mediaPlayerBoss.start()
    }

    fun moveUp() {

        if (turn(Direction.UP)) {
            _charaScreenStateFlow.update {
                it.copy(direction = Direction.UP)
            }
            return
        }

        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x, coordinates.y - 1)

        moveIfPossible(newCoordinates, coordinates)
    }

    fun moveDown() {
        val turn = turn(Direction.DOWN)

        if (turn) {
            _charaScreenStateFlow.update {
                it.copy(direction = Direction.DOWN)
            }
            return
        }

        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x, coordinates.y + 1)
        moveIfPossible(newCoordinates, coordinates)
    }

    fun moveLeft() {
        val turn = turn(Direction.LEFT)

        if (turn) {
            _charaScreenStateFlow.update {
                it.copy(direction = Direction.LEFT)
            }
            return
        }

        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x - 1, coordinates.y)
        moveIfPossible(newCoordinates, coordinates)
    }

    fun moveRight() {
        val turn = turn(Direction.RIGHT)

        if (turn) {
            _charaScreenStateFlow.update {
                it.copy(direction = Direction.RIGHT)
            }
            return
        }

        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x + 1, coordinates.y)
        moveIfPossible(newCoordinates, coordinates)
    }

    private fun moveIfPossible(
        newCoordinates: Coordinates,
        coordinates: Coordinates
    ) {
        if (!movePossible(newCoordinates)) {
            return
        }

        jumpAnimation()

        level.field[coordinates.x][coordinates.y].removeIf { it.id == chara.id }
        val levelObjectList = level.field[newCoordinates.x][newCoordinates.y]
        //TODO: ConcurrentModificationException
        levelObjectList.forEach {
            when (it.type) {
                LevelObjectType.COIN -> {
                    getRandomReward()
                    levelObjectList.removeIf { levelObject -> it.id == levelObject.id }
                }

                LevelObjectType.POTION -> {
                    heal((it as Potion).hpCure)
                }

                LevelObjectType.LADDER -> nextLevel()
                LevelObjectType.WEAPON -> takeWeapon(newCoordinates)
                LevelObjectType.ARMOR -> takeArmor(newCoordinates)

                else -> {}
            }
        }

        levelObjectList.add(chara)
        chara.position = newCoordinates
        _charaScreenStateFlow.update {
            it.copy(position = chara.position)
        }
    }

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

    fun interact() {
        _charaScreenStateFlow.update {
            it.copy(nudge = true)
        }
        viewModelScope.launch {
            delay(Settings.animDuration)
            _charaScreenStateFlow.update {
                it.copy(nudge = false)
            }
        }
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

        val levelObjectList = level.field[coordinates.x][coordinates.y]
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
        val levelObject = levelObjectList.firstOrNull()
        when (levelObject?.type) {
            LevelObjectType.TREASURE -> {
                levelObjectList.removeIf { it.id == levelObject.id }
                when (level.drop()) {
                    LevelObjectType.COIN -> placeCoin(coordinates)
                    LevelObjectType.POTION -> placePotion(coordinates)
                    LevelObjectType.WEAPON -> placeWeapon(coordinates)
                    LevelObjectType.ARMOR -> placeArmor(coordinates)
                    else -> {
                        placeCoin(coordinates)
                    }
                }
                removeFromLevelObjectStateFlow(levelObject.id)
            }

            LevelObjectType.LADDER -> {
                nextLevel()
            }

            LevelObjectType.COIN -> {
                getRandomReward()
                levelObjectList.removeIf { it.id == levelObject.id }
                removeFromLevelObjectStateFlow(levelObject.id)
            }

            LevelObjectType.POTION -> {
                heal((levelObject as Potion).hpCure)
                levelObjectList.removeIf { it.id == levelObject.id }
                removeFromLevelObjectStateFlow(levelObject.id)
            }

            LevelObjectType.WEAPON -> {
                takeWeapon(coordinates)
                removeFromLevelObjectStateFlow(levelObject.id)
            }

            LevelObjectType.ARMOR -> {
                takeArmor(coordinates)
                removeFromLevelObjectStateFlow(levelObject.id)
            }

            else -> {}

        }
    }

    private fun movePossible(coordinates: Coordinates): Boolean {
        if (level.movableEntitiesList.any { it.position == coordinates }) {
            return false
        }
        if (coordinates.x >= level.field.size || coordinates.x < 0) {
            return false
        }
        if (coordinates.y >= level.field[coordinates.x].size || coordinates.y < 0) {
            return false
        }
        val levelObjectList = level.field[coordinates.x][coordinates.y]
        if (levelObjectList.isNotEmpty() && levelObjectList.any { !it.type.isSteppableObject() }) {
            return false
        }
        if (level.movableEntitiesList.any { it.position == coordinates }) {
            return false
        }
        return true
    }

    private fun getRandomReward() {
        chara.gold += level.randomMoney(Settings.treasureMaxMoney)
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

    private fun nextLevel() {
        val levelCount = level.levelCount
        level.levelCount = levelCount + 1
        if (levelCount > Settings.levelsMax) {
            saveGold()
            _gameState.update {
                GameState.EndGameOnVictory
            }
        } else {
            reset(newGame = false)
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

    private fun takeWeapon(coordinates: Coordinates) {
        val oldWeapon = chara.weapon
        val weapon = level.field[coordinates.x][coordinates.y]
            .find { it.type == LevelObjectType.WEAPON } as Weapon

        chara.putOnWeapon(weapon)
        _charaScreenStateFlow.update {
            it.copy(weaponId = weapon.id)
        }

        if (oldWeapon != null) {
            level.field[coordinates.x][coordinates.y].add(0, oldWeapon)
            _objectsStateList.add(
                LevelObjectState(
                    oldWeapon.id,
                    oldWeapon.type,
                    coordinates,
                    Direction.DOWN
                )
            )
        }
        level.field[coordinates.x][coordinates.y].removeIf { it.id == weapon.id }
        _objectsStateList.removeIf { it.id == weapon.id }

    }

    private fun takeArmor(coordinates: Coordinates) {
        val oldArmor = chara.armor
        val armor = level.field[coordinates.x][coordinates.y]
            .find { it.type == LevelObjectType.ARMOR } as Armor

        chara.putOnArmor(armor)
        _charaScreenStateFlow.update {
            it.copy(cuirassId = armor.id)
        }

        if (oldArmor != null) {
            level.field[coordinates.x][coordinates.y].add(0, oldArmor)
            _objectsStateList.add(
                LevelObjectState(
                    oldArmor.id,
                    oldArmor.type,
                    coordinates,
                    Direction.DOWN
                )
            )
        }
        level.field[coordinates.x][coordinates.y].removeIf { armor.id == it.id }
        _objectsStateList.removeIf { it.id == armor.id }

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
        val field = level.field.toList()
        for (row in field.indices) {
            val index =
                field[row].indexOfFirst { it.indexOfFirst { levelObject -> levelObject.id == id } != -1 }
            if (index != -1) {
                return Coordinates(row, index)
            }
        }
        return Coordinates(-1, -1)
    }

    private fun attack(attackedEnemy: BasicEnemy) {
        val weaponBonus = chara.weapon?.attack ?: 0
        attackedEnemy.takeDamage(chara.baseAttack + weaponBonus)
        flashEnemiesRed(attackedEnemy.id)
        if (attackedEnemy.health <= 0) {
            onEnemyDefeated(attackedEnemy)
        }

    }

    private fun placeCoin(position: Coordinates) {
        val coin = level.coinStack.removeFirst()
        level.field[position.x][position.y].add(Coin(coin))
        level.coinStack.addLast(coin)
        addToLevelObjectStateFlow(
            LevelObjectState(
                coin,
                LevelObjectType.COIN,
                position,
                Direction.DOWN
            )
        )
    }

    private fun placePotion(position: Coordinates) {
        val potion = level.potionStack.removeFirst()
        level.field[position.x][position.y].add(Potion(potion))
        level.potionStack.addLast(potion)
        addToLevelObjectStateFlow(
            LevelObjectState(
                potion,
                LevelObjectType.POTION,
                position,
                Direction.DOWN
            )
        )
    }

    private fun placeWeapon(position: Coordinates) {
        val weapon = level.randomWeapon()
        level.field[position.x][position.y].add(weapon)
        addToLevelObjectStateFlow(
            LevelObjectState(
                weapon.id,
                LevelObjectType.WEAPON,
                position,
                Direction.DOWN
            )
        )

    }

    private fun placeArmor(position: Coordinates) {
        val armor = level.randomArmor()
        level.field[position.x][position.y].add(armor)
        addToLevelObjectStateFlow(
            LevelObjectState(
                armor.id,
                LevelObjectType.ARMOR,
                position,
                Direction.DOWN
            )
        )
    }

    private fun addToLevelObjectStateFlow(levelObject: LevelObjectState) {
        _objectsStateList.add(levelObject)
    }

    private fun removeFromLevelObjectStateFlow(levelObjectId: String) {
        _objectsStateList.removeIf { it.id == levelObjectId }
    }

    private fun onEnemyDefeated(attackedEnemy: BasicEnemy) {
        if (attackedEnemy is Ogre) {
            level.endBossDefeated()
            addNewGameObjectsToObjectsList()
        } else {
            placeCoin(attackedEnemy.position)
        }

        // TODO: maybe this could be removed?
        attackedEnemy.position = Coordinates(-1, -1)
        _enemiesStateList.replaceAll {
            if (it.id == attackedEnemy.id) {
                it.copy(visible = false)
            } else {
                it
            }
        }
        _enemiesStateList.removeIf { it.id == attackedEnemy.id }
        attackedEnemy.destroy()

    }

    fun reset(newGame: Boolean = true) {

        if (newGame) {
            chara = MainChara()
            level = Level(chara)

            viewModelScope.launch {
                dataStoreManager?.getDataFromDataStoreGameScreen()?.collect {
                    val charaStats = CharaStats(
                        health = it.health,
                        attack = it.attack,
                        defense = it.defense,
                        gold = it.gold
                    )
                    chara.setBaseValues(charaStats)
                }
            }
            _gameState.update {
                GameState.InitGame(level.levelCount)
            }
        } else {
            viewModelScope.launch {
                _gameState.emit(GameState.NextLevel)
            }

            level.initLevel()

            viewModelScope.launch {
                delay(100.milliseconds)
                // TODO whatever the fuck is wrong with this
                chara.position = findCoordinate(chara.id)
                _charaScreenStateFlow.update {
                    it.copy(
                        position = findCoordinate(chara.id),
                        health = chara.health,
                        gold = chara.gold
                    )
                }
                delay(200.milliseconds)
                _gameState.emit(GameState.NextLevelReady(level.levelCount))
            }
        }
        chara.position = findCoordinate(chara.id)
        _charaScreenStateFlow.update {
            it.copy(position = chara.position, health = chara.health, gold = chara.gold)
        }
        _enemiesStateList.clear()
        level.movableEntitiesList.filter { it.type == LevelObjectType.ENEMY }.forEach {
            val enemyType = when (it as BasicEnemy) {
                is Slime -> EnemyEnum.SLIME
                is Wolf -> EnemyEnum.WOLF
                is Ogre -> EnemyEnum.OGRE
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
                    loadsAttack = false
                )
            )
        }
        stopAllEnemyCollectionJobs()
        setupEnemyPositionChangeCollector()
        setupEnemyCollector()

        _objectsStateList.clear()
        addNewGameObjectsToObjectsList()
    }

    private fun addNewGameObjectsToObjectsList() {
        level.gameObjectIds.forEach { id ->
            val coordinates = findCoordinate(id)
            if (coordinates != Coordinates(-1, -1)) {
                val newObject = level.field[coordinates.x][coordinates.y].find { it.id == id }
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

    private fun setupEnemyCollector() {
        level.movableEntitiesList.filterIsInstance<BasicEnemy>().forEach {
            enemyAttackJobs.add(viewModelScope.launch {
                it.attackDamage.collect { dto ->
                    onEnemyAttack(dto)
                }
            })
        }
    }

    private fun stopAllEnemyCollectionJobs() {
        enemyPositionChangeJob?.cancel()
        enemyAttackJobs.forEach { it.cancel() }
        enemyAttackJobs.clear()
    }

    private fun onEnemyAttack(
        damageDTO: EnemyDamageDTO
    ) {
        if (gameState.value == GameState.EndGameOnGameOver || gameState.value == GameState.EndGameOnVictory) {
            return
        }
        nudgeEnemy(damageDTO)

        val protection = chara.armor?.protection ?: 0
        chara.health -= max(0, (damageDTO.damage - (chara.baseDefense + protection)))
        _charaScreenStateFlow.update {
            it.copy(health = chara.health)
        }

        if (chara.health <= 0) {
            //TODO
            //killedBy = enemyId
            saveGold()

            viewModelScope.launch {
                _gameState.emit(GameState.EndGameOnGameOver)
            }
        }

        flashCharaRed()
    }

    private fun saveGold() {
        viewModelScope.launch {
            dataStoreManager?.saveGatheredGoldToDataStore(chara.gold)
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
}


class MissingEnemyTypeException(message: String) : Exception(message) {

}