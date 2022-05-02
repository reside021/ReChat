package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.ActivityMain.Companion.sqliteHelper
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.adapters.MyAdapterForCrtDlg
import com.example.myapplication.dataClasses.NewUserDLGTable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

class DialogCreating : AppCompatActivity(){
    private lateinit var sp : SharedPreferences
    private lateinit var tagUser : String
    private lateinit var listUserForDlg : MutableList<String>
    private lateinit var myAdapterForCrtDlg : MyAdapterForCrtDlg
    private lateinit var editTextNameChat : EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_dialog_layout)
        sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        tagUser = sp.getString("tagUser", "")!!
        supportActionBar?.apply {
            title = resources.getString(R.string.create_a_dialog)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        val dataForAdapter = sqliteHelper.getAllFriends(tagUser).toList()
        myAdapterForCrtDlg = MyAdapterForCrtDlg(dataForAdapter)
        listUserForDlg = mutableListOf()
        val listViewCreateDlg = findViewById<ListView>(R.id.listViewCreateDlg)
        listViewCreateDlg.adapter = myAdapterForCrtDlg
        listViewCreateDlg.onItemClickListener = itemListClick
        findViewById<Button>(R.id.createDlg).setOnClickListener(buttonCreateDlg)
        editTextNameChat = findViewById(R.id.nameNewChat)
    }

    private val buttonCreateDlg = View.OnClickListener {
        if (listUserForDlg.size == 0) return@OnClickListener
        if (listUserForDlg.size > 1){
            if (editTextNameChat.text.toString().isBlank()) return@OnClickListener
        }
        if (webSocketClient.connection.isClosed){
            Toast.makeText(this, "Отсутствует подключение к серверу",
                Toast.LENGTH_SHORT).show()
        }
        else
        {
            val newUserDlg = NewUserDLGTable(
                    "NEWUSERDLG::",
                    listUserForDlg,
                    editTextNameChat.text.toString())
            val msg = Json.encodeToString(newUserDlg)
            webSocketClient.send(msg)
            onBackPressed()
        }
    }

    private val itemListClick = AdapterView.OnItemClickListener { parent, view, position, id ->
        val checkBox = view.findViewById<CheckBox>(R.id.checkedForDlg)
        checkBox.isChecked = !checkBox.isChecked
        val elementTagUser = (myAdapterForCrtDlg.getItem(position) as Pair<String, String>).first
        if(checkBox.isChecked)
        {
            listUserForDlg.add(elementTagUser)
        }
        else
        {
            listUserForDlg.remove(elementTagUser)
        }
        myAdapterForCrtDlg.updateListChecked(listUserForDlg)
        if (listUserForDlg.size > 1){
            editTextNameChat.isEnabled = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_crt_dlg, menu)
        val menuItem = menu!!.findItem(R.id.action_search)
        val searchView = menuItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = resources.getString(R.string.searchDlg)
        searchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                myAdapterForCrtDlg.filter.filter(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

}