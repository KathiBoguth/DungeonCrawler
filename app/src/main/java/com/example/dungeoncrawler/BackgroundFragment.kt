package com.example.dungeoncrawler

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.dungeoncrawler.entity.GroundType
import com.example.dungeoncrawler.ground.GroundFragment
import kotlin.random.Random

class BackgroundFragment : Fragment() {

    private var random: Random = Random(System.currentTimeMillis())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backgroundLayout = getBackgroundLayout()
        if (savedInstanceState == null) {
            backgroundLayout.forEachIndexed {x, column ->
                column.forEachIndexed { y, ground ->
                    parentFragmentManager.commit {
                        setReorderingAllowed(true)
                        val bundle = Bundle()
                        val xPos = convertDpToPixel(x*Settings.moveLength)
                        val yPos = convertDpToPixel(y*Settings.moveLength)
                        bundle.putFloat("xPos", xPos)
                        bundle.putFloat("yPos", yPos)
                        bundle.putSerializable("type", ground)
                        add<GroundFragment>(R.id.background_container, args = bundle)
                    }
                }
            }
        }

    }

    private fun getBackgroundLayout(): List<List<GroundType>> {
        val layout = MutableList(Settings.fieldSize - 2) {
            MutableList(2) { GroundType.STONE }
        }
        layout.forEach {
            val newFields = MutableList(Settings.fieldSize - 2) {randomGroundType()}
            it.addAll(1, newFields)
        }
        layout.add(0, MutableList(Settings.fieldSize) { GroundType.STONE })
        layout.add( MutableList(Settings.fieldSize) { GroundType.STONE })
        return layout
    }

    private fun randomGroundType() : GroundType {
       return when( random.nextInt(4) ) {
           0 -> GroundType.GROUND1
           1 -> GroundType.GROUND2
           2 -> GroundType.PEBBLES
           3 -> GroundType.WATER
           else -> GroundType.GROUND1
       }
    }

    private fun convertDpToPixel(dp: Float): Float {
        return dp * (requireContext().resources
            .displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}