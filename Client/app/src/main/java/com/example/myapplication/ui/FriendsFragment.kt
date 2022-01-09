package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.example.myapplication.MyAdapterForFriends
import com.example.myapplication.R


class FriendsFragment : Fragment() {

    internal interface OnFragmentSendDataListener {
        fun onFriendsLoadView()
    }

    companion object{
        fun newInstance() = FriendsFragment()
    }

    private var fragmentSendDataListener: OnFragmentSendDataListener? = null

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
        fragmentSendDataListener?.onFriendsLoadView()
    }

    fun setUserData(myAdapterForFriends: MyAdapterForFriends){
        val listViewFriends = requireView().findViewById<ListView>(R.id.listViewFriends)
        listViewFriends.adapter = myAdapterForFriends
    }
}