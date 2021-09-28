package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SqliteHelper(context: Context) :
        SQLiteOpenHelper(context, DB_NAME, null, DB_VERSIOM) {

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = "CREATE TABLE $TABLE_NAME " +
                "($ID TEXT PRIMARY KEY, $NAME TEXT)"
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun DelTable(id: String){
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$ID = ?", arrayOf(id))
    }

    fun addUser(user: Pair<String, String>): Boolean {
        //Create and/or open a database that will be used for reading and writing.
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(ID, user.first)
        values.put(NAME, user.second)
        val _success = db.insert(TABLE_NAME, null, values)
        db.close()
        Log.d("________InsertedID_________", "$_success")
        return (Integer.parseInt("$_success") != -1)
    }

    //get all users
    fun getAllUsers(): MutableList<Pair<String,String>>{
        var allUser = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    var id = cursor.getString(cursor.getColumnIndex(ID))
                    var name = cursor.getString(cursor.getColumnIndex(NAME))
                    Log.d("QQQQQ", "$id = $name")
                    allUser.add(id to name)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allUser
    }

    fun createMsgDlgTable(){
        val db = this.writableDatabase
        val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $MSGDLGTABLE " +
                "($ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, $DIALOG_ID TEXT, $SENDER INTEGER, $TEXT TEXT, $TIMECREATED TEXT)"
        // sender: 1 - from me, 0 - from other
        db?.execSQL(CREATE_TABLE)
    }

    fun addMsgInTable(dialogID : String, sender : Int,  text : String, timecreated : String ) : Boolean{
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(DIALOG_ID, dialogID)
        values.put(SENDER, sender)
        values.put(TEXT, text)
        values.put(TIMECREATED, timecreated)
        val _success = db.insert(MSGDLGTABLE, null, values)
        db.close()
        Log.d("________InsertedID_________", "$_success")
        return (Integer.parseInt("$_success") != -1)
    }
    fun getMsgWithUser(dialogID : String) : MutableList<Pair<String,Int>>{
        var allMsg = mutableListOf<Pair<String, Int>>()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $MSGDLGTABLE WHERE $DIALOG_ID = '$dialogID'"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    var text = cursor.getString(cursor.getColumnIndex(TEXT))
                    var sender = cursor.getInt(cursor.getColumnIndex(SENDER))
                    Log.d("QQQQQ", "$text = $sender")
                    allMsg.add(text to sender)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allMsg
    }

    companion object {
        private val DB_NAME = "UserChat"
        private val DB_VERSIOM = 1;
        private val TABLE_NAME = "users"
        private val MSGDLGTABLE = "MsgDlgTable"
        private val ID = "id"
        private val NAME = "name"
        private val DIALOG_ID = "dialog_id"
        private val TEXT = "text"
        private val TIMECREATED = "timecreated"
        private val SENDER = "sender"
    }
}