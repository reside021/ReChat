package com.example.myapplication.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myapplication.*
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.adapters.MyAdapterForUsers
import com.example.myapplication.dataClasses.ConfirmAddFriend
import com.example.myapplication.dataClasses.FindPeople
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserListFragment : Fragment(),
    SharedPreferences.OnSharedPreferenceChangeListener{
    internal interface OnFragmentSendDataListener {
        fun onUserListLoadView()
    }
    companion object{
        fun newInstance() = UserListFragment()
    }
    private var fragmentSendDataListener: OnFragmentSendDataListener? = null
    private lateinit var searchView: SearchView
    private lateinit var sp : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_users, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentSendDataListener = context as OnFragmentSendDataListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sp = requireActivity().getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        sp.registerOnSharedPreferenceChangeListener(this)
        searchView = view.findViewById(R.id.searchField)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query.isNullOrEmpty()) return false
                if(query.length < 8) return false
                val tagUser = query.substring(0,8)
                val dataUser = FindPeople("FRND::", "FIND::", tagUser)
                val msg = Json.encodeToString(dataUser)
                if(!webSocketClient.connection.isClosed){
                    webSocketClient.send(msg)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        val listViewUsers = requireView().findViewById<ListView>(R.id.listViewUser)
        listViewUsers.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(activity, FriendsProfile::class.java);
            val tagUser = view.findViewById<TextView>(R.id.idUser)
            val nameOfUser = view.findViewById<TextView>(R.id.userName)
            intent.putExtra("idTag", tagUser.text)
            intent.putExtra("nameOfUser", nameOfUser.text)
            startActivity(intent)
        }
    }
    fun setUserData(myAdapterForUsers: MyAdapterForUsers){
        val listViewUsers = requireView().findViewById<ListView>(R.id.listViewUser)
        listViewUsers.adapter = myAdapterForUsers
    }

    override fun onResume() {
        super.onResume()
        fragmentSendDataListener?.onUserListLoadView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(this.isVisible){
            if(key.equals("changeStatusFind")){
                val intent = Intent(activity, FriendsProfile::class.java);
                val tagUser = sharedPreferences?.getString("tagUserFind","")
                val nameOfUser = sharedPreferences?.getString("nameUserFind","")
                intent.putExtra("idTag", tagUser)
                intent.putExtra("nameOfUser", nameOfUser)
                startActivity(intent)
            }
        }
    }
}