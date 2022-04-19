package com.example.myapplication.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.myapplication.ActivityMain.Companion.sqliteHelper
import com.example.myapplication.R
import com.squareup.picasso.Picasso

class MyAdapterForCrtDlg(_ourTag : String) : BaseAdapter(), Filterable {
    private val ourTag = _ourTag
    private val data = sqliteHelper.getAllFriends(ourTag).toList()
    private var listFiltered : List<Pair<String, String>> = data
    private var list : List<Pair<String, String>> = data
    private var listChecked : MutableList<String> = mutableListOf()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.d("__qwe__", "myadapterforchat")
        val context = parent?.context!!
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView : View = layoutInflater.inflate(R.layout.crt_dlg_element, parent, false)
        val userName = newView.findViewById<TextView>(R.id.userName)
        userName.text = listFiltered[position].second
        val imageOfUser = newView.findViewById<ImageView>(R.id.avatarUser)
        val tagUser = listFiltered[position].first
        val idOfUser = newView.findViewById<TextView>(R.id.idUser)
        idOfUser.text = tagUser
        val urlAvatar = "http://imagerc.ddns.net:80/avatar/avatarImg/$tagUser.jpg"
        Picasso.get()
            .load(urlAvatar)
            .placeholder(R.drawable.user_profile_photo)
            .into(imageOfUser)
        if(listChecked.contains(tagUser)){
            newView.findViewById<CheckBox>(R.id.checkedForDlg).isChecked = true
        }
        return newView
    }

    override fun getItem(position: Int): Any {
        return listFiltered[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return  listFiltered.size
    }

    override fun getFilter(): Filter {
        return object: Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
                val filterResults = FilterResults()
                val originalData = list
                val filteredList = mutableListOf<Pair<String, String>>()
                for(pair in originalData){
                    if (pair.second.contains(charString, ignoreCase = true))
                    {
                        filteredList.add(pair)
                    }
                }
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                listFiltered = results?.values as List<Pair<String, String>>
                notifyDataSetChanged()
            }
        }
    }
    fun updateListChecked(_listChecked : MutableList<String>){
        listChecked = _listChecked
    }

}