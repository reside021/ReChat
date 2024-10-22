package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.myapplication.ActivityMain
import com.example.myapplication.ActivityMain.Companion.sqliteHelper
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.R
import com.example.myapplication.dataClasses.NewUserDLGTable
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MyAdapterForUsers(queryImg : String) : BaseAdapter() {
    private val queryImg = queryImg
    private val list : List<Pair<String, String>> = sqliteHelper.getAllUsersOnline().toList()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context = parent?.context
        val sp = context!!.getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        val ourTag : String = sp.getString("tagUser", "NONE")!!
        val newView : View
        if(convertView == null){
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = layoutInflater.inflate(R.layout.friends_element, parent, false)
            val nameOfUser = newView.findViewById<TextView>(R.id.userName)
            nameOfUser.text = list[position].second
            val addChat = newView.findViewById<Button>(R.id.addChatBtn)
            val imageOfUser = newView.findViewById<ImageView>(R.id.avatarUser)
            val tagUser = list[position].first
            val idOfUser = newView.findViewById<TextView>(R.id.idUser)
            idOfUser.text = tagUser
            val urlAvatar = "http://imagerc.ddns.net:80/avatar/avatarImg/$tagUser.jpg?time=$queryImg"
            Picasso.get()
                .load(urlAvatar)
                .placeholder(R.drawable.user_profile_photo)
                .into(imageOfUser)
            addChat.setOnClickListener {
                val tagUser = list[position].first
                if(sqliteHelper.checkUserInChat(tagUser)) {
                    Toast.makeText(context, "C данным пользователем уже существует чат",
                        Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if(webSocketClient.connection.isClosed) {
                    Toast.makeText(context, "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if(!sqliteHelper.checkExistChatWithUser(ourTag, tagUser)){
                    val newUser = NewUserDLGTable("NEWUSERDLG::", mutableListOf(tagUser), "")
                    val msg = Json.encodeToString(newUser)
                    webSocketClient.send(msg)
                }
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