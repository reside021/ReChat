package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.google.android.material.tabs.TabLayout


class FriendsFragment : Fragment() {

    internal interface OnFragmentSendDataListener {
        fun onFriendsLoadView()
    }

    companion object{
        fun newInstance() = FriendsFragment()
    }

    private var fragmentSendDataListener: OnFragmentSendDataListener? = null
    private lateinit var tabLayout : TabLayout
    private lateinit var viewPager2: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentSendDataListener = context as OnFragmentSendDataListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabLayout = requireView().findViewById(R.id.tabsFriends)
        loadFragment(FriendListFragment.newInstance())
        fragmentSendDataListener?.onFriendsLoadView()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val fragment: Fragment
                when (tab?.position) {
                    0 -> {
                        fragment = FriendListFragment()
                        loadFragment(fragment)
                    }
                    1 -> {
                        fragment = UserListFragment()
                        loadFragment(fragment)
                    }
                    2 -> {
                        fragment = FrndListRequestFragment()
                        loadFragment(fragment)
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })
    }

    private fun loadFragment(fragment : Fragment){
         requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.host_fragmentListFriends, fragment)
            .commit()
    }


}