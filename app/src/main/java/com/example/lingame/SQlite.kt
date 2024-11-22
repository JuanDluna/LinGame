package com.example.lingame

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.firebase.firestore.FirebaseFirestore

class DATABASE(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    var FirebaseFS = FirebaseFirestore.getInstance();
    companion object{
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "lingame"
        private val TABLE_NAME = "users"
    }


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NAME (id TEXT PRIMARY KEY , name TEXT, email TEXT, level_En INTEGER, level_Pr INTEGER, level_Fr INTEGER)");

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun newUser(UID : String, name: String, email: String){
        val db = this.writableDatabase
        val sql = "INSERT INTO $TABLE_NAME (id, name, email) VALUES ('$UID' ,'$name', '$email')"
    }

    fun setUsers(UID: String, name: String, email: String, level_En: Int?, level_Pr: Int?, level_Fr: Int?){
        val db = this.writableDatabase
        val sql = "UPDATE $TABLE_NAME SET name = '$name', email = '$email', level_En = $level_En, level_Pr = $level_Pr, level_Fr = $level_Fr WHERE id = '$UID' "
        db.execSQL(sql)
    }

    fun increaseEnglishLevel(UID: String){
        val db = this.writableDatabase
        val sql = "UPDATE $TABLE_NAME SET level_En = level_En + 1 WHERE id = '$UID'"
        db.execSQL(sql)
    }

    fun increaseFrenchLevel(UID: String){
        val db = this.writableDatabase
        val sql = "UPDATE $TABLE_NAME SET level_Fr = level_Fr + 1 WHERE id = '$UID'"
        db.execSQL(sql)
    }

    fun increasePortugueseLevel(UID: String){
        val db = this.writableDatabase
        val sql = "UPDATE $TABLE_NAME SET level_Pr = level_Pr + 1 WHERE id = '$UID'"
        db.execSQL(sql)
    }


    fun getUserLevels(UID: String): List<Int>{
        val db = this.readableDatabase
        val sql = "SELECT level_En, level_Pr, level_Fr FROM $TABLE_NAME WHERE id = '$UID'"
        val cursor = db.rawQuery(sql, null)

        val levels = mutableListOf<Int>()

        if (cursor.moveToFirst()) {
            val levelEn = cursor.getInt(
                cursor.getColumnIndexOrThrow("level_En"))
            val levelPr = cursor.getInt(
                cursor.getColumnIndexOrThrow("level_Pr"))
            val levelFr = cursor.getInt(
                cursor.getColumnIndexOrThrow("level_Fr"))

            levels.add(levelEn)
            levels.add(levelPr)
            levels.add(levelFr)
        }

        cursor.close()
        return levels
    }

}