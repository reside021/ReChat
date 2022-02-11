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
import com.example.myapplication.dataClasses.ConfirmAddFriend
import com.example.myapplication.dataClasses.DeleteFriend
import com.example.myapplication.dataClasses.NewUserDLGTable
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MyAdapterForRequest(_ourTag : String): BaseAdapter() {
    private val ourTag = _ourTag
    private val list : List<Pair<String, String>> = sqliteHelper.getAllFrndRequest(ourTag).toList()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context = parent?.context!!
        val newView : View
        if(convertView == null){
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = layoutInflater.inflate(R.layout.request_friends_element, parent, false)
            val nameOfUser = newView.findViewById<TextView>(R.id.userName)
            nameOfUser.text = list[position].second
            val imageOfUser = newView.findViewById<ImageView>(R.id.avatarUser)
            val tagUser = list[position].first
            val idOfUser = newView.findViewById<TextView>(R.id.idUser)
            idOfUser.text = tagUser
            val urlAvatar = "http://imagerc.ddns.net:80/avatar/avatarImg/$tagUser.jpg"
            Picasso.get()
                .load(urlAvatar)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .placeholder(R.drawable.user_profile_photo)
                .into(imageOfUser)

            val addFriendBtn = newView.findViewById<Button>(R.id.addFriendBtn)
            val deleteFriendBtn = newView.findViewById<Button>(R.id.deleteFriendBtn)
            addFriendBtn.setOnClickListener {
                if(webSocketClient.connection.isClosed){
                    Toast.makeText(
                        context, "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT
                    ).show()
                } else{
                    val dataUser = ConfirmAddFriend("FRND::", "CNFRMADD::", idOfUser.text.toString())
                    val msg = Json.encodeToString(dataUser)
                    webSocketClient.send(msg)
                }
            }
            deleteFriendBtn.setOnClickListener {
                if(webSocketClient.connection.isClosed){
                    Toast.makeText(
                        context, "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT
                    ).show()
                } else{
                    val dataUser =  DeleteFriend("FRND::", "DELETE::", idOfUser.text.toString(), "DELFROMREQ")
                    val msg = Json.encodeToString(dataUser)
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