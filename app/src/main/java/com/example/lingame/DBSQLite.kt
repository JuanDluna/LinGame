package com.example.lingame

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.firebase.firestore.FirebaseFirestore

class DBSQLite(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    // Uso de base de datos local en caso de que no se pueda acceder a Firebase
    var FirebaseFS = FirebaseFirestore.getInstance();
    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "lingame"
        private const val TABLE_NAME = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PHOTO_URL = "photo_url"
        private const val COLUMN_LEVEL_EN = "level_En"
        private const val COLUMN_LEVEL_PR = "level_Pr"
        private const val COLUMN_LEVEL_FR = "level_Fr"
    }


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NAME " +
                "(${COLUMN_ID} TEXT PRIMARY KEY ," +
                "${COLUMN_NAME} TEXT," +
                "${COLUMN_EMAIL} TEXT," +
                "${COLUMN_PHOTO_URL} TEXT," +
                "${COLUMN_LEVEL_EN} INTEGER," +
                "${COLUMN_LEVEL_FR} INTEGER," +
                "${COLUMN_LEVEL_PR} INTEGER)");

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun newUser(UID : String, name: String, email: String, photo_url: String? = null,) : Boolean{
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("id", UID)
            put("name", name)
            put("email", email)
            put("photo_url", photo_url)
        }
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
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