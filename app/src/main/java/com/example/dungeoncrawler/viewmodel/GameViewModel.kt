package com.example.dungeoncrawler.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dungeoncrawler.Settings
import com.example.dungeoncrawler.entity.CharaStats
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.Coin
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.enemy.LevelObjectPositionChangeDTO
import com.example.dungeoncrawler.entity.Level
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.MainChara
import com.example.dungeoncrawler.entity.Potion
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.weapon.Bow
import com.example.dungeoncrawler.entity.weapon.Weapon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class GameViewModel(application: Application) : AndroidViewModel(application) {

    var chara = MainChara()
    var killedBy = ""

    // TODO: UninitializedPropertyAccessException
    lateinit var level: Level

    val attackedEntityAnimation: MutableLiveData<String> by lazy { MutableLiveData() }
    val endGame: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val updateLevel: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    fun onEnemyPositionChange(levelObjectPositionChangeDTO: LevelObjectPositionChangeDTO) {
        if (!movePossible(levelObjectPositionChangeDTO.newPosition)) {
            return
        }

        val oldCoordinates = findCoordinate(levelObjectPositionChangeDTO.id)
        if(oldCoordinates.x == -1 || oldCoordinates.y == -1 || level.field.isEmpty()) {
            return
        }
        // TODO: ConcurrentModificationException: maybe do something completely different (not a list on field?)
        val enemy = level.movableEntitiesList.find { it.id == levelObjectPositionChangeDTO.id }
        if (enemy != null) {
            enemy.position = levelObjectPositionChangeDTO.newPosition
        }

    }

    fun onEnemyAttack(damage: Int, enemyId: String) {
        val protection = chara.armor?.protection ?: 0
        chara.health -= max(0, (damage - (chara.baseDefense+protection)))
        if (chara.health <= 0) {
            killedBy = enemyId
            endGame.value = false
        }
    }

    fun interact() {
        var coordinates = findCoordinate(chara.id)

        coordinates = when (chara.direction) {
            Direction.UP -> Coordinates(coordinates.x, coordinates.y-1)
            Direction.DOWN -> Coordinates(coordinates.x, coordinates.y+1)
            Direction.LEFT -> Coordinates(coordinates.x-1, coordinates.y)
            Direction.RIGHT -> Coordinates(coordinates.x+1, coordinates.y)
        }

        val levelObjectList = level.field[coordinates.x][coordinates.y]
        if(levelObjectList.isEmpty() && level.movableEntitiesList.none{ it.position == coordinates }) {
            if (chara.weapon is Bow){
                if (!isArrowOnField()) {
                    throwArrow(coordinates, chara.direction)
                }
            }
            return
        }
        val enemy = level.movableEntitiesList.find { it.type == LevelObjectType.ENEMY && it.position == coordinates}
        if (enemy != null) {
            attack(enemy as BasicEnemy)
            return
        }
        val levelObject = levelObjectList.firstOrNull()
        when (levelObject?.type) {
            LevelObjectType.TREASURE -> {
                levelObjectList.removeIf{it.id == levelObject.id}
                when (level.drop()) {
                    LevelObjectType.COIN -> placeCoin(coordinates)
                    LevelObjectType.POTION -> placePotion(coordinates)
                    LevelObjectType.WEAPON -> placeWeapon(coordinates)
                    LevelObjectType.ARMOR -> placeArmor(coordinates)
                    else -> {placeCoin(coordinates)}
                }
            }
            LevelObjectType.LADDER -> {
                nextLevel()
            }
            LevelObjectType.COIN -> {
                getRandomReward()
                levelObjectList.removeIf{it.id == levelObject.id}
            }
            LevelObjectType.POTION -> {
                heal((levelObject as Potion).hpCure)
                levelObjectList.removeIf{it.id == levelObject.id}
            }
            LevelObjectType.WEAPON -> {
                takeWeapon(coordinates)
            }
            LevelObjectType.ARMOR -> {
                takeArmor(coordinates)
            }
            else -> {}

        }
    }

    private fun isArrowOnField(): Boolean {
        return listOf("${Level.ARROW}_up", "${Level.ARROW}_up", "${Level.ARROW}_left", "${Level.ARROW}_right").any{
            findCoordinate(it) != Coordinates(-1, -1)
        }
    }

    private fun throwArrow(coordinates: Coordinates, direction: Direction) {
        level.throwArrow(coordinates, direction, ::attack)
    }

    private fun nextLevel() {
        val levelCount = level.levelCount
        level.levelCount = levelCount +1
        if (levelCount > Settings.levelsMax) {
            endGame.value = true
        } else {
            level.nextLevel.value = levelCount
        }
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

    fun moveUp() {
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x, coordinates.y-1)

        moveIfPossible(newCoordinates, coordinates)
    }

    fun moveDown() {
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x, coordinates.y+1)
        moveIfPossible(newCoordinates, coordinates)
    }

    fun moveLeft() {
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x-1, coordinates.y)
        moveIfPossible(newCoordinates, coordinates)
    }

    fun moveRight() {
        val coordinates = findCoordinate(chara.id)
        if (coordinates.x == -1 || coordinates.y == -1) {
            return
        }
        val newCoordinates = Coordinates(coordinates.x+1, coordinates.y)
        moveIfPossible(newCoordinates, coordinates)

    }

    private fun moveIfPossible(
        newCoordinates: Coordinates,
        coordinates: Coordinates
    ) {
        if (!movePossible(newCoordinates)) {
            return
        }

        level.field[coordinates.x][coordinates.y].removeIf { it.id == chara.id }
        val levelObjectList = level.field[newCoordinates.x][newCoordinates.y]
        //TODO: ConcurrentModificationException
        levelObjectList.forEach {
            when (it.type) {
                LevelObjectType.COIN -> {
                    getRandomReward()
                    levelObjectList.removeIf{levelObject -> it.id == levelObject.id}
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
        updateLevel.value = true

        levelObjectList.add(chara)
    }

    fun findCoordinate(id: String): Coordinates {
        val movableEntity = level.movableEntitiesList.firstOrNull { it.id == id }
        if (movableEntity != null) {
            return movableEntity.position
        }

        // TODO: check if part below is still needed
        // TODO: ConcurrentModificationException
        val field = level.field.toList()
        for (row in field.indices) {
            val index = field[row].indexOfFirst { it.indexOfFirst { levelObject -> levelObject.id == id  } != -1}
            if ( index != -1) {
                return Coordinates(row, index)
            }
        }
        return Coordinates(-1, -1)
    }

    fun turn(direction: Direction): Boolean {

        if (chara.direction == direction) {
            return false
        }
        chara.direction = direction
        return true
    }

    private fun attack(attackedEnemy: BasicEnemy) {
        val weaponBonus = chara.weapon?.attack ?: 0
        attackedEnemy.takeDamage(chara.baseAttack + weaponBonus)
        attackedEntityAnimation.value = attackedEnemy.id
        placeCoinIfEnemyDefeated(attackedEnemy)

    }

    private fun placeCoinIfEnemyDefeated(attackedEnemy: BasicEnemy) {
        if (attackedEnemy.health <= 0) {
            placeCoin(attackedEnemy.position)
            attackedEnemy.position = Coordinates(-1, -1)
        }
    }

    fun reset(newGame: Boolean = true) {
        endGame.value = null
        if (newGame) {
            chara = MainChara()
            level = Level(chara)
        }

        viewModelScope.launch {
            getApplication<Application>().dataStore.data.collect { preferences ->
                val health = preferences[intPreferencesKey(MenuViewModel.HEALTH_KEY)] ?: Settings.healthBaseValue
                val attack = preferences[intPreferencesKey(MenuViewModel.ATTACK_KEY)] ?: Settings.attackBaseValue
                val defense = preferences[intPreferencesKey(MenuViewModel.DEFENSE_KEY)] ?: Settings.defenseBaseValue
                val charaStats = CharaStats(health, attack, defense)
                chara.setBaseValues(charaStats)
            }
        }

    }

    private fun getRandomReward() {
        chara.gold += level.randomMoney(Settings.treasureMaxMoney)
    }

    private fun heal(hpCure: Int) {
        chara.health = min(hpCure + chara.health, chara.maxHealth)
    }

    private fun placeCoin(position: Coordinates){
        val coin = level.coinStack.removeFirst()
        level.field[position.x][position.y].add(Coin(coin))
        level.coinStack.addLast(coin)
    }

    private fun placePotion(position: Coordinates){
        val potion = level.potionStack.removeFirst()
        level.field[position.x][position.y].add(Potion(potion))
        level.coinStack.addLast(potion)
    }

    private fun placeWeapon(position: Coordinates){
        val weapon = level.randomWeapon()
        level.field[position.x][position.y].add(weapon)
    }

    private fun placeArmor(position: Coordinates){
        val armor = level.randomArmor()
        level.field[position.x][position.y].add(armor)
    }

    private fun movePossible(coordinates: Coordinates) : Boolean {
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
        if ( levelObjectList.isNotEmpty() && levelObjectList.any { !it.type.isSteppableObject()} ) {
            return false
        }
        if (level.movableEntitiesList.any { it.position == coordinates }) {
            return false
        }
        return true
    }


}