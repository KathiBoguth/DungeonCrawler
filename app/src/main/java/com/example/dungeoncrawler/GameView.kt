package com.example.dungeoncrawler

import android.animation.ValueAnimator
import android.animation.ValueAnimator.REVERSE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.dungeoncrawler.databinding.FragmentGameViewBinding
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.enemy.EnemyDamageDTO
import com.example.dungeoncrawler.entity.enemy.EnemyPositionChangeDTO
import com.example.dungeoncrawler.entity.LevelObject
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.weapon.Weapon
import kotlin.math.abs
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
    private lateinit var charaArmorObserver: Observer<Armor>
    private lateinit var attackedEntityAnimationObserver: Observer<String>
    private lateinit var endGameObserver: Observer<Boolean>
    private lateinit var updateLevelObserver: Observer<Boolean>
    private lateinit var nextLevelObserver: Observer<Int>

    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            if (backgroundPos.x == -1) {
                val character = view?.findViewById<ImageView>(R.id.character)
                backgroundPos = Coordinates(character?.x?.toInt() ?: -1, character?.y?.toInt() ?: -1)
                backgroundOrigPos = backgroundPos
                redraw(0)
            } else {
                redraw()
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

        setupObserver(view)
    }

    private fun setupObserver(view: View) {
        enemyObserver = Observer<EnemyPositionChangeDTO> {
            onEnemyMove(view, it)
        }

        enemyDamageObserver = Observer<EnemyDamageDTO> {
            onEnemyAttack(it, view)
        }

        setupEnemyObservers()

        charaWeaponObserver = Observer<Weapon> {
            showWeapon(it.id)
        }
        charaArmorObserver = Observer<Armor> {
            showArmor(it.id)
        }

        gameViewModel.chara.weaponObservable.observe(viewLifecycleOwner, charaWeaponObserver)
        gameViewModel.chara.armorObservable.observe(viewLifecycleOwner, charaArmorObserver)

        attackedEntityAnimationObserver = Observer<String> {
            val enemyView = getGameObjectView(view, it)

            flashRed(enemyView)

        }
        gameViewModel.attackedEntityAnimation.observe(
            viewLifecycleOwner,
            attackedEntityAnimationObserver
        )

        endGameObserver = Observer<Boolean> { victory ->
            if (victory == null) {
                setupEnemyObservers()
                return@Observer
            }
            hideAllEnemies()
            if (victory) {
                this.findNavController().navigate(R.id.action_gameView_to_victoryView)
            } else {
                this.findNavController().navigate(R.id.action_gameView_to_gameOverView)
            }
        }

        gameViewModel.endGame.observe(
            viewLifecycleOwner,
            endGameObserver
        )

        updateLevelObserver = Observer<Boolean> {
            updateLevel()
        }

        gameViewModel.updateLevel.observe(
            viewLifecycleOwner,
            updateLevelObserver
        )

        nextLevelObserver = Observer<Int> {
            removeEnemyObservers()
            hideAllEnemies()
            fadeView()
            backgroundPos = Coordinates(-1,-1)

            gameViewModel.level.initLevel()

            setupEnemyObservers()
            binding?.level?.text = String.format(
                resources.getString(
                    (R.string.level),
                    gameViewModel.level.levelCount.toString()
                )
            )
            redraw(0, true)
        }

        gameViewModel.level.nextLevel.observe(
            viewLifecycleOwner,
            nextLevelObserver
        )
    }

    private fun removeEnemyObservers() {
        gameViewModel.level.enemies.forEach {
            it.positionChange.removeObservers(viewLifecycleOwner)
            it.attackDamage.removeObservers(viewLifecycleOwner)
        }
    }

    private fun setupEnemyObservers() {
        gameViewModel.level.enemies.forEach {
            it.positionChange.observe(viewLifecycleOwner, enemyObserver)
            it.attackDamage.observe(viewLifecycleOwner, enemyDamageObserver)
        }
    }

    private fun hideAllEnemies() {
        gameViewModel.level.enemies.forEach{
            if (view != null) {
                val enemyView = getGameObjectView(view, it.id)
                enemyView?.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateLevel() {
        removeGameObjects()
        updateStats()
    }

    private fun onEnemyAttack(
        it: EnemyDamageDTO,
        view: View
    ) {
        gameViewModel.onEnemyAttack(it.damage, it.id)
        val charaView = getGameObjectView(view, gameViewModel.chara.id)
        flashRed(charaView)

        binding?.health?.text = String.format(
            resources.getString(
                R.string.health,
                gameViewModel.chara.health.toString()
            )
        )
        val enemyView = getGameObjectView(view, it.id)
        nudge(enemyView, it.id, it.direction)
    }

    private fun onEnemyMove(
        view: View,
        it: EnemyPositionChangeDTO
    ) {
        val enemyView = getGameObjectView(view, it.id)
        val jumpUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.jump)
        enemyView?.startAnimation(jumpUpAnimation)

        gameViewModel.onEnemyPositionChange(it)
    }

    fun interact() {
        gameViewModel.interact()

        val charaView = getGameObjectView(view, gameViewModel.chara.id)
        nudge(charaView, gameViewModel.chara.id, gameViewModel.chara.direction)

        updateLevel()
    }

    fun moveUp() {

        val turn = gameViewModel.turn(Direction.UP)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_back, requireContext().theme))
            return
        }
        gameViewModel.moveUp()

        redraw(charaMoves = true)
    }

    fun moveDown() {
        val turn = gameViewModel.turn(Direction.DOWN)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_front, requireContext().theme))
            return
        }

        gameViewModel.moveDown()

        redraw(charaMoves = true)
    }

    fun moveLeft() {
        val turn = gameViewModel.turn(Direction.LEFT)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_left, requireContext().theme))
            return
        }

        gameViewModel.moveLeft()

        redraw(charaMoves = true)
    }

    fun moveRight() {
        val turn = gameViewModel.turn(Direction.RIGHT)

        if (turn) {
            val chara = view?.findViewById<ImageView>(R.id.character)
            chara?.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.chara_right, requireContext().theme))
            return
        }

        gameViewModel.moveRight()

        redraw(charaMoves = true)
    }

    private fun fadeView() {
        view?.animate()?.alpha(0F)?.setDuration(300L)?.withEndAction{
            view?.animate()?.alpha(1F)?.setDuration(300L)
        }
    }

    private fun redraw(duration: Long = Settings.animDuration, charaMoves: Boolean = false) {
        val background = view?.findViewById<FragmentContainerView>(R.id.background_container)
        val chara = view?.findViewById<ImageView>(R.id.character)


        if (background == null) {
            return
        }
        val charaPosition = gameViewModel.findCoordinate(gameViewModel.chara.id)
        val moveLength = convertDpToPixel(Settings.moveLength)
        val xPosBackground = backgroundOrigPos.x.minus(charaPosition.x*moveLength)
        val yPosBackground = backgroundOrigPos.y.minus(charaPosition.y*moveLength)

        backgroundPos = Coordinates(xPosBackground.toInt(), yPosBackground.toInt())

        if (charaMoves){
            val jumpUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.jump)
            chara?.startAnimation(jumpUpAnimation)
        }
        background.animate().x(xPosBackground).y(yPosBackground).setDuration(duration)

        drawObjects(duration)
    }

    private fun flashRed(gameObjectView: ImageView?) {
        if (gameObjectView == null) {
            return
        }
        val colorAnim = ValueAnimator.ofArgb(
            ResourcesCompat.getColor(
                resources,
                R.color.red_semitransparent,
                null
            )
        )
        colorAnim.addUpdateListener { valueAnimator ->
            gameObjectView.setColorFilter(
                (valueAnimator.animatedValue as Int)
            )
        }
        colorAnim.setDuration(Settings.animDuration)
        colorAnim.repeatMode = REVERSE
        colorAnim.repeatCount = 1
        colorAnim.start()
    }

    private fun nudge(gameObjectView: ImageView?, id: String, direction: Direction) {
        if (gameObjectView == null) {
            return
        }
        var deltaX = 0
        var deltaY = 0

        when (direction) {
            Direction.UP -> deltaY = -1
            Direction.DOWN -> deltaY = 1
            Direction.LEFT -> deltaX = -1
            Direction.RIGHT -> deltaX = 1
        }

        val coords = gameViewModel.findCoordinate(id)
        val (xPos, yPos) = getPositionFromCoordinates(coords)
        val nudgeWidth = convertDpToPixel(Settings.nudgeWidth)
        val newX = xPos + deltaX*nudgeWidth
        val newY = yPos + deltaY*nudgeWidth
        gameObjectView.animate().x(newX).y(newY)
            .setDuration(Settings.animDuration).withEndAction {
                gameObjectView.animate().x(xPos).y(yPos)
                    .setDuration(Settings.animDuration)
            }

    }

    private fun getPositionFromCoordinates(coords: Coordinates): Pair<Float, Float> {
        val moveLength = convertDpToPixel(Settings.moveLength)
        val xPos = coords.x * moveLength + backgroundPos.x + Settings.margin
        val yPos = coords.y * moveLength + backgroundPos.y + Settings.margin
        return Pair(xPos, yPos)
    }

    private fun drawObjects(duration: Long) {
        val fieldCopy = gameViewModel.level.field.clone()
        fieldCopy.forEachIndexed { x, row ->
            row.forEachIndexed { y, levelObjects ->
                val levelObjectList = levelObjects.toMutableList()
                levelObjectList.forEach {
                        if ( it.id != gameViewModel.chara.id){
                            moveObject(it, x, y, duration)
                    }
                }
            }
        }
    }

    private fun moveObject(levelObject: LevelObject, x: Int, y: Int, duration: Long) {
        val gameObjectView = getGameObjectView(view, levelObject.id)
                ?: return

        val (xPos, yPos) = getPositionFromCoordinates(Coordinates(x, y))

        if(levelObject.type == LevelObjectType.COIN || levelObject.type == LevelObjectType.WEAPON) {
            val moveLength = convertDpToPixel(Settings.moveLength)
            if(gameObjectView.x.toInt() <= moveLength && gameObjectView.y.toInt() <= moveLength ){
                gameObjectView.x = xPos
                gameObjectView.y = yPos

            } else {
                gameObjectView.animate().x(xPos).y(yPos).setDuration(duration)
            }
        } else {
            val nudgeWidth = convertDpToPixel(Settings.nudgeWidth)

            if (abs(yPos - gameObjectView.y) > nudgeWidth || abs(xPos - gameObjectView.x) > nudgeWidth){
                gameObjectView.animate().x(xPos).y(yPos).setDuration(duration)
            }
        }

        gameObjectView.visibility = View.VISIBLE
        gameObjectView.bringToFront()

        if (levelObject is BasicEnemy) {
            if (levelObject.health <= 0) {
                gameViewModel.level.field[x][y].remove(levelObject)
                gameObjectView.visibility = View.INVISIBLE
                levelObject.positionChange.removeObserver(enemyObserver)
                return
            }
            val drawableName = when(levelObject.direction) {
                Direction.DOWN -> "${levelObject.skin}_front"
                Direction.UP -> "${levelObject.skin}_back"
                Direction.LEFT -> "${levelObject.skin}_left"
                Direction.RIGHT -> "${levelObject.skin}_right"

            }
            val drawableId = resources.getIdentifier(
                drawableName,
                "drawable",
                requireContext().packageName
            )

            // TODO: catch exceptions
            gameObjectView.setImageDrawable(ResourcesCompat.getDrawable(resources, drawableId, requireContext().theme))

        }
    }

    private fun removeGameObjects() {
        gameViewModel.level.gameObjectIds.forEach{
            hideGameObjectIfRemoved(it)
        }
    }

    private fun hideGameObjectIfRemoved(id: String) {
        if (!gameViewModel.level.field.any { arrayOfLevelObjects -> arrayOfLevelObjects.any { it.any{itemInList -> itemInList.id == id }}}) {
            val gameObjectView = view?.findViewById<ImageView>(resources.getIdentifier(id, "id", requireContext().packageName))
            gameObjectView?.visibility = View.INVISIBLE
        }
    }

    private fun updateStats() {
        binding?.goldCounter?.text = String.format(
            resources.getString((R.string.gold),
            gameViewModel.chara.gold.toString())
        )
        binding?.health?.text = String.format(
            resources.getString((R.string.health),
                gameViewModel.chara.health.toString())
        )
    }

    private fun showWeapon(id: String) {
        for (sword in gameViewModel.level.swords) {
            val i = sword.id
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

    private fun showArmor(id: String) {
        for (armor in gameViewModel.level.armors) {
            val i = armor.id
            val armorId = "gui_$i"
            val armorView = view?.findViewById<View>(
                resources.getIdentifier(
                    armorId,
                    "id",
                    requireContext().packageName
                )
            )
            armorView?.visibility = View.INVISIBLE
        }

        val armorId = "gui_$id"
        val armorView = view?.findViewById<View>(
            resources.getIdentifier(
                armorId,
                "id",
                requireContext().packageName
            )
        )
        armorView?.visibility = View.VISIBLE
    }

    private fun getGameObjectView(view: View?, objectId: String): ImageView? {
        return view?.findViewById(
            resources.getIdentifier(
                objectId,
                "id",
                requireContext().packageName
            )
        )
    }

    private fun convertDpToPixel(dp: Float): Float {
        return dp * (requireContext().resources
            .displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

}

