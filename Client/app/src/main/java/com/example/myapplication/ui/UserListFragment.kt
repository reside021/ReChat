package com.example.myapplication.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myapplication.*
import com.example.myapplication.adapters.MyAdapterForUsers

class UserListFragment : Fragment() {
    internal interface OnFragmentSendDataListener {
        fun onUserListLoadView()
    }
    companion object{
        fun newInstance() = UserListFragment()
    }
    private var fragmentSendDataListener: OnFragmentSendDataListener? = null
    private lateinit var searchView: SearchView

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
        fragmentSendDataListener?.onUserListLoadView()
        searchView = view.findViewById(R.id.searchField)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(activity,
                    query,
                    Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
    }
    fun setUserData(myAdapterForUsers: MyAdapterForUsers){
        val listViewUsers = requireView().findViewById<ListView>(R.id.listViewUser)
        listViewUsers.adapter = myAdapterForUsers
        listViewUsers.setOnItemClickListener { parent, view, position, id ->
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