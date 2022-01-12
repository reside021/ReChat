package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.java_websocket.client.WebSocketClient

class MyAdapterForFriends() : BaseAdapter() {
    private val sqliteHelper : SqliteHelper = MainActivity.sqliteHelper
    private val list : List<Pair<String, String>> = sqliteHelper.getAllUsersOnline().toList()
    private val webSocketClient = MainActivity.webSocketClient
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context = parent?.context
        val sp = context!!.getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        val ourTag : String = sp.getString("tagUser", "NONE")!!
        val newView : View
        if(convertView == null){
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = layoutInflater.inflate(R.layout.friends_element, parent, false)
            val textView2 = newView.findViewById<TextView>(R.id.userName)
            textView2.text = list[position].second
            val addFriendsBtn = newView.findViewById<Button>(R.id.addFriendBtn)
            val addChat = newView.findViewById<Button>(R.id.addChatBtn)
            val imageOfUser = newView.findViewById<ImageView>(R.id.avatarUser)
            val urlAvatar = "http://imagerc.ddns.net:80/avatarImg/$ourTag.jpg"
            Picasso.get()
                    .load(urlAvatar)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .placeholder(R.drawable.user_profile_photo)
                    .into(imageOfUser)
            addFriendsBtn.setOnClickListener {
                Toast.makeText(context, "gg",
                        Toast.LENGTH_SHORT).show()
            }
            addChat.setOnClickListener {
                val tagUser = list[position].first
                if(sqliteHelper.checkUserInChat(tagUser)) {
                    Toast.makeText(context, "C данным пользователем уже существует чат",
                            Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if(webSocketClient.connection.readyState.ordinal == 0) {
                    Toast.makeText(context, "Отсутствует подключение к серверу",
                            Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                sqliteHelper.addUserInChat(list[position])
                if(!sqliteHelper.checkExistChatWithUser(ourTag, tagUser)){
                    val newUser = NewUserDLGTable("NEWUSERDLG::", tagUser)
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
    @Serializable
    data class NewUserDLGTable(
            val type : String,
            val tagUser : String
    )
}