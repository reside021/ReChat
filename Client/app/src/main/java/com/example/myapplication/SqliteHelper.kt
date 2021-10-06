package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SqliteHelper(context: Context) :
        SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        val tableUsersChat = "CREATE TABLE IF NOT EXISTS $LIST_USERS_CHAT " +
                "($TAG_USER TEXT PRIMARY KEY, " +
                "$NAME_USER TEXT)"
        db?.execSQL(tableUsersChat)
        val tableUsersOnline = "CREATE TABLE IF NOT EXISTS $ONLINE_USERS " +
                "($TAG_USER TEXT PRIMARY KEY, " +
                "$NAME_USER TEXT)"
        db?.execSQL(tableUsersOnline)

        val tableMsgDlg = "CREATE TABLE IF NOT EXISTS $MSGDLGTABLE " +
                "($ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, $DIALOG_ID TEXT, $SENDER INTEGER, $TEXT TEXT, $TIMECREATED TEXT)"
        // sender: 1 - from me, 0 - from other
        db?.execSQL(tableMsgDlg)
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $LIST_USERS_CHAT")
        db?.execSQL("DROP TABLE IF EXISTS $ONLINE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $MSGDLGTABLE")
        onCreate(db)
    }


    fun clearOnlineTable(){
        val db = this.writableDatabase
        db.delete(ONLINE_USERS, null,null)
    }

    fun addUserInOnline(tagId : String, username : String) : Boolean{
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(TAG_USER, tagId)
        values.put(NAME_USER, username)
        val success = db.insert(ONLINE_USERS, null, values)
        db.close()
        Log.d("________InsertedInOnline_________", "$success")
        return (Integer.parseInt("$success") != -1)
    }
    fun getAllUsersOnline() : MutableList<Pair<String, String>>{
        val allUser = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $ONLINE_USERS"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val idTag = cursor.getString(cursor.getColumnIndex(TAG_USER))
                    val nameuser = cursor.getString(cursor.getColumnIndex(NAME_USER))
                    allUser.add(idTag to nameuser)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allUser
    }
    fun deleteUserFromOnline(tag: String) : Boolean{
        val db = this.writableDatabase
        val success = db.delete(ONLINE_USERS, "$TAG_USER = ?", arrayOf(tag))
        db.close()
        Log.d("________DeletedUsersOnline_________", "$success")
        return (Integer.parseInt("$success") != -1)
    }

    fun deleteUserChat(tag: String) : Boolean{
        val db = this.writableDatabase
        val success = db.delete(LIST_USERS_CHAT, "$TAG_USER = ?", arrayOf(tag))
        db.close()
        Log.d("________DeletedUsersChat_________", "$success")
        return (Integer.parseInt("$success") != -1)
    }

    fun addUserInChat(user: Pair<String, String>): Boolean {
        //Create and/or open a database that will be used for reading and writing.
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(TAG_USER, user.first)
        values.put(NAME_USER, user.second)
        val success = db.insert(LIST_USERS_CHAT, null, values)
        db.close()
        Log.d("________InsertedInChat_________", "$success")
        return (Integer.parseInt("$success") != -1)
    }

    //get all users
    fun getAllUsersChat(): MutableList<Pair<String,String>>{
        val allUser = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $LIST_USERS_CHAT"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val idTag = cursor.getString(cursor.getColumnIndex(TAG_USER))
                    val nameuser = cursor.getString(cursor.getColumnIndex(NAME_USER))
                    allUser.add(idTag to nameuser)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allUser
    }


    fun addMsgInTable(dialogID : String, sender : Int,  text : String, timecreated : String ) : Boolean{
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(DIALOG_ID, dialogID)
        values.put(SENDER, sender)
        values.put(TEXT, text)
        values.put(TIMECREATED, timecreated)
        val success = db.insert(MSGDLGTABLE, null, values)
        db.close()
        Log.d("________InsertedInTblMsg_________", "$success")
        return (Integer.parseInt("$success") != -1)
    }

    fun getMsgWithUser(dialogID : String) : MutableList<Pair<String,Int>>{
        val allMsg = mutableListOf<Pair<String, Int>>()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $MSGDLGTABLE WHERE $DIALOG_ID = '$dialogID'"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val text = cursor.getString(cursor.getColumnIndex(TEXT))
                    val sender = cursor.getInt(cursor.getColumnIndex(SENDER))
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
        private val DB_VERSION = 2;

        private val ONLINE_USERS = "OnlineUsers" // tablename
        private val TAG_USER = "Tag_Of_User" // field in table
        private val NAME_USER = "Name_Of_User" // field in table

        private val LIST_USERS_CHAT = "ListUsersChat" // tablename

        private val MSGDLGTABLE = "MsgDlgTable" // tablename
        private val ID = "id" // field in table
        private val DIALOG_ID = "dialog_id" // field in table (tag of users)
        private val TEXT = "text" // field in table (text of message)
        private val TIMECREATED = "timecreated" // field in table (time message)
        private val SENDER = "sender" // field in table (who is sender?)


    }
}