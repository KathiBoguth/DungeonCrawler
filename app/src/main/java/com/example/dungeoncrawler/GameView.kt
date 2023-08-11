package com.example.dungeoncrawler

import android.media.MediaPlayer
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.dungeoncrawler.databinding.FragmentGameViewBinding
import com.example.dungeoncrawler.entity.armor.Armor
import com.example.dungeoncrawler.entity.enemy.LevelObjectPositionChangeDTO
import com.example.dungeoncrawler.entity.weapon.Weapon
import com.example.dungeoncrawler.screen.gamescreen.GameScreen
import com.example.dungeoncrawler.viewmodel.ComposableGameViewModel
import com.example.dungeoncrawler.viewmodel.GameViewModel

class GameView : Fragment() {

    val gameViewModel: GameViewModel by activityViewModels()
    val composableGameViewModel: ComposableGameViewModel by viewModels()

    private var binding: FragmentGameViewBinding? = null

    private lateinit var charaWeaponObserver: Observer<Weapon>
    private lateinit var charaArmorObserver: Observer<Armor>
    private lateinit var updateLevelObserver: Observer<Boolean>

    private lateinit var mediaPlayerDungeon: MediaPlayer
    private lateinit var mediaPlayerBoss: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {

            setContent {
                GameScreen(
                    gameViewModel = composableGameViewModel,
                    onNavigate = { dest -> findNavController().navigate(dest) },
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
        //setupEnemyObservers(view)

        updateLevelObserver = Observer<Boolean> {
            updateLevel()
        }

        gameViewModel.updateLevel.observe(
            viewLifecycleOwner,
            updateLevelObserver
        )
    }

    private fun updateLevel() {
        updateStats()
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

//            val drawableName = if (levelObject is Ogre && levelObject.attackCharged) {
//                "${levelObject.skin}_attack"
//




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

