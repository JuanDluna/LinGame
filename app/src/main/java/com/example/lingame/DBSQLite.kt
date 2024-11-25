package com.example.lingame

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
class DBSQLite(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    // Uso de base de datos local en caso de que no se pueda acceder a Firebase
    var FirebaseFS = FirebaseFirestore.getInstance()

    companion object {
        private const val DATABASE_VERSION = 3  // Incrementamos la versión de la base de datos
        private const val DATABASE_NAME = "lingame"
        private const val TABLE_USERS = "users"
        private const val TABLE_ENGLISH = "english_levels"
        private const val TABLE_FRENCH = "french_levels"
        private const val TABLE_PORTUGUESE = "portuguese_levels"

        // Columnas de la tabla 'users'
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PHOTO_URL = "photo_url"
        private const val COLUMN_GENERAL_LEVEL = "generalLevel"  // Nueva columna

        // Columnas de las tablas de niveles de idiomas
        private const val COLUMN_LEVEL_CREA_HISTORIA = "levelCreaHistoria"
        private const val COLUMN_LEVEL_RR = "levelRR"
        private const val COLUMN_LEVEL_TRADUCELO = "levelTraducelo"
        private const val COLUMN_LEVEL_PARAFRASEA = "levelParafrasea"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla de usuarios (incluyendo la columna generalLevel)
        db.execSQL(
            "CREATE TABLE $TABLE_USERS (" +
                    "$COLUMN_ID TEXT PRIMARY KEY," +
                    "$COLUMN_NAME TEXT," +
                    "$COLUMN_EMAIL TEXT," +
                    "$COLUMN_PHOTO_URL TEXT," +
                    "$COLUMN_GENERAL_LEVEL REAL DEFAULT 0.0)"  // Agregamos la columna generalLevel
        )

        // Las tablas de idiomas se crean solo si el usuario tiene una relación con el idioma
        // Crear tabla de inglés (si no existe)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_ENGLISH (" +
                    "$COLUMN_ID TEXT PRIMARY KEY," +
                    "$COLUMN_LEVEL_CREA_HISTORIA INTEGER DEFAULT 0," +
                    "$COLUMN_LEVEL_RR INTEGER DEFAULT 0," +
                    "$COLUMN_LEVEL_TRADUCELO INTEGER DEFAULT 0," +
                    "$COLUMN_LEVEL_PARAFRASEA INTEGER DEFAULT 0," +
                    "FOREIGN KEY($COLUMN_ID) REFERENCES $TABLE_USERS($COLUMN_ID))"
        )

        // Crear tabla de francés (si no existe)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_FRENCH (" +
                    "$COLUMN_ID TEXT PRIMARY KEY," +
                    "$COLUMN_LEVEL_CREA_HISTORIA INTEGER DEFAULT 0," +
                    "$COLUMN_LEVEL_RR INTEGER DEFAULT 0," +
                    "$COLUMN_LEVEL_TRADUCELO INTEGER DEFAULT 0," +
                    "$COLUMN_LEVEL_PARAFRASEA INTEGER DEFAULT 0," +
                    "FOREIGN KEY($COLUMN_ID) REFERENCES $TABLE_USERS($COLUMN_ID))"
        )

        // Crear tabla de portugués (si no existe)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_PORTUGUESE (" +
                    "$COLUMN_ID TEXT PRIMARY KEY," +
                    "$COLUMN_LEVEL_CREA_HISTORIA INTEGER DEFAULT 0," +
                    "$COLUMN_LEVEL_RR INTEGER DEFAULT 0," +
                    "$COLUMN_LEVEL_TRADUCELO INTEGER DEFAULT 0," +
                    "$COLUMN_LEVEL_PARAFRASEA INTEGER DEFAULT 0," +
                    "FOREIGN KEY($COLUMN_ID) REFERENCES $TABLE_USERS($COLUMN_ID))"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            // Si la versión es 2 o menor, agregamos la columna generalLevel a la tabla de usuarios
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_GENERAL_LEVEL REAL DEFAULT 0.0")
        }
    }

    // Método para crear un nuevo usuario, incluyendo el campo generalLevel
    fun newUser(UID: String, name: String, email: String, photo_url: String? = null): Boolean {
        val db = this.writableDatabase

        val userValues = ContentValues().apply {
            put(COLUMN_ID, UID)
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PHOTO_URL, photo_url)
            put(COLUMN_GENERAL_LEVEL, 0.0)  // Valor inicial de generalLevel
        }

        // Insertar usuario
        val userResult = db.insert(TABLE_USERS, null, userValues)

        db.close()
        return userResult != -1L
    }

    // Método para actualizar el nivel general del usuario
    fun updateGeneralLevel(UID: String, newGeneralLevel: Float) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_GENERAL_LEVEL, newGeneralLevel)
        }
        db.update(TABLE_USERS, values, "$COLUMN_ID = ?", arrayOf(UID))
    }

    // Método para obtener el nivel general del usuario
    fun getGeneralLevel(UID: String): Float {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_GENERAL_LEVEL),
            "$COLUMN_ID = ?",
            arrayOf(UID),
            null,
            null,
            null
        )

        var generalLevel = 0.0f
        if (cursor.moveToFirst()) {
            generalLevel = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_GENERAL_LEVEL))
        }

        cursor.close()
        return generalLevel
    }

    // Método para actualizar todos los datos de un usuario, incluyendo generalLevel
    fun setUserData(
        UID: String,
        name: String,
        email: String,
        photoUrl: String? = null,
        generalLevel: Float,
        englishLevel: Map<String, Int>,
        frenchLevel: Map<String, Int>,
        portugueseLevel: Map<String, Int>) {

        Log.d("DBSQLite", "Actualizando datos de la base de datos")
        val db = this.writableDatabase
        val userValues = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PHOTO_URL, photoUrl)
            put(COLUMN_GENERAL_LEVEL, generalLevel)
        }
        val userResult = db.update(
            TABLE_USERS, userValues, "$COLUMN_ID = ?", arrayOf(UID)
        )

        Log.d("DBSQLite", "Resultado de actualizacion : ${userResult}")
        if (userResult > 0) {
            when{
                englishLevel.isNotEmpty() -> {
                    Log.d("DBSQLite", "Actualizando tabla de ingles")
                    val englishValues = ContentValues().apply {
                        put(COLUMN_ID, UID)
                        put(COLUMN_LEVEL_CREA_HISTORIA, englishLevel[COLUMN_LEVEL_CREA_HISTORIA])
                        put(COLUMN_LEVEL_RR, englishLevel[COLUMN_LEVEL_RR])
                        put(COLUMN_LEVEL_TRADUCELO, englishLevel[COLUMN_LEVEL_TRADUCELO])
                        put(COLUMN_LEVEL_PARAFRASEA, englishLevel[COLUMN_LEVEL_PARAFRASEA])
                    }
                    db.insert(TABLE_ENGLISH, null, englishValues)
                }

                portugueseLevel.isNotEmpty() ->{
                    Log.d("DBSQLite", "Actualizando tabla de portugues")
                    val portugueseValues = ContentValues().apply {
                        put(COLUMN_ID, UID)
                        put(COLUMN_LEVEL_CREA_HISTORIA, portugueseLevel[COLUMN_LEVEL_CREA_HISTORIA])
                        put(COLUMN_LEVEL_RR, portugueseLevel[COLUMN_LEVEL_RR])
                        put(COLUMN_LEVEL_TRADUCELO, portugueseLevel[COLUMN_LEVEL_TRADUCELO])
                        put(COLUMN_LEVEL_PARAFRASEA, portugueseLevel[COLUMN_LEVEL_PARAFRASEA])
                    }
                    db.insert(TABLE_PORTUGUESE, null, portugueseValues)
                }
                frenchLevel.isNotEmpty() ->{
                    Log.d("DBSQLite", "Actualizando tabla de frances")
                    val frenchValues = ContentValues().apply {
                        put(COLUMN_ID, UID)
                        put(COLUMN_LEVEL_CREA_HISTORIA, frenchLevel[COLUMN_LEVEL_CREA_HISTORIA])
                        put(COLUMN_LEVEL_RR, frenchLevel[COLUMN_LEVEL_RR])
                        put(COLUMN_LEVEL_TRADUCELO, frenchLevel[COLUMN_LEVEL_TRADUCELO])
                        put(COLUMN_LEVEL_PARAFRASEA, frenchLevel[COLUMN_LEVEL_PARAFRASEA])
                    }
                    db.insert(TABLE_FRENCH, null, frenchValues)
                }
            }
        }
    }

    fun getUserData(UID: String): Cursor? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_GENERAL_LEVEL, COLUMN_PHOTO_URL),
            "$COLUMN_ID = ?",
            arrayOf(UID),
            null,
            null,
            null
        )

        Log.d("DBSQLite", "Datos a regresar de la consulta: ${cursor.count}")
        return cursor
    }


    // Metodo para verificar si un usuario existe
    fun isUserExists(UID: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_ID = ?",
            arrayOf(UID),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        Log.d("DBSQLite", "Cantidad de campos obtenidos: ${cursor.count}")
        Log.d("DBSQLite", "Existencia de usuario ${UID} en base de datos:${exists}")
        return exists
    }

    fun createFrenchTable(UID: String) {
        val db = this.writableDatabase
        val frenchValues = ContentValues().apply {
            put(COLUMN_ID, UID)
        }
        db.insert(TABLE_FRENCH, null, frenchValues)
        db.close()
    }

    fun createEnglishTable(UID: String) {
        val db = this.writableDatabase
        val englishValues = ContentValues().apply {
            put(COLUMN_ID, UID)
        }
        db.insert(TABLE_ENGLISH, null, englishValues)
        db.close()
    }

    fun createPortugueseTable(UID: String) {
        val db = this.writableDatabase
        val portugueseValues = ContentValues().apply {
            put(COLUMN_ID, UID)
        }
        db.insert(TABLE_PORTUGUESE, null, portugueseValues)
    }

}
