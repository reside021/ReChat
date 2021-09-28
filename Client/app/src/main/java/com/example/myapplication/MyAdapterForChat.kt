package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView

class MyAdapterForChat(_list: MutableList<Pair<String,String>>) : BaseAdapter() {
    private val list : MutableList<Pair<String, String>> = _list

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context = parent?.context
        val newView : View
        if(convertView == null){
            val layoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            newView = layoutInflater.inflate(R.layout.chat_element, parent, false)
            val nameOfUser = newView.findViewById<TextView>(R.id.userName)
            val idOfUser = newView.findViewById<TextView>(R.id.idUser)
            nameOfUser.text = list[position].second
            idOfUser.text = list[position].first
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