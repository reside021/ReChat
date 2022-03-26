package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.icu.text.SimpleDateFormat
import android.util.Log
import com.example.myapplication.dataClasses.DataOfFriends
import com.example.myapplication.dataClasses.ResultActionWithFrnd
import com.example.myapplication.dataClasses.ResultCnfrmAddFriend
import com.example.myapplication.dataClasses.ResultDeleteFrined

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
        val tableFriends = "CREATE TABLE IF NOT EXISTS $FRIENDSTABLE " +
                "($TAGSENDERFRND TEXT, " +
                "$TAGRECEIVERFRND TEXT, " +
                "$FRNDNAME TEXT, " +
                "$STATUS INTEGER)"
        db?.execSQL(tableFriends)

        val tableMsgDlg = "CREATE TABLE IF NOT EXISTS $MSGDLGTABLE " +
                "($ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "$DIALOG_ID TEXT, "+
                "$SENDER TEXT, " +
                "$TYPEMSG TEXT, " +
                "$TEXTMSG TEXT, " +
                "$TIMECREATED INTEGER)"
        db?.execSQL(tableMsgDlg)

        val tableUserDlg = "CREATE TABLE IF NOT EXISTS $USERDLGTABLE " +
                "($ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "$DIALOG_ID TEXT, " +
                "$TAG_USER TEXT, " +
                "$ENTEREDTIME TEXT)"
        db?.execSQL(tableUserDlg)


    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $LIST_USERS_CHAT")
        db?.execSQL("DROP TABLE IF EXISTS $ONLINE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $MSGDLGTABLE")
        db?.execSQL("DROP TABLE IF EXISTS $USERDLGTABLE")
        db?.execSQL("DROP TABLE IF EXISTS $FRIENDSTABLE")
        onCreate(db)
    }


    fun clearTable(){
        val db = this.writableDatabase
        db.delete(ONLINE_USERS, null,null)
        db.delete(MSGDLGTABLE, null,null)
        db.delete(USERDLGTABLE, null,null)
        db.delete(LIST_USERS_CHAT, null,null)
        db.delete(FRIENDSTABLE, null,null)
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
                    val idTag = cursor.getString(cursor.getColumnIndexOrThrow(TAG_USER))
                    val nameuser = cursor.getString(cursor.getColumnIndexOrThrow(NAME_USER))
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

    fun getAllFriends(ourTag: String) : MutableList<Pair<String, String>>{
        val allUser = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $FRIENDSTABLE WHERE $TAGRECEIVERFRND <> '$ourTag' AND $STATUS = 2"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val tagUser = cursor.getString(cursor.getColumnIndexOrThrow(TAGRECEIVERFRND))
                    val nameOfUser = cursor.getString(cursor.getColumnIndexOrThrow(FRNDNAME))
                    allUser.add(tagUser to nameOfUser)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allUser
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
    fun checkUserInChat(tagUser : String) : Boolean{
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $LIST_USERS_CHAT WHERE $TAG_USER = '$tagUser'"
        val cursor = db.rawQuery(selectQuery, null)
        if(cursor != null){
            if (cursor.moveToFirst()) {
                cursor.close()
                db.close()
                return true
            }
        }
        cursor.close()
        db.close()
        return false
    }
    fun updateNameInUserChat(tagUser: String, newUserName : String) : Boolean{
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(NAME_USER, newUserName)
        val success = db.update(LIST_USERS_CHAT, values, "$TAG_USER = ?", arrayOf(tagUser))
        db.close()
        Log.d("________UpdateInTblListUsersChat_________", "$success")
        return (Integer.parseInt("$success") != -1)
    }
    fun getNameInUserChat(tagUser: String) : String{
        var name = ""
        val db = this.readableDatabase
        val selectQuery = "SELECT $NAME_USER FROM $LIST_USERS_CHAT WHERE $TAG_USER = '$tagUser'"
        val cursor = db.rawQuery(selectQuery, null)
        if(cursor != null){
            if (cursor.moveToFirst()) {
                do {
                    name = cursor.getString(cursor.getColumnIndexOrThrow(NAME_USER))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return name
    }
    fun checkExistChatWithUser(ourTag : String, tagUser : String ) : Boolean{
        val dialogId1 : String = "$CHAT$tagUser::$ourTag"
        val dialogId2 : String = "$CHAT$ourTag::$tagUser"
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $USERDLGTABLE WHERE $DIALOG_ID = '$dialogId1' OR $DIALOG_ID = '$dialogId2'"
        val cursor = db.rawQuery(selectQuery, null)
        if(cursor != null){
            if (cursor.moveToFirst()) {
                cursor.close()
                db.close()
                return true
            }
        }
        cursor.close()
        db.close()
        return false
    }

    fun getDialogIdWithUser(tagUser: String) : String{
        var dialogId : String = ""
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $USERDLGTABLE WHERE $TAG_USER = '$tagUser'"
        val cursor = db.rawQuery(selectQuery, null)
        if(cursor != null){
            if (cursor.moveToFirst()) {
                do {
                    dialogId = cursor.getString(cursor.getColumnIndexOrThrow(DIALOG_ID))
                    if(dialogId.substringBefore("#") == "CHAT"
                            || dialogId.substringBefore("#") == "GROUP") break
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return dialogId
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
                    val idTag = cursor.getString(cursor.getColumnIndexOrThrow(TAG_USER))
                    val nameuser = cursor.getString(cursor.getColumnIndexOrThrow(NAME_USER))
                    allUser.add(idTag to nameuser)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allUser
    }


    fun addMsgInTable(dialogID : String, sender : String, typeMsg : String, text : String, timecreated : Int) : Boolean{
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(DIALOG_ID, dialogID)
        values.put(SENDER, sender)
        values.put(TYPEMSG, typeMsg)
        values.put(TEXTMSG, text)
        values.put(TIMECREATED, timecreated)
        val success = db.insert(MSGDLGTABLE, null, values)
        db.close()
        Log.d("________InsertedInTblMsg_________", "$text $timecreated")
        return (Integer.parseInt("$success") != -1)
    }

    fun getMsgWithUser(dialogID : String) : MutableList<Array<String>>{
        val allMsg = mutableListOf<Array<String>>()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $MSGDLGTABLE WHERE $DIALOG_ID = '$dialogID' ORDER BY $TIMECREATED"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val text = cursor.getString(cursor.getColumnIndexOrThrow(TEXTMSG))
                    val sender = cursor.getString(cursor.getColumnIndexOrThrow(SENDER))
                    val timeCreated = cursor.getString(cursor.getColumnIndexOrThrow(TIMECREATED))
                    val typeMsg = cursor.getString(cursor.getColumnIndexOrThrow(TYPEMSG))
                    allMsg.add(arrayOf(sender, text, timeCreated, typeMsg))

                    Log.d("____DB____", "$text + $typeMsg + $sender + $timeCreated")
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allMsg
    }
    fun qq() : MutableList<Array<String>>{
        val allMsg = mutableListOf<Array<String>>()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $USERDLGTABLE"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val dialog_id = cursor.getString(cursor.getColumnIndexOrThrow(DIALOG_ID))
                    val tagUser = cursor.getString(cursor.getColumnIndexOrThrow(TAG_USER))
                    val timeCreated = cursor.getString(cursor.getColumnIndexOrThrow(TIMECREATED))
                    allMsg.add(arrayOf(dialog_id, tagUser, timeCreated))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allMsg
    }

    fun addUserInDLG(dialogID : String, tagUser : String,  enteredTime : String ) : Boolean{
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(DIALOG_ID, dialogID)
        values.put(TAG_USER, tagUser)
        values.put(ENTEREDTIME, enteredTime)
        val success = db.insert(USERDLGTABLE, null, values)
        db.close()
        Log.d("________InsertedInTblDlg_________", "$success")
        return (Integer.parseInt("$success") != -1)
    }

    fun getAllDlgFromDLG(): MutableList<String>{
        var allDlg : MutableList<String> = mutableListOf()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $USERDLGTABLE"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val dialog_id = cursor.getString(cursor.getColumnIndexOrThrow(DIALOG_ID))
                    allDlg.add(dialog_id)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allDlg
    }

    fun addUserInFriend(msg : ResultActionWithFrnd){
        //Create and/or open a database that will be used for reading and writing.
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(TAGSENDERFRND, msg.tagUserSender)
        values.put(TAGRECEIVERFRND, msg.tagUserReceiver)
        values.put(FRNDNAME, msg.nameUserReceiver)
        values.put(STATUS, 1)
        db.insert(FRIENDSTABLE, null, values)
        values.put(TAGSENDERFRND, msg.tagUserReceiver)
        values.put(TAGRECEIVERFRND, msg.tagUserSender)
        values.put(FRNDNAME, msg.nameUserSender)
        values.put(STATUS, 0)
        db.insert(FRIENDSTABLE, null, values)
        db.close()
        Log.d("________InsertedInFriend_________", "")
    }

    fun getAllFrndRequest(ourTag: String) : MutableList<Pair<String, String>>{
        val allUser = mutableListOf<Pair<String, String>>()
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $FRIENDSTABLE WHERE $TAGSENDERFRND = '$ourTag' AND $STATUS = 0"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val tagUser = cursor.getString(cursor.getColumnIndexOrThrow(TAGRECEIVERFRND))
                    val nameOfUser = cursor.getString(cursor.getColumnIndexOrThrow(FRNDNAME))
                    allUser.add(tagUser to nameOfUser)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return allUser
    }

    fun updateStatusFriend(msg : ResultCnfrmAddFriend){
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(STATUS, 2)
        db.update(FRIENDSTABLE, values, "$TAGSENDERFRND = ? AND $TAGRECEIVERFRND = ?", arrayOf(msg.tagUserFriend, msg.tagUserOur))
        db.update(FRIENDSTABLE, values, "$TAGSENDERFRND = ? AND $TAGRECEIVERFRND = ?", arrayOf(msg.tagUserOur, msg.tagUserFriend))
        db.close()
        Log.d("________UpdateInTblListUsersChat_________", "")
    }

    fun deleteFriend(msg : ResultDeleteFrined){
        val db = this.writableDatabase
        db.delete(FRIENDSTABLE, "$TAGSENDERFRND = ? AND $TAGRECEIVERFRND = ?", arrayOf(msg.tagUserFriend, msg.tagUserOur))
        db.delete(FRIENDSTABLE, "$TAGSENDERFRND = ? AND $TAGRECEIVERFRND = ?", arrayOf(msg.tagUserOur, msg.tagUserFriend))
        db.close()
        Log.d("________DeletedUsersChat_________", "$")
    }

    fun getStatusUser(tagUser: String) : Int{
        var status = -1;
        val db = readableDatabase
        val selectALLQuery = "SELECT $STATUS FROM $FRIENDSTABLE WHERE $TAGRECEIVERFRND = '$tagUser'"
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    status = cursor.getInt(cursor.getColumnIndexOrThrow(STATUS))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return status
    }

    fun addUserInFriendDW(el : DataOfFriends){
        //Create and/or open a database that will be used for reading and writing.
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(TAGSENDERFRND, el.tagSenderFrnd)
        values.put(TAGRECEIVERFRND, el.tagReceiverFrnd)
        values.put(FRNDNAME, el.nameFrnd)
        values.put(STATUS, el.status)
        db.insert(FRIENDSTABLE, null, values)
        db.close()
        Log.d("________InsertedInFriendDW_________", "")
    }

    fun updateNameInFriends(tagUser: String, newUserName : String){
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(FRNDNAME, newUserName)
        val success = db.update(FRIENDSTABLE, values, "$TAGRECEIVERFRND = ?", arrayOf(tagUser))
        db.close()
        Log.d("________UpdateNameInTblFreinds_________", "")
    }

    fun getCountMsgDlg() :  String{
        val db = this.readableDatabase
        val selectQuery = "SELECT $DIALOG_ID FROM $USERDLGTABLE"
        val cursor = db.rawQuery(selectQuery, null)
        if(cursor != null){
            if (cursor.moveToFirst()) {
                do {
                    return cursor.getString(cursor.getColumnIndexOrThrow(DIALOG_ID))
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()
        return ""
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
        private val TEXTMSG = "textMsg" // field in table (text of message)
        private val TYPEMSG = "typeMsg"
        private val TIMECREATED = "timecreated" // field in table (time message)
        private val SENDER = "sender" // field in table (who is sender?)

        private val USERDLGTABLE = "UserDlgTable" // tablename
        private val ENTEREDTIME = "EnteredTime"

        private val FRIENDSTABLE = "FriendsTable" // tablename
        private val TAGSENDERFRND = "tagSenderFrnd" // field in table
        private val TAGRECEIVERFRND = "tagReceiverFrnd" // field in table
        private val STATUS = "status" // field in table
        private val FRNDNAME = "friendName" // field in table

        private val CHAT = "CHAT#"

    }
}