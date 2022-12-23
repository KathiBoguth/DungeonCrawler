package com.example.dungeoncrawler

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.dungeoncrawler.databinding.FragmentGameViewBinding
import com.example.dungeoncrawler.entity.BasicEnemy
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.EnemyDamageDTO
import com.example.dungeoncrawler.entity.EnemyPositionChangeDTO
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.MovableEntity
import com.example.dungeoncrawler.entity.weapon.Weapon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class GameView : Fragment() {

    private var backgroundPos = Coordinates(-1,-1)
    private var backgroundOrigPos = Coordinates(-1,-1)
    val gameViewModel: GameViewModel by activityViewModels()

    private var binding: FragmentGameViewBinding? = null

    private val scope = CoroutineScope(Dispatchers.IO)
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var enemyObserver: Observer<EnemyPositionChangeDTO>
    private lateinit var enemyDamageObserver: Observer<EnemyDamageDTO>
    private lateinit var charaWeaponObserver: Observer<Weapon>

    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            if (backgroundPos.x == -1) {
                val background = view?.findViewById<ImageView>(R.id.background)
                backgroundPos = Coordinates(background?.x?.toInt() ?: -1, background?.y?.toInt() ?: -1)
                backgroundOrigPos = backgroundPos
                drawObjects(0)
            } else {
                drawObjects()
            }
            handler.postDelayed(this, 100)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentGameViewBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        gameViewModel.reset()

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            gameFragment = this@GameView
        }

        scope.launch {
            handler.postDelayed(runnableCode, 5)
        }

        enemyObserver = Observer<EnemyPositionChangeDTO>{
            val enemyView = getGameObjectView(view, it.id)
            val jumpUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.jump)
            enemyView?.startAnimation(jumpUpAnimation)

            gameViewModel.onEnemyPositionChange(it)
        }

        gameViewModel.level.enemies.forEach {
            it.positionChange.observe(viewLifecycleOwner, enemyObserver)
        }

        enemyDamageObserver = Observer<EnemyDamageDTO> {
            gameViewModel.onEnemyAttack(it.damage, it.id)
            binding?.health?.text = String.format(
                resources.getString((R.string.health),
                    gameViewModel.chara.health.toString())
            )
            if (gameViewModel.chara.health <= 0) {
                this.findNavController().navigate(R.id.action_gameView_to_gameOverView)
            }
        }
        gameViewModel.level.enemies.forEach {
            it.attackDamage.observe(viewLifecycleOwner, enemyDamageObserver)
        }

        charaWeaponObserver = Observer<Weapon> {
            showWeapon(it.id)
        }

        gameViewModel.chara.weaponObservable.observe(viewLifecycleOwner, charaWeaponObserver)
    }

    fun interact() {
        val endGame = gameViewModel.interact()
        if (endGame) {
            this.findNavController().navigate(R.id.action_gameView_to_victoryView)
            return
        }
        removeTreasures()
        removeEnemies()
        removeCoins()
        removeWeapons()
        updateStats()
    }

    fun moveUp() {

        val turn = gameViewModel.turn(Direction.UP)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_back, requireContext().theme))
            return
        }
        gameViewModel.moveUp(gameViewModel.chara.id)

        redraw()
    }

    fun moveDown() {
        val turn = gameViewModel.turn(Direction.DOWN)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_front, requireContext().theme))
            return
        }

        gameViewModel.moveDown(gameViewModel.chara.id)

        redraw()
    }

    fun moveLeft() {
        val turn = gameViewModel.turn(Direction.LEFT)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_left, requireContext().theme))
            return
        }

        gameViewModel.moveLeft(gameViewModel.chara.id)

        redraw()
    }

    fun moveRight() {
        val turn = gameViewModel.turn(Direction.RIGHT)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_right, requireContext().theme))
            return
        }

        gameViewModel.moveRight(gameViewModel.chara.id)

        redraw()
    }

    private fun redraw() {
        val background = view?.findViewById<ImageView>(R.id.background)
        val chara = view?.findViewById<ImageView>(R.id.character)

        val jumpUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.jump)

        if (background == null) {
            return
        }
        val charaPosition = gameViewModel.findCoordinate("character")
        val xPosBackground = backgroundOrigPos.x.minus(charaPosition.x*Settings.moveLength)
        val yPosBackground = backgroundOrigPos.y.minus(charaPosition.y*Settings.moveLength)

        backgroundPos = Coordinates(xPosBackground.toInt(), yPosBackground.toInt())

        chara?.startAnimation(jumpUpAnimation)
        background.animate().x(xPosBackground).y(yPosBackground).setDuration(100)

        drawObjects()
    }

    private fun drawObjects(duration: Long = 100) {
        for (x in 0 until gameViewModel.level.field.size) {
            for (y in 0 until gameViewModel.level.field[x].size) {
                if (gameViewModel.level.field[x][y] != null && gameViewModel.level.field[x][y]?.id != gameViewModel.chara.id) {
                    moveObject(x, y, duration)
                }
            }
        }
    }

    private fun moveObject(x: Int, y: Int, duration: Long) {
        val gameObject = gameViewModel.level.field[x][y]
        val gameObjectView =
            view?.findViewById<ImageView>(resources.getIdentifier(gameObject?.id, "id", requireContext().packageName))
                ?: return

        val xPos = x*Settings.moveLength + backgroundPos.x + Settings.margin
        val yPos = y*Settings.moveLength + backgroundPos.y + Settings.margin

        if(gameObject?.type == LevelObjectType.COIN || gameObject?.type == LevelObjectType.WEAPON) {
            gameObjectView.x = xPos
            gameObjectView.y = yPos
        } else {
            gameObjectView.animate().x(xPos).y(yPos).setDuration(duration)
        }

        gameObjectView.visibility = View.VISIBLE
        gameObjectView.bringToFront()

        if (gameObject is BasicEnemy) {
            if (gameObject.health <= 0) {
                gameViewModel.level.field[x][y] = null
                gameObjectView.visibility = View.INVISIBLE
                gameObject.positionChange.removeObserver(enemyObserver)
                return
            }
            val drawableId = when((gameViewModel.level.field[x][y] as MovableEntity).direction) {
                Direction.DOWN -> R.drawable.slime_front
                Direction.UP -> R.drawable.slime_back
                Direction.LEFT -> R.drawable.slime_left
                Direction.RIGHT -> R.drawable.slime_right

            }
            gameObjectView.setImageDrawable(ResourcesCompat.getDrawable(resources, drawableId, requireContext().theme))

        }
    }

    private fun removeTreasures() {
        for (i in 0..Settings.treasureMax) {
            val treasureId = "treasure$i"
            hideGameObjectIfRemoved(treasureId)
        }
    }

    private fun removeEnemies() {
        for (enemy in  gameViewModel.level.enemies) {
            hideGameObjectIfRemoved(enemy.id)
        }
    }

    private fun removeCoins() {
        for (i in 0..Settings.coinsMax) {
            val coinId = "coin$i"
            hideGameObjectIfRemoved(coinId)
        }
    }

    private fun removeWeapons() {
        for (id in gameViewModel.level.swordIds) {

            hideGameObjectIfRemoved(id)
        }
    }

    private fun hideGameObjectIfRemoved(id: String) {
        if (!gameViewModel.level.field.any { arrayOfLevelObjects -> arrayOfLevelObjects.any { it?.id == id }}) {
            val gameObjectView = view?.findViewById<ImageView>(resources.getIdentifier(id, "id", requireContext().packageName))
            gameObjectView?.visibility = View.INVISIBLE
        }
    }

    private fun updateStats() {
        binding?.goldCounter?.text = String.format(
            resources.getString((R.string.gold),
            gameViewModel.chara.gold.toString())
        )
    }

    private fun showWeapon(id: String) {
        for (i in gameViewModel.level.swordIds) {
            val weaponId = "gui_$i"
            val weaponView = view?.findViewById<View>(
                resources.getIdentifier(
                    weaponId,
                    "id",
                    requireContext().packageName
                )
            )
            weaponView?.visibility = View.INVISIBLE
        }

        val weaponId = "gui_$id"
        val weaponView = view?.findViewById<View>(
            resources.getIdentifier(
                weaponId,
                "id",
                requireContext().packageName
            )
        )
        weaponView?.visibility = View.VISIBLE
    }

    private fun getGameObjectView(view: View, objectId: String): ImageView? {
        val coordinates = gameViewModel.findCoordinate(objectId)
        val id = gameViewModel.level.field[coordinates.x][coordinates.y]?.id
        return view.findViewById(
            resources.getIdentifier(
                id,
                "id",
                requireContext().packageName
            )
        )
    }

}

