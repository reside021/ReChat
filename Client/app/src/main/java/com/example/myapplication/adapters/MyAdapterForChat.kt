package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.ActivityMain
import com.example.myapplication.ActivityMain.Companion.sqliteHelper
import com.example.myapplication.R
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class MyAdapterForChat() : BaseAdapter() {
    private val list : MutableList<Pair<String, String>> = sqliteHelper.getAllUsersChat()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context = parent?.context
        val newView : View
        if(convertView == null){
            val layoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = layoutInflater.inflate(R.layout.chat_element, parent, false)
            val nameOfUser = newView.findViewById<TextView>(R.id.userName)
            val idOfUser = newView.findViewById<TextView>(R.id.idUser)
            val countNewMsg = newView.findViewById<TextView>(R.id.countNewMsg)
            nameOfUser.text = list[position].second
            idOfUser.text = list[position].first
            val userTag = idOfUser.text
            val imageOfUser = newView.findViewById<ImageView>(R.id.avatarUser)
            val urlAvatar = "http://imagerc.ddns.net:80/avatar/avatarImg/$userTag.jpg"
            Picasso.get()
                    .load(urlAvatar)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .placeholder(R.drawable.user_profile_photo)
                    .into(imageOfUser)
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