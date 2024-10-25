package com.willismarques.projetowhatsappfirebase.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.willismarques.projetowhatsappfirebase.fragments.ContatosFragment
import com.willismarques.projetowhatsappfirebase.fragments.ConversasFragment


class ViewPagerAdapter(
   private val abas: List<String>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int {
        return abas.size
    }

    override fun createFragment(position: Int): Fragment {
        when(position){
            1 -> return ContatosFragment()
        }
        return ConversasFragment()
    }

}