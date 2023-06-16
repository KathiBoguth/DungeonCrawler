package com.example.dungeoncrawler

import android.animation.ValueAnimator
import android.animation.ValueAnimator.REVERSE
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.res.ResourcesCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dungeoncrawler.databinding.FragmentGameViewBinding
import com.example.dungeoncrawler.entity.Coordinates
import com.example.dungeoncrawler.entity.Direction
import com.example.dungeoncrawler.entity.LevelObject
import com.example.dungeoncrawler.entity.LevelObjectType
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.enemy.BasicEnemy
import com.example.dungeoncrawler.entity.enemy.EnemyDamageDTO
import com.example.dungeoncrawler.entity.enemy.LevelObjectPositionChangeDTO
import com.example.dungeoncrawler.entity.enemy.Ogre
import com.example.dungeoncrawler.entity.weapon.Arrow
import com.example.dungeoncrawler.entity.weapon.Weapon
import com.example.dungeoncrawler.viewmodel.ComposableGameViewModel
import com.example.dungeoncrawler.viewmodel.GameViewModel
import com.example.dungeoncrawler.viewmodel.MenuViewModel
import com.example.dungeoncrawler.viewmodel.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.abs


class GameView : Fragment() {

    private var backgroundPos = Coordinates(-1, -1)
    private var backgroundOrigPos = Coordinates(-1, -1)
    val gameViewModel: GameViewModel by activityViewModels()
    val composableGameViewModel: ComposableGameViewModel by viewModels()

    private var binding: FragmentGameViewBinding? = null

    private val scope = CoroutineScope(Dispatchers.IO)
    private var handler = Handler(Looper.getMainLooper())
    private val enemyAttackFlowCollectionJobList = mutableListOf<Job>()
    private val enemyPositionChangeFlowCollectionJobList = mutableListOf<Job>()
    private lateinit var charaWeaponObserver: Observer<Weapon>
    private lateinit var charaArmorObserver: Observer<Armor>
    private lateinit var attackedEntityAnimationObserver: Observer<String>
    private lateinit var updateLevelObserver: Observer<Boolean>
    private lateinit var nextLevelObserver: Observer<Int>

    private lateinit var mediaPlayerDungeon: MediaPlayer
    private lateinit var mediaPlayerBoss: MediaPlayer

    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            if (backgroundPos.x == -1) {
                val character = view?.findViewById<ImageView>(R.id.character)
                backgroundPos =
                    Coordinates(character?.x?.toInt() ?: -1, character?.y?.toInt() ?: -1)
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
//        val fragmentBinding = FragmentGameViewBinding.inflate(inflater, container, false)
//        binding = fragmentBinding
        //gameViewModel.reset()

        return ComposeView(requireContext()).apply {

            setContent {
                GameScreen(
                    gameViewModel = composableGameViewModel
                )
            }
        }
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

        mediaPlayerDungeon = MediaPlayer.create(requireContext(), R.raw.dungeon)
        mediaPlayerDungeon.isLooping = true
        mediaPlayerDungeon.start()
        mediaPlayerBoss = MediaPlayer.create(requireContext(), R.raw.boss)
        mediaPlayerBoss.isLooping = true
    }

    override fun onPause() {
        super.onPause()
        mediaPlayerDungeon.pause()
        mediaPlayerBoss.pause()
    }

    override fun onResume() {
        super.onResume()
//        if (gameViewModel.level.levelCount >= Settings.enemiesPerLevel.size){
//            mediaPlayerBoss.start()
//        } else {
//            mediaPlayerDungeon.start()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerDungeon.stop()
        mediaPlayerBoss.stop()
        mediaPlayerDungeon.release()
        mediaPlayerBoss.release()
    }

    private fun setupObserver(view: View) {
        setupEnemyObservers(view)

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

        lifecycleScope.launch {
            gameViewModel.endGame.collect { victory ->
                if (victory == null) {
                    return@collect
                }
                removeEnemyObservers()
                hideAllEnemies()
                saveGold()
                if (findNavController().currentDestination?.id == R.id.gameView) {
                    if (victory) {
                        findNavController().navigate(R.id.action_gameView_to_victoryView)
                    } else {
                        findNavController().navigate(R.id.action_gameView_to_gameOverView)
                    }
                    this.cancel()
                }

            }
        }

        updateLevelObserver = Observer<Boolean> {
            updateLevel()
        }

        gameViewModel.updateLevel.observe(
            viewLifecycleOwner,
            updateLevelObserver
        )

//        nextLevelObserver = Observer<Int> {
//            removeEnemyObservers()
//            hideAllEnemies()
//            fadeView()
//            backgroundPos = Coordinates(-1,-1)
//
//            gameViewModel.level.initLevel()
//
//            setupEnemyObservers(view)
//            binding?.level?.text = String.format(
//                resources.getString(
//                    (R.string.level),
//                    gameViewModel.level.levelCount.toString()
//                )
//            )
//            redraw(0, true)
//
//            if (gameViewModel.level.levelCount >= Settings.enemiesPerLevel.size){
//                mediaPlayerDungeon.pause()
//                mediaPlayerBoss.start()
//            }
//        }
//
//        gameViewModel.level.nextLevel.observe(
//            viewLifecycleOwner,
//            nextLevelObserver
//        )
    }

    private fun removeEnemyObservers() {

        enemyAttackFlowCollectionJobList.forEach {
            it.cancel()
        }
        enemyPositionChangeFlowCollectionJobList.forEach {
            it.cancel()
        }
        enemyAttackFlowCollectionJobList.clear()
        enemyPositionChangeFlowCollectionJobList.clear()
    }

    private fun setupEnemyObservers(view: View) {
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

    private fun hideAllEnemies() {
//        gameViewModel.level.movableEntitiesList.filterIsInstance<BasicEnemy>().forEach{
//            if (view != null) {
//                val enemyView = getGameObjectView(view, it.id)
//                requireActivity().runOnUiThread {
//                    enemyView?.visibility = View.GONE
//                }
//            }
//        }
    }

    private fun saveGold() {
        scope.launch {
            requireContext().dataStore.edit { preferences ->
                val oldGold = preferences[intPreferencesKey(MenuViewModel.GOLD_KEY)]
                val newGold = oldGold?.plus(gameViewModel.chara.gold) ?: gameViewModel.chara.gold
                preferences[intPreferencesKey(MenuViewModel.GOLD_KEY)] = newGold
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
                gameViewModel.chara.health
            )
        )
        val enemyView = getGameObjectView(view, it.id)
        //nudge(enemyView, it.id, it.direction)
    }

    private fun onEnemyMove(
        view: View,
        it: LevelObjectPositionChangeDTO
    ) {
        val enemyView = getGameObjectView(view, it.id)
        val jumpUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.jump)
        enemyView?.startAnimation(jumpUpAnimation)

        gameViewModel.onEnemyPositionChange(it)
    }


    private fun fadeView() {
        requireActivity().runOnUiThread {
            view?.animate()?.alpha(0F)?.setDuration(300L)?.withEndAction {
                view?.animate()?.alpha(1F)?.setDuration(300L)
            }
        }

    }

    private fun redraw(duration: Long = Settings.animDuration, charaMoves: Boolean = false) {
        val background = view?.findViewById<FragmentContainerView>(R.id.background_container)
        val chara = view?.findViewById<ImageView>(R.id.character)


        if (background == null) {
            return
        }
        // val charaPosition = gameViewModel.findCoordinate(gameViewModel.chara.id)
        val moveLength = convertDpToPixel(Settings.moveLength)
        // val xPosBackground = backgroundOrigPos.x.minus(charaPosition.x*moveLength)
        // val yPosBackground = backgroundOrigPos.y.minus(charaPosition.y*moveLength)

        // backgroundPos = Coordinates(xPosBackground.toInt(), yPosBackground.toInt())

        if (charaMoves) {
            val jumpUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.jump)
            chara?.startAnimation(jumpUpAnimation)
        }
        // requireActivity().runOnUiThread {
        //    background.animate().x(xPosBackground).y(yPosBackground).setDuration(duration)
        // }

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
        requireActivity().runOnUiThread {
            colorAnim.start()
        }
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

//        val coords = gameViewModel.findCoordinate(id)
//        val (xPos, yPos) = getPositionFromCoordinates(coords)
//        val nudgeWidth = convertDpToPixel(Settings.nudgeWidth)
//        val newX = xPos + deltaX*nudgeWidth
//        val newY = yPos + deltaY*nudgeWidth
//        requireActivity().runOnUiThread {
//            gameObjectView.animate().x(newX).y(newY)
//                .setDuration(Settings.animDuration).withEndAction {
//                    gameObjectView.animate().x(xPos).y(yPos)
//                        .setDuration(Settings.animDuration)
//                }
//        }
    }

    private fun getPositionFromCoordinates(
        coords: Coordinates,
        isOgre: Boolean = false
    ): Pair<Float, Float> {
        val moveLength = convertDpToPixel(Settings.moveLength)
        var xPos = coords.x * moveLength + backgroundPos.x + Settings.margin
        var yPos = coords.y * moveLength + backgroundPos.y + Settings.margin
        if (isOgre) {
            xPos -= convertDpToPixel(70f)
            yPos -= convertDpToPixel(70f)
        }
        return Pair(xPos, yPos)
    }

    private fun drawObjects(duration: Long) {
//        val fieldCopy = gameViewModel.level.field.clone()
//        fieldCopy.forEachIndexed { x, row ->
//            row.forEachIndexed { y, levelObjects ->
//                val levelObjectList = levelObjects.toMutableList()
//                levelObjectList.forEach {
//                        if ( it.id != gameViewModel.chara.id){
//                            moveObject(it, x, y, duration)
//                    }
//                }
//            }
//        }
//        gameViewModel.level.movableEntitiesList.filter { it.position != Coordinates(-1, -1) }.forEach {
//            if ( it.id != gameViewModel.chara.id){
//                moveObject(it, it.position.x, it.position.y, duration)
//            }
//        }
    }

    private fun moveObject(levelObject: LevelObject, x: Int, y: Int, duration: Long) {
        val gameObjectView = getGameObjectView(view, levelObject.id)
            ?: return

        val (xPos, yPos) = getPositionFromCoordinates(Coordinates(x, y), (levelObject is Ogre))

        if (levelObject.type == LevelObjectType.COIN || levelObject.type == LevelObjectType.WEAPON) {
            val moveLength = convertDpToPixel(Settings.moveLength)
            if (gameObjectView.x.toInt() <= moveLength && gameObjectView.y.toInt() <= moveLength) {
                gameObjectView.x = xPos
                gameObjectView.y = yPos

            } else {
                requireActivity().runOnUiThread {
                    gameObjectView.animate().x(xPos).y(yPos).setDuration(duration)
                }
            }
        } else {
            val nudgeWidth = convertDpToPixel(Settings.nudgeWidth)

            if (abs(yPos - gameObjectView.y) > nudgeWidth || abs(xPos - gameObjectView.x) > nudgeWidth) {
                if (levelObject.type == LevelObjectType.ARROW) {
                    requireActivity().runOnUiThread {
                        gameObjectView.animate().x(xPos).y(yPos)
                            .setDuration((levelObject as Arrow).speed.toLong() - 10)
                    }
                } else {
                    requireActivity().runOnUiThread {
                        gameObjectView.animate().x(xPos).y(yPos).setDuration(duration)
                    }
                }
            }
        }

        gameObjectView.visibility = View.VISIBLE
        gameObjectView.bringToFront()

        if (levelObject is BasicEnemy) {
            if (levelObject.health <= 0) {
                gameObjectView.visibility = View.GONE
                return
            }
            val drawableName = if (levelObject is Ogre && levelObject.attackCharged) {
                "${levelObject.skin}_attack"
            } else {
                when (levelObject.direction) {
                    Direction.DOWN -> "${levelObject.skin}_front"
                    Direction.UP -> "${levelObject.skin}_back"
                    Direction.LEFT -> "${levelObject.skin}_left"
                    Direction.RIGHT -> "${levelObject.skin}_right"

                }
            }
            val drawableId = resources.getIdentifier(
                drawableName,
                "drawable",
                requireContext().packageName
            )

            removeGameObjects()

            // TODO: catch exceptions
            gameObjectView.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    drawableId,
                    requireContext().theme
                )
            )

        }
    }

    private fun removeGameObjects() {
//        gameViewModel.level.gameObjectIds.forEach{
//            hideGameObjectIfRemoved(it)
//        }
    }

    private fun hideGameObjectIfRemoved(id: String) {
//        val notOnField = !gameViewModel.level.field.any { arrayOfLevelObjects -> arrayOfLevelObjects.any { it.any{itemInList -> itemInList.id == id }}}
//        val movableEntity = gameViewModel.level.movableEntitiesList.firstOrNull{it.id == id}
//        if (movableEntity != null){
//            if(movableEntity.position.x == -1 || movableEntity.position.y == -1) {
//                val gameObjectView = view?.findViewById<ImageView>(resources.getIdentifier(id, "id", requireContext().packageName))
//                gameObjectView?.visibility = View.GONE
//                if (id.contains(Level.ARROW)) {
//                    val charaView = getGameObjectView(view, gameViewModel.chara.id)
//                    gameObjectView?.x = charaView?.x ?: 0F
//                    gameObjectView?.y = charaView?.y ?: 0F
//                }
//            }
//        } else {
//            if (notOnField) {
//                val gameObjectView = view?.findViewById<ImageView>(resources.getIdentifier(id, "id", requireContext().packageName))
//                gameObjectView?.visibility = View.GONE
//            }
//        }

    }

    private fun updateStats() {
        binding?.goldCounter?.text = String.format(
            resources.getString(
                (R.string.gold),
                gameViewModel.chara.gold
            )
        )
        binding?.health?.text = String.format(
            resources.getString(
                (R.string.health),
                gameViewModel.chara.health
            )
        )
    }

    private fun showWeapon(id: String) {
//        for (sword in gameViewModel.level.weapons) {
//            val i = sword.id
//            val weaponId = "gui_$i"
//            val weaponView = view?.findViewById<View>(
//                resources.getIdentifier(
//                    weaponId,
//                    "id",
//                    requireContext().packageName
//                )
//            )
//            weaponView?.visibility = View.GONE
//        }

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
//        for (armor in gameViewModel.level.armors) {
//            val i = armor.id
//            val armorId = "gui_$i"
//            val armorView = view?.findViewById<View>(
//                resources.getIdentifier(
//                    armorId,
//                    "id",
//                    requireContext().packageName
//                )
//            )
//            armorView?.visibility = View.GONE
//        }

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

