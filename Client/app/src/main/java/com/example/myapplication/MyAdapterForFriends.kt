package com.example.myapplication

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MyAdapterForFriends() : BaseAdapter() {
    private val sqliteHelper : SqliteHelper = MainActivity.sqliteHelper
    private val list : List<Pair<String, String>> = sqliteHelper.getAllUsersOnline().toList()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context = parent?.context
        val newView : View
        if(convertView == null){
            val layoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = layoutInflater.inflate(R.layout.friends_element, parent, false)
            val textView2 = newView.findViewById<TextView>(R.id.userName)
            textView2.text = list[position].second
            val addFriendsBtn = newView.findViewById<Button>(R.id.addFriendBtn)
            val addChat = newView.findViewById<Button>(R.id.addChatBtn)
            addFriendsBtn.setOnClickListener {
                Toast.makeText(context, "Пользователь добавлен в список друзей",
                        Toast.LENGTH_SHORT).show()
            }
            addChat.setOnClickListener {
                Toast.makeText(context, "С пользователем создан чат",
                        Toast.LENGTH_SHORT).show()
                sqliteHelper.addUserInChat(list[position])
            }
            return newView
        }
        return convertView

    }


    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return  list.size
    }
}