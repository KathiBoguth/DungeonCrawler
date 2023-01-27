package com.example.dungeoncrawler.ground

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.example.dungeoncrawler.R
import com.example.dungeoncrawler.entity.GroundType

class GroundFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ground, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = requireArguments()
        val xPos = bundle.getFloat("xPos")
        val yPos = bundle.getFloat("yPos")
        val groundId = when (bundle.getSerializable("type") as GroundType) {
            GroundType.GROUND1 -> R.drawable.ground
            GroundType.GROUND2 -> R.drawable.ground2
            GroundType.PEBBLES -> R.drawable.pebbles
            GroundType.WATER -> R.drawable.water
            GroundType.STONE -> R.drawable.stone_wall
        }
        view.x = xPos
        view.y = yPos
        view.findViewById<ImageView>(R.id.ground)
            .setImageDrawable(ResourcesCompat.getDrawable(resources, groundId, requireContext().theme))
    }
}