package com.example.myapplication.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.ui.FriendListFragment
import com.example.myapplication.ui.FrndListRequestFragment
import com.example.myapplication.ui.UserListFragment

class MyViewPagerAdapterPeople(fragment: Fragment) : FragmentStateAdapter(fragment) {
    val fragments: MutableList<Fragment> = mutableListOf()
    override fun getItemCount(): Int {
        return 3
    }
    override fun createFragment(position: Int): Fragment {
        val fragment: Fragment
        when (position) {
            1 -> {
                fragment = UserListFragment()
                fragments.add(fragment)
            }
            2 -> {
                fragment = FrndListRequestFragment()
                fragments.add(fragment)
            }
            else ->{
                fragment = FriendListFragment()
                fragments.add(fragment)
            }
        }
        return fragment
    }

}