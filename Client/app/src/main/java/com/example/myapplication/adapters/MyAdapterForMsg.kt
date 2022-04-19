package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.ActivityMain.Companion.sqliteHelper
import com.example.myapplication.R
import com.squareup.picasso.Picasso


class MyAdapterForMsg(context: Context, dataMsg : MutableList<Array<String>>, dialog_id : String, ourTag : String) : BaseAdapter() {
    val context = context
    val dataMsg = dataMsg
    val dialog_id = dialog_id
    val ourTag = ourTag

    override fun getCount(): Int {
        return dataMsg.size
    }

    override fun getItem(position: Int): Any {
        return dataMsg[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {


        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var newView = inflater.inflate(R.layout.clearlayout, null)
        when {
            dataMsg[position][3] == "IMAGE" -> {
                val nameImg = dataMsg[position][1]
                var chatName = dialog_id.replace("#", "%23")
                chatName = chatName.replace("::", "--")
                val urlImg = "http://imagerc.ddns.net:80/userImgMsg/$chatName/$nameImg.jpg"
                var imageInMessage: ImageView
                if(dataMsg[position][0] != ourTag){
                    var nameOfUser = sqliteHelper.getNameInUserChat(dataMsg[position][0])
                    if (nameOfUser.isEmpty()){
                        nameOfUser = context.resources.getString(R.string.user_name)
                    }
                    newView = inflater.inflate(R.layout.message_from_image, null)
                    newView.findViewById<TextView>(R.id.senderName).text = nameOfUser
                    imageInMessage = newView.findViewById(R.id.msgFromImage)
                } else{
                    newView = inflater.inflate(R.layout.message_to_image, null)
                    imageInMessage = newView.findViewById(R.id.msgToImage)
                }
                Picasso.get()
                    .load(urlImg)
                    .placeholder(R.drawable.error_image)
                    .into(imageInMessage)
            }
            dataMsg[position][3] == "TEXT" -> {
                if(dataMsg[position][0] != ourTag){
                    var nameOfUser = sqliteHelper.getNameInUserChat(dataMsg[position][0])
                    if (nameOfUser.isEmpty()){
                        nameOfUser = context.resources.getString(R.string.user_name)
                    }
                    newView = inflater.inflate(R.layout.message_from, null)
                    newView.findViewById<TextView>(R.id.msgFrom).text = dataMsg[position][1]
                    newView.findViewById<TextView>(R.id.senderName).text = nameOfUser
                } else{
                    newView = inflater.inflate(R.layout.message_to, null)
                    newView.findViewById<TextView>(R.id.msgTO).text = dataMsg[position][1]
                }
            }
        }
        return newView
    }
}