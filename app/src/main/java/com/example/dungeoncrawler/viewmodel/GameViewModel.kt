package com.example.dungeoncrawler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.dungeoncrawler.entity.MainChara
import com.example.dungeoncrawler.entity.enemy.LevelObjectPositionChangeDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.max

class GameViewModel(application: Application) : AndroidViewModel(application) {

    var chara = MainChara()
    var killedBy = ""

    val attackedEntityAnimation: MutableLiveData<String> by lazy { MutableLiveData() }
    val endGame: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val updateLevel: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    fun onEnemyPositionChange(levelObjectPositionChangeDTO: LevelObjectPositionChangeDTO) {
//        TODO: implement in new viewmodel
        //        if (!movePossible(levelObjectPositionChangeDTO.newPosition)) {
//            return
//        }
//
//        val oldCoordinates = findCoordinate(levelObjectPositionChangeDTO.id)
//        if(oldCoordinates.x == -1 || oldCoordinates.y == -1 || level.field.isEmpty()) {
//            return
//        }
//        // TODO: ConcurrentModificationException: maybe do something completely different (not a list on field?)
//        val enemy = level.movableEntitiesList.find { it.id == levelObjectPositionChangeDTO.id }
//        if (enemy != null) {
//            enemy.position = levelObjectPositionChangeDTO.newPosition
//        }

    }

    fun onEnemyAttack(damage: Int, enemyId: String) {
        val protection = chara.armor?.protection ?: 0
        chara.health -= max(0, (damage - (chara.baseDefense + protection)))
        if (chara.health <= 0) {
            killedBy = enemyId
            endGame.value = false
        }
    }


}