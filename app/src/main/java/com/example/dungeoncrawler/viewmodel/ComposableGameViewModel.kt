package com.example.dungeoncrawler.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dungeoncrawler.CharaState
import com.example.dungeoncrawler.EnemyState
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.entity.CharaStats
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min

class ComposableGameViewModel(application: Application) : AndroidViewModel(application) {

    // TODO: UninitializedPropertyAccessException
    lateinit var level: Level

    var chara = MainChara()

    private val _charaStateFlow =
        MutableStateFlow(CharaState(direction = Direction.DOWN, nudge = false, jump = false, position = chara.position, flashRed = false))
    val charaStateFlow = _charaStateFlow.asStateFlow()

    private val _enemiesStateFlow = MutableStateFlow(listOf<EnemyState>())
    val enemiesStateFlow = _enemiesStateFlow.asStateFlow()

    private val _endGame: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    private val endGame = _endGame.asStateFlow()

    fun moveUp() {

        if (turn(Direction.UP)) {
            _charaStateFlow.update {
                it.copy(direction = Direction.UP)
            }
            return
        }

        //gameViewModel.moveUp()
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

        //gameViewModel.moveDown()
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x, coordinates.y + 1)
        moveIfPossible(newCoordinates, coordinates)
        //redraw(charaMoves = true)
    }

    fun moveLeft() {
        val turn = turn(Direction.LEFT)

        if (turn) {
            _charaStateFlow.update {
                it.copy(direction = Direction.LEFT)
            }
            return
        }

        //gameViewModel.moveLeft()
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x - 1, coordinates.y)
        moveIfPossible(newCoordinates, coordinates)
        //redraw(charaMoves = true)
    }

    fun moveRight() {
        val turn = turn(Direction.RIGHT)

        if (turn) {
            _charaStateFlow.update {
                it.copy(direction = Direction.RIGHT)
            }
            return
        }

        //gameViewModel.moveRight()
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x + 1, coordinates.y)
        moveIfPossible(newCoordinates, coordinates)

        //redraw(charaMoves = true)
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
        // TODO: level as state
        //updateLevel.value = true

        levelObjectList.add(chara)
        _charaStateFlow.update {
            it.copy(position = newCoordinates)
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
        //gameViewModel.interact()
        var coordinates = chara.position

        coordinates = when (chara.direction) {
            Direction.UP -> Coordinates(coordinates.x, coordinates.y - 1)
            Direction.DOWN -> Coordinates(coordinates.x, coordinates.y + 1)
            Direction.LEFT -> Coordinates(coordinates.x - 1, coordinates.y)
            Direction.RIGHT -> Coordinates(coordinates.x + 1, coordinates.y)
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
            }

            LevelObjectType.LADDER -> {
                nextLevel()
            }

            LevelObjectType.COIN -> {
                getRandomReward()
                levelObjectList.removeIf { it.id == levelObject.id }
            }

            LevelObjectType.POTION -> {
                heal((levelObject as Potion).hpCure)
                levelObjectList.removeIf { it.id == levelObject.id }
            }

            LevelObjectType.WEAPON -> {
                takeWeapon(coordinates)
            }

            LevelObjectType.ARMOR -> {
                takeArmor(coordinates)
            }

            else -> {}

        }

        //val charaView = getGameObjectView(view, gameViewModel.chara.id)
        //nudge(charaView, gameViewModel.chara.id, gameViewModel.chara.direction)

        //updateLevel()
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
            _endGame.update {
                true
            }
        } else {
            level.nextLevel.value = levelCount
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
        // TODO: animate on screen
        //attackedEntityAnimation.value = attackedEnemy.id
        if (attackedEnemy.health <= 0) {
            onEnemyDefeated(attackedEnemy)
        }

    }

    private fun placeCoin(position: Coordinates) {
        val coin = level.coinStack.removeFirst()
        level.field[position.x][position.y].add(Coin(coin))
        level.coinStack.addLast(coin)
    }

    private fun placePotion(position: Coordinates) {
        val potion = level.potionStack.removeFirst()
        level.field[position.x][position.y].add(Potion(potion))
        level.coinStack.addLast(potion)
    }

    private fun placeWeapon(position: Coordinates) {
        val weapon = level.randomWeapon()
        level.field[position.x][position.y].add(weapon)
    }

    private fun placeArmor(position: Coordinates) {
        val armor = level.randomArmor()
        level.field[position.x][position.y].add(armor)
    }

    private fun onEnemyDefeated(attackedEnemy: BasicEnemy) {
        if (attackedEnemy is Ogre) {
            level.endBossDefeated()
            return
        }
        placeCoin(attackedEnemy.position)
        attackedEnemy.position = Coordinates(-1, -1)
    }

    fun reset(newGame: Boolean = true) {
        _endGame.update {
            null
        }
        if (newGame) {
            chara = MainChara()
            level = Level(chara)
            chara.position = findCoordinate(chara.id)
            _charaStateFlow.update {
                it.copy(position = chara.position)
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
                    list.add(EnemyState(it.id, nudge = false, jump = false, it.direction, it.position, enemyType, flashRed = false ))
                }
                return@update list
            }
            setupEnemyPositionChangeCollector()
        }

        viewModelScope.launch {
            getApplication<Application>().dataStore.data.collect { preferences ->
                val health = preferences[intPreferencesKey(MenuViewModel.HEALTH_KEY)]
                    ?: Settings.healthBaseValue
                val attack = preferences[intPreferencesKey(MenuViewModel.ATTACK_KEY)]
                    ?: Settings.attackBaseValue
                val defense = preferences[intPreferencesKey(MenuViewModel.DEFENSE_KEY)]
                    ?: Settings.defenseBaseValue
                val charaStats = CharaStats(health, attack, defense)
                chara.setBaseValues(charaStats)
            }
        }

    }

    private fun setupEnemyPositionChangeCollector() {
        viewModelScope.launch {
            level.enemyPositionFlow.collect { changeDto ->
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

    private fun setupEnemyObservers() {
        level.movableEntitiesList.filterIsInstance<BasicEnemy>().forEach {
            viewModelScope.launch {
                it.attackDamage.collect { dto ->
                    onEnemyAttack(dto)
                }
            }
        }
//        gameViewModel.level.movableEntitiesList.filterIsInstance<BasicEnemy>().forEach {
//            enemyPositionChangeFlowCollectionJobList.add(
//                scope.launch {
//                    it.positionChange.collect { dto ->
//                        onEnemyMove(view, dto)
//                    }
//                }
//            )
//            enemyAttackFlowCollectionJobList.add(
//                scope.launch {
//                    it.attackDamage.collect { dto ->
//                        onEnemyAttack(dto, view)
//                    }
//                }
//            )
//        }
    }

    private fun onEnemyAttack(
        damageDTO: EnemyDamageDTO
    ) {
        val protection = chara.armor?.protection ?: 0
        chara.health -= max(0, (damageDTO.damage - (chara.baseDefense + protection)))
        if (chara.health <= 0) {
            //TODO
            //killedBy = enemyId
            //endGame.value = false
        }

        flashCharaRed()

//        binding?.health?.text = String.format(
//            resources.getString(
//                R.string.health,
//                gameViewModel.chara.health
//            )
//        )
//        val enemyView = getGameObjectView(view, it.id)
//        //nudge(enemyView, it.id, it.direction)
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