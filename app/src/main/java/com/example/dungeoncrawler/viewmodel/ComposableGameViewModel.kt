package com.example.dungeoncrawler.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dungeoncrawler.data.CharaState
import com.example.dungeoncrawler.data.EnemyState
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.data.CharaStats
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
import com.example.dungeoncrawler.entity.weapon.Bow
import com.example.dungeoncrawler.entity.weapon.Weapon
import com.example.dungeoncrawler.service.DataStoreManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

class ComposableGameViewModel(application: Application) : AndroidViewModel(application) {

    // TODO: UninitializedPropertyAccessException
    lateinit var level: Level

    var chara = MainChara()

    private val _charaStateFlow =
        MutableStateFlow(CharaState(direction = Direction.DOWN, nudge = false, jump = false, position = chara.position, flashRed = false, health = Settings.healthBaseValue, gold = 0))
    val charaStateFlow = _charaStateFlow.asStateFlow()

    private val _enemiesStateFlow = MutableStateFlow(listOf<EnemyState>())
    val enemiesStateFlow = _enemiesStateFlow.asStateFlow()

    private val _objectsStateFlow = MutableStateFlow(listOf<LevelObjectState>())
    val objectsStateFlow = _objectsStateFlow.asStateFlow()

    private val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState.InitGame(0))
    val gameState = _gameState.asStateFlow()

    private var dataStoreManager: DataStoreManager? = null

    private var enemyPositionChangeJob: Job? = null // TODO: better solution?
    private val enemyAttackJobs: MutableList<Job> = mutableListOf()

    fun initDataStoreManager(newManager: DataStoreManager){
        dataStoreManager = newManager
    }

    fun moveUp() {

        if (turn(Direction.UP)) {
            _charaStateFlow.update {
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
            _charaStateFlow.update {
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
            _charaStateFlow.update {
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
            _charaStateFlow.update {
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
        _charaStateFlow.update {
            it.copy(position = chara.position)
        }
    }

    private fun jumpAnimation() {
        _charaStateFlow.update {
            it.copy(jump = true)
        }
        viewModelScope.launch {
            delay(Settings.animDuration)
            _charaStateFlow.update {
                it.copy(jump = false)
            }
        }
    }

    private fun turn(direction: Direction): Boolean {

        if (chara.direction == direction) {
            return false
        }
        chara.direction = direction
        _charaStateFlow.update {
            it.copy(direction = direction)
        }
        return true
    }

    fun interact() {
        _charaStateFlow.update {
            it.copy(nudge = true)
        }
        viewModelScope.launch {
            delay(Settings.animDuration)
            _charaStateFlow.update {
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
    }

    private fun heal(hpCure: Int) {
        chara.health = min(hpCure + chara.health, chara.maxHealth)
    }

    private fun nextLevel() {
        val levelCount = level.levelCount
        level.levelCount = levelCount + 1
        if (levelCount > Settings.levelsMax) {
            _gameState.update {
                GameState.EndGameOnVictory
            }
        } else {
            reset(newGame = false)
        }
    }

    private fun throwArrow(coordinates: Coordinates, direction: Direction) {
        level.throwArrow(coordinates, direction, ::attack)
    }

    private fun takeWeapon(coordinates: Coordinates) {
        val oldWeapon = chara.weapon
        val weapon = level.field[coordinates.x][coordinates.y]
            .find { it.type == LevelObjectType.WEAPON } as Weapon

        chara.putOnWeapon(weapon)

        if (oldWeapon != null) {
            level.field[coordinates.x][coordinates.y].add(0, oldWeapon)
        } else {
            level.field[coordinates.x][coordinates.y].removeIf { it.id == weapon.id }
        }
    }

    private fun takeArmor(coordinates: Coordinates) {
        val oldArmor = chara.armor
        val armor = level.field[coordinates.x][coordinates.y]
            .find { it.type == LevelObjectType.ARMOR } as Armor

        chara.putOnArmor(armor)

        if (oldArmor != null) {
            level.field[coordinates.x][coordinates.y].add(0, oldArmor)
        } else {
            level.field[coordinates.x][coordinates.y].removeIf { armor.id == it.id }
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
        addToLevelObjectStateFlow(LevelObjectState("", LevelObjectType.COIN, position))
    }

    private fun placePotion(position: Coordinates) {
        val potion = level.potionStack.removeFirst()
        level.field[position.x][position.y].add(Potion(potion))
        level.potionStack.addLast(potion)
        addToLevelObjectStateFlow(LevelObjectState("", LevelObjectType.POTION, position))
    }

    private fun placeWeapon(position: Coordinates) {
        val weapon = level.randomWeapon()
        level.field[position.x][position.y].add(weapon)
        addToLevelObjectStateFlow(LevelObjectState(weapon.id, LevelObjectType.WEAPON, position))

    }

    private fun placeArmor(position: Coordinates) {
        val armor = level.randomArmor()
        level.field[position.x][position.y].add(armor)
        addToLevelObjectStateFlow(LevelObjectState(armor.id, LevelObjectType.ARMOR, position))
    }

    private fun addToLevelObjectStateFlow(levelObject: LevelObjectState) {
        _objectsStateFlow.update {oldList ->
            val newList = oldList.toMutableList()
            newList.add(levelObject)
            return@update newList
        }
    }

    private fun removeFromLevelObjectStateFlow(levelObjectId: String) {
        _objectsStateFlow.update {oldList ->
            val newList = oldList.toMutableList()
            newList.removeIf{it.id == levelObjectId}
            return@update newList
        }
    }

    private fun onEnemyDefeated(attackedEnemy: BasicEnemy) {
        if (attackedEnemy is Ogre) {
            level.endBossDefeated()
            return
        }
        placeCoin(attackedEnemy.position)
        attackedEnemy.position = Coordinates(-1, -1)
        _enemiesStateFlow.update {
            val newList = mutableStateListOf<EnemyState>()
            it.forEach { enemyState ->
                if (enemyState.id == attackedEnemy.id) {
                    newList.add(enemyState.copy(visible = false))
                } else {
                    newList.add(enemyState)
                }
            }
            return@update newList
        }
    }

    fun reset(newGame: Boolean = true) {

        if (newGame) {
            chara = MainChara()
            level = Level(chara)

            viewModelScope.launch {
                dataStoreManager?.getDataFromDataStore()?.collect{
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
                delay(300.milliseconds)
                _gameState.emit(GameState.NextLevelReady(level.levelCount))
            }
//            binding?.level?.text = String.format(
//                resources.getString(
//                    (R.string.level),
//                    gameViewModel.level.levelCount.toString()
//                )
//            )

//            if (gameViewModel.level.levelCount >= Settings.enemiesPerLevel.size){
//                mediaPlayerDungeon.pause()
//                mediaPlayerBoss.start()
//            }
        }
        chara.position = findCoordinate(chara.id)
        _charaStateFlow.update {
            it.copy(position = chara.position, health = chara.health, gold = chara.gold)
        }
        _enemiesStateFlow.update {
            val list = mutableStateListOf<EnemyState>()
            level.movableEntitiesList.filter { it.type == LevelObjectType.ENEMY }.forEach{
                val enemyType = when(it as BasicEnemy){
                    is Slime -> EnemyEnum.SLIME
                    is Wolf -> EnemyEnum.WOLF
                    is Ogre -> EnemyEnum.OGRE
                    else -> throw MissingEnemyTypeException("Enemy type not mapped for this enemy. Probably forgot to add here after adding new enemy.")
                }
                list.add(EnemyState(it.id, nudge = false, jump = false, it.direction, it.position, enemyType, flashRed = false, visible = true ))
            }
            return@update list
        }
        stopAllEnemyCollectionJobs()
        setupEnemyPositionChangeCollector()
        setupEnemyCollector()

        _objectsStateFlow.update {
            val newList = mutableListOf<LevelObjectState>()
            level.gameObjectIds.forEach{id ->
                val coordinates = findCoordinate(id)
                if (coordinates != Coordinates(-1,-1)){
                    val newObject = level.field[coordinates.x][coordinates.y].find { it.id == id }
                    if (newObject != null){
                        newList.add(LevelObjectState(id, newObject.type, coordinates))
                    }
                }

            }
            return@update newList
        }

    }

    private fun setupEnemyPositionChangeCollector() {
        enemyPositionChangeJob = viewModelScope.launch {
            level.enemyPositionFlow.collect { changeDto ->
                // TODO: probably not needed
                val enemyIndex = level.movableEntitiesList.indexOfFirst { it.id == changeDto.id }
                if (enemyIndex != -1) {
                    val enemy = level.movableEntitiesList[enemyIndex]
                    enemy.position = changeDto.newPosition
                    level.movableEntitiesList[enemyIndex] = enemy
                }

                _enemiesStateFlow.update {
                    val newList = mutableStateListOf<EnemyState>()
                    it.forEach { enemyState ->
                        if (enemyState.id == changeDto.id) {
                            newList.add(enemyState.copy(position = changeDto.newPosition, direction = changeDto.newDirection))
                        } else {
                            newList.add(enemyState)
                        }
                    }
                    return@update newList
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
        enemyAttackJobs.forEach{ it.cancel() }
        enemyAttackJobs.clear()
    }

    private fun onEnemyAttack(
        damageDTO: EnemyDamageDTO
    ) {
        nudgeEnemy(damageDTO)

        val protection = chara.armor?.protection ?: 0
        chara.health -= max(0, (damageDTO.damage - (chara.baseDefense + protection)))
        _charaStateFlow.update {
            it.copy(health = chara.health)
        }

        if (chara.health <= 0) {
            //TODO
            //killedBy = enemyId
            //hideAllEnemies()
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
        _enemiesStateFlow.update { enemiesState ->
            val newList = enemiesState.toMutableStateList()
            val enemyState = enemiesState.firstOrNull { it.id == damageDTO.id }
            if(enemyState != null){
                newList[enemiesState.indexOfFirst { it.id == damageDTO.id }] =
                    enemyState.copy(nudge = true)
            }
            return@update newList
        }
        viewModelScope.launch {
            delay(Settings.animDuration)
            _enemiesStateFlow.update { enemiesState ->
                val newList = enemiesState.toMutableStateList()
                val enemyState = enemiesState.firstOrNull { it.id == damageDTO.id }
                if(enemyState != null) {
                    newList[enemiesState.indexOfFirst { it.id == damageDTO.id }] =
                        enemyState.copy(nudge = false)
                }

                return@update newList
            }
        }
    }

    private fun flashEnemiesRed(id: String) {
        _enemiesStateFlow.update {enemiesState ->
            val newList = enemiesState.toMutableStateList()
            val enemyState = enemiesState.first { it.id == id }
            newList[enemiesState.indexOfFirst { it.id == id }] = enemyState.copy(flashRed = true)

            return@update newList
        }
        viewModelScope.launch {
            delay(Settings.animDuration)
            _enemiesStateFlow.update {enemiesState ->
                val newList = enemiesState.toMutableStateList()
                val enemyState = enemiesState.first { it.id == id }
                newList[enemiesState.indexOfFirst { it.id == id }] = enemyState.copy(flashRed = false)

                return@update newList
            }
        }
    }

    private fun flashCharaRed() {
        _charaStateFlow.update {
            it.copy(flashRed = true)
        }
        viewModelScope.launch {
            delay(Settings.animDuration)
            _charaStateFlow.update {
                it.copy(flashRed = false)
            }
        }
    }
}


class MissingEnemyTypeException(message: String): Exception(message){

}