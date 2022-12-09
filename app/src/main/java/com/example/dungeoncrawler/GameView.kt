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
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.MovableEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class GameView : Fragment() {

    private var backgroundPos = Coordinates(-1,-1)
    val gameViewModel: GameViewModel by activityViewModels()

    private var binding: FragmentGameViewBinding? = null

    private val scope = CoroutineScope(Dispatchers.IO)
    private var handler = Handler(Looper.getMainLooper())

    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            if (backgroundPos.x == -1) {
                val background = view?.findViewById<ImageView>(R.id.background)
                backgroundPos = Coordinates(background?.x?.toInt() ?: -1, background?.y?.toInt() ?: -1)
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
        // Inflate the layout for this fragment
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

        val enemyObserver = Observer<Coordinates>{
            val enemy = getGameObjectView(view)
            val jumpUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.jump)
            enemy?.startAnimation(jumpUpAnimation)

            gameViewModel.onEnemyPositionChange(it)
        }
        gameViewModel.enemy.positionChange.observe(viewLifecycleOwner, enemyObserver)
    }


    fun interact() {
        val endGame = gameViewModel.interact()
        if (endGame) {
            this.findNavController().navigate(R.id.action_gameView_to_victoryView)
            return
        }
        removeTreasures()
        updateStats()
    }

    fun moveUp() {

        val turn = gameViewModel.turn(Direction.UP)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_back, requireContext().theme))
            return
        }
        val moveBackground = gameViewModel.moveUp(gameViewModel.chara.id)

        redraw(moveBackground)
    }

    fun moveDown() {
        val turn = gameViewModel.turn(Direction.DOWN)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_front, requireContext().theme))
            return
        }

        val moveBackground = gameViewModel.moveDown(gameViewModel.chara.id)

        redraw(moveBackground)
    }

    fun moveLeft() {
        val turn = gameViewModel.turn(Direction.LEFT)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_left, requireContext().theme))
            return
        }

        val moveBackground = gameViewModel.moveLeft(gameViewModel.chara.id)

        redraw(moveBackground)
    }

    fun moveRight() {
        val turn = gameViewModel.turn(Direction.RIGHT)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_right, requireContext().theme))
            return
        }

        val moveBackground = gameViewModel.moveRight(gameViewModel.chara.id)

        redraw(moveBackground)
    }

    private fun redraw(moveBackground: Coordinates) {
        val background = view?.findViewById<ImageView>(R.id.background)
        val chara = view?.findViewById<ImageView>(R.id.character)

        val jumpUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.jump)

        if (background == null) {
            return
        }
        val xPosBackground = background.x.minus(moveBackground.x*Settings.moveLength)
        val yPosBackground = background.y.minus(moveBackground.y*Settings.moveLength)

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
        val id = gameViewModel.level.field[x][y]?.id
        val gameObject =
            view?.findViewById<ImageView>(resources.getIdentifier(id, "id", requireContext().packageName))
                ?: return

        val xPos = x*Settings.moveLength + backgroundPos.x + Settings.margin
        val yPos = y*Settings.moveLength + backgroundPos.y + Settings.margin

        gameObject.animate().x(xPos).y(yPos).setDuration(duration)

        gameObject.visibility = View.VISIBLE
        gameObject.bringToFront()

        if (id == "basicEnemy") {
            val drawableId = when((gameViewModel.level.field[x][y] as MovableEntity).direction) {
                Direction.DOWN -> R.drawable.slime_front
                Direction.UP -> R.drawable.slime_back
                Direction.LEFT -> R.drawable.slime_left
                Direction.RIGHT -> R.drawable.slime_right

            }
            gameObject.setImageDrawable(ResourcesCompat.getDrawable(resources, drawableId, requireContext().theme))

        }
    }

    private fun removeTreasures() {
        for (i in 0..Settings.treasureMax) {
            val treasureId = "treasure$i"
            if (!gameViewModel.level.field.any { arrayOfLevelObjects -> arrayOfLevelObjects.any { it?.id == treasureId }}) {
                val treasure = view?.findViewById<ImageView>(resources.getIdentifier(treasureId, "id", requireContext().packageName))
                treasure?.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateStats() {
        binding?.goldCounter?.text = String.format(
            resources.getString((R.string.gold),
            gameViewModel.chara.gold.toString())
        )
    }

    private fun getGameObjectView(view: View): ImageView? {
        val coordinates = gameViewModel.findCoordinate(gameViewModel.enemy.id)
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

