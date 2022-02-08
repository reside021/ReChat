package com.example.myapplication.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.FriendsProfile
import com.example.myapplication.adapters.MyAdapterForFriends
import com.example.myapplication.R

class FriendListFragment : Fragment() {
    internal interface OnFragmentSendDataListener {
        fun onFriendsListLoadView()
    }

    companion object{
        fun newInstance() = FriendListFragment()
    }

    private var fragmentSendDataListener: OnFragmentSendDataListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_friends, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentSendDataListener = context as OnFragmentSendDataListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSendDataListener?.onFriendsListLoadView()
    }

    fun setUserData(myAdapterForFriends: MyAdapterForFriends){
        val listViewFriends = requireView().findViewById<ListView>(R.id.listViewFriends)
        listViewFriends.adapter = myAdapterForFriends
        listViewFriends.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(activity, FriendsProfile::class.java);
            val tagUser = view.findViewById<TextView>(R.id.idUser)
            val nameOfUser = view.findViewById<TextView>(R.id.userName)
            intent.putExtra("idTag", tagUser.text)
            // проверка на наличие в друзьях sql
            intent.putExtra("nameOfUser", nameOfUser.text)
            startActivity(intent)
        }
    }

}