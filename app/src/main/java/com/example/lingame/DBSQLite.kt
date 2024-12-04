package com.example.lingame

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.Settings.Global.getString
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class DBSQLite(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    // Uso de base de datos local en caso de que no se pueda acceder a Firebase
    var FirebaseFS = FirebaseFirestore.getInstance()
    var resources = context.resources

    companion object {
        const val DATABASE_VERSION = 3  // Incrementamos la versión de la base de datos
        const val DATABASE_NAME = "lingame"
        const val TABLE_USERS = "users"
        const val TABLE_ENGLISH = "english_levels"
        const val TABLE_FRENCH = "french_levels"
        const val TABLE_PORTUGUESE = "portuguese_levels"

        // Columnas de la tabla 'users'
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHOTO_URL = "photo_url"
        const val COLUMN_GENERAL_LEVEL = "generalLevel"

        // Columnas de las tablas de niveles de idiomas
        const val COLUMN_LEVEL_CREA_HISTORIA = "levelCreaHistoria"
        const val COLUMN_LEVEL_RR = "levelRR"
        const val COLUMN_LEVEL_TRADUCELO = "levelTraducelo"
        const val COLUMN_LEVEL_PARAFRASEA = "levelParafrasea"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_USERS (" +
                    "$COLUMN_ID TEXT PRIMARY KEY," +
                    "$COLUMN_NAME TEXT," +
                    "$COLUMN_EMAIL TEXT," +
                    "$COLUMN_PHOTO_URL TEXT," +
                    "$COLUMN_GENERAL_LEVEL REAL DEFAULT 0.0)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_ENGLISH (" +
                    "$COLUMN_ID TEXT PRIMARY KEY," +
                    "$COLUMN_LEVEL_CREA_HISTORIA INTEGER DEFAULT 1," +
                    "$COLUMN_LEVEL_RR REAL DEFAULT 0.0," +
                    "$COLUMN_LEVEL_TRADUCELO REAL DEFAULT 0.0," +
                    "$COLUMN_LEVEL_PARAFRASEA REAL DEFAULT 0.0," +
                    "FOREIGN KEY($COLUMN_ID) REFERENCES $TABLE_USERS($COLUMN_ID))"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_FRENCH (" +
                    "$COLUMN_ID TEXT PRIMARY KEY," +
                    "$COLUMN_LEVEL_CREA_HISTORIA INTEGER DEFAULT 1," +
                    "$COLUMN_LEVEL_RR REAL DEFAULT 0.0," +
                    "$COLUMN_LEVEL_TRADUCELO REAL DEFAULT 0.0," +
                    "$COLUMN_LEVEL_PARAFRASEA REAL DEFAULT 0.0," +
                    "FOREIGN KEY($COLUMN_ID) REFERENCES $TABLE_USERS($COLUMN_ID))"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_PORTUGUESE (" +
                    "$COLUMN_ID TEXT PRIMARY KEY," +
                    "$COLUMN_LEVEL_CREA_HISTORIA INTEGER DEFAULT 1," +
                    "$COLUMN_LEVEL_RR REAL DEFAULT 0.0," +
                    "$COLUMN_LEVEL_TRADUCELO REAL DEFAULT 0.0," +
                    "$COLUMN_LEVEL_PARAFRASEA REAL DEFAULT 0.0," +
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

        val languageValues = ContentValues().apply {
            put(COLUMN_ID, UID)
            put(COLUMN_LEVEL_CREA_HISTORIA, 1)
            put(COLUMN_LEVEL_RR, 0.0)
            put(COLUMN_LEVEL_TRADUCELO, 0.0)
            put(COLUMN_LEVEL_PARAFRASEA, 0.0)
        }
        db.insert(TABLE_ENGLISH, null, languageValues)
        db.insert(TABLE_FRENCH, null, languageValues)
        db.insert(TABLE_PORTUGUESE, null, languageValues)

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

    fun updateLevelsByCategory(UID: String, language: String, category: String, increment: Float): Boolean {
        val db = this.writableDatabase
        var success = false

        try {
            // Actualizar el nivel general del usuario
            val currentGeneralLevel = getGeneralLevel(UID)

            val newGeneralLevel = currentGeneralLevel + increment

            updateGeneralLevel(UID, newGeneralLevel)

            // Seleccionar la tabla correspondiente al idioma
            val tableName = when (language) {
                resources.getString(R.string.englishValuePreferences) -> TABLE_ENGLISH
                resources.getString(R.string.frenchValuePreferences) -> TABLE_FRENCH
                resources.getString(R.string.portugueseValuePreferences) -> TABLE_PORTUGUESE
                else -> null
            }

            if (tableName != null) {
                // Obtener el nombre de la columna basado en la categoría
                val columnName = when (category) {
                    resources.getString(R.string.creaTuHistoria) -> COLUMN_LEVEL_CREA_HISTORIA
                    resources.getString(R.string.retoRapido) -> COLUMN_LEVEL_RR
                    resources.getString(R.string.traducelo) -> COLUMN_LEVEL_TRADUCELO
                    resources.getString(R.string.parafrasea) -> COLUMN_LEVEL_PARAFRASEA
                    else -> null
                }

                if (columnName != null) {
                    // Obtener el nivel actual de la categoría
                    val query = "SELECT $columnName FROM $tableName WHERE $COLUMN_ID = ?"
                    val cursor = db.rawQuery(query, arrayOf(UID))
                    var currentLevel = 0F
                    if (cursor.moveToFirst()) {
                        Log.i("DBSQLite", "cursor: ${cursor.getFloat(cursor.getColumnIndexOrThrow(columnName))}")
                        currentLevel = cursor.getFloat(cursor.getColumnIndexOrThrow(columnName))
                    }
                    cursor.close()

                    // Actualizar el nivel en la categoría correspondiente
                    val newLevel = currentLevel + increment
                    val categoryValues = ContentValues().apply {
                        put(columnName, newLevel)
                    }
                    val rowsAffected = db.update(tableName, categoryValues, "$COLUMN_ID = ?", arrayOf(UID))
                    success = rowsAffected > 0
                }
            }
        } catch (e: Exception) {
            Log.e("DBSQLite", "Error actualizando niveles: ${e.message}")
        } finally {
            db.close()
        }

        return success
    }

    fun updateLevelCreateStory(UID: String, newLevel: Int, language: String): Boolean {
        val db = this.writableDatabase
        var success = false
        try {
            val tableName = when (language) {
                resources.getString(R.string.englishValuePreferences) -> TABLE_ENGLISH
                resources.getString(R.string.frenchValuePreferences) -> TABLE_FRENCH
                resources.getString(R.string.portugueseValuePreferences) -> TABLE_PORTUGUESE
                else -> {
                    Log.e("DBSQLite", "Idioma no válido: $language")
                    return false
                }
            }
            val categoryValues = ContentValues().apply {
                put(COLUMN_LEVEL_CREA_HISTORIA, newLevel)
            }
            val rowsAffected = db.update(tableName, categoryValues, "$COLUMN_ID = ?", arrayOf(UID))
            success = rowsAffected > 0

        }
        catch (e: Exception){
            Log.e("DBSQLite", "Error actualizando niveles: ${e.message}")
        }
        finally {
            db.close()
        }
        return success
    }

    fun getLevelByCategoryAndLanguage(UID: String, language: String, category: String): Float {

        val db = this.readableDatabase


        // Determinar la tabla según el idioma
        val tableName = when (language) {
            resources.getString(R.string.englishValuePreferences) -> TABLE_ENGLISH
            resources.getString(R.string.frenchValuePreferences) -> TABLE_FRENCH
            resources.getString(R.string.portugueseValuePreferences) -> TABLE_PORTUGUESE
            else -> {
                Log.e("DBSQLite", "Idioma no válido: $language")
                return 0.0f  // Devolver un valor predeterminado o lanzar un error
            }
        }


        // Construir la consulta SQL
        val query = "SELECT $category FROM $tableName WHERE $COLUMN_ID = ?"

        try {
            val cursor = db.rawQuery(query, arrayOf(UID))
            var level = 0.0f

            // Verificar si se obtuvo un resultado
            if (cursor.moveToFirst()) {
                // Comprobar si la columna 'category' existe en el cursor
                val columnIndex = cursor.getColumnIndex(category)
                if (columnIndex != -1) {
                    level = cursor.getFloat(columnIndex)
                } else {
                    Log.e("DBSQLite", "Columna no encontrada: $category")
                }
            } else {
                Log.e("DBSQLite", "No se encontró un registro para UID: $UID")
            }

            cursor.close()
            Log.i("DBSQLite", "Level: $level")
            return level
        } catch (e: Exception) {
            Log.e("DBSQLite", "Error en la consulta: ${e.message}")
            return 0.0f  // Devolver un valor predeterminado en caso de error
        }
    }

    fun getLevelCreateStoryByLanguage(UID: String, language: String): Int {
        val db = this.readableDatabase
        val tableName = when (language) {
            resources.getString(R.string.englishValuePreferences) -> TABLE_ENGLISH
            resources.getString(R.string.frenchValuePreferences) -> TABLE_FRENCH
            resources.getString(R.string.portugueseValuePreferences) -> TABLE_PORTUGUESE
            else -> {
                Log.e("DBSQLite", "Idioma no válido: $language")
                return 0
            }
        }
        val query = "SELECT $COLUMN_LEVEL_CREA_HISTORIA FROM $tableName WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(UID))

        var level = 0
        if (cursor.moveToFirst()) {
            level = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LEVEL_CREA_HISTORIA))
        }
        cursor.close()
        return level
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

    fun createTableLanguageIfDoestnExists(UID: String, language: String){
        when(language){
            "Inglés" -> createEnglishTable(UID)
            "Francés" -> createFrenchTable(UID)
            "Portugués" -> createPortugueseTable(UID)
        }
    }

}
