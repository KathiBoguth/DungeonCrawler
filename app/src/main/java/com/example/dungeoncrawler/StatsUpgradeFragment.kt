package com.example.dungeoncrawler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dungeoncrawler.databinding.FragmentStatsUpgradeBinding

class StatsUpgradeFragment: Fragment() {

    val statsViewModel: StatsViewModel by activityViewModels()

    private var binding: FragmentStatsUpgradeBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentStatsUpgradeBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            upgradeStatsFragment = this@StatsUpgradeFragment
        }
    }

    fun returnToMain() {
        this.findNavController().navigate(R.id.action_statsUpgradeFragment_to_mainFragment)
    }
}