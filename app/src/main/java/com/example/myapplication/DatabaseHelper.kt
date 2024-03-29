import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "rec.db"
        private const val TABLE_NAME = "recipes"
        private const val COLUMN_URL = "url"
        private const val COLUMN_IMG = "img"
        private const val COLUMN_NUMBER = "number"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_AMOUNT_SERVINGS = "amount_servings"
        private const val COLUMN_ENERGY = "energy"
        private const val COLUMN_INGREDIENTS = "ingredients"
        private const val COLUMN_INSTRUCTIONS = "instructions"
    }

    private val databasePath: String by lazy { context.getDatabasePath(DATABASE_NAME).path }

    init {
        copyDatabaseFromAssets()
    }

    private fun copyDatabaseFromAssets() {
        val inputStream: InputStream
        val outputStream: FileOutputStream
        try {
            inputStream = context.assets.open(DATABASE_NAME)
            val outFile = File(databasePath)
            outputStream = FileOutputStream(outFile)

            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Database creation not needed here since it's already created from the assets
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Database upgrade not needed here
    }

    fun getRecipeUrlAndImgByNumber(number: Int): Pair<String, String>? {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var result: Pair<String, String>? = null
        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_NAME, $COLUMN_IMG FROM $TABLE_NAME WHERE $COLUMN_NUMBER = ?",
                arrayOf(number.toString())
            )
            if (cursor.moveToFirst()) {
                result = Pair(cursor.getString(0), cursor.getString(1))
            }
        } catch (e: SQLiteException) {
            // Handle exception
        } finally {
            cursor?.close()
            db.close()
        }
        return result
    }


    @SuppressLint("Range")
    fun getRecipeNumbersByIngredients(ingredients: String): List<Int> {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        val recipeNumbers = mutableListOf<Int>()

        try {
            //al query1 = "SELECT $COLUMN_NUMBER FROM $TABLE_NAME WHERE $COLUMN_NAME IN ($placeholders)"

            val query = "SELECT * FROM recipes WHERE lower(ingredients) like '%$ingredients%' ORDER BY likes DESC LIMIT 10"
            cursor = db.rawQuery(query, arrayOfNulls(0))

            while (cursor.moveToNext()) {
                val number = cursor.getInt(cursor.getColumnIndex(COLUMN_NUMBER))
                recipeNumbers.add(number)
            }
        } catch (e: SQLiteException) {
            // Обрабатываем исключение
        } finally {
            cursor?.close()
            db.close()
        }

        return recipeNumbers
    }


    data class Recipe_bd(val name: String, val img: String, val time: String)

    @SuppressLint("Range", "Recycle")
    fun getTopRecipesByLikes(limit: Int, offset: Int): List<Recipe_bd> {
        val db = this.readableDatabase
        val cursor: Cursor?
        val result = mutableListOf<Recipe_bd>()

        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_NAME, $COLUMN_IMG, $COLUMN_TIME FROM $TABLE_NAME ORDER BY likes DESC LIMIT ? OFFSET ?",
                arrayOf(limit.toString(), offset.toString())
            )

            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                val img = cursor.getString(cursor.getColumnIndex(COLUMN_IMG))
                val time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME))
                result.add(Recipe_bd(name, img, time))
            }
        } catch (e: SQLiteException) {
            // Handle exception
        } finally {
        }

        return result
    }

    @SuppressLint("Range")
    fun getRecipeDataByName(word: String): List<RecipeData> {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        val result = mutableListOf<RecipeData>()

        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_NAME, $COLUMN_TIME, $COLUMN_IMG, $COLUMN_AMOUNT_SERVINGS, $COLUMN_ENERGY, $COLUMN_INGREDIENTS, $COLUMN_INSTRUCTIONS, $COLUMN_NUMBER FROM $TABLE_NAME WHERE LOWER($COLUMN_NAME) LIKE ? ORDER BY $COLUMN_NUMBER DESC LIMIT 20",
                arrayOf("%$word%")
            )

            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                val time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME))
                val img = cursor.getString(cursor.getColumnIndex(COLUMN_IMG))
                val amountServings = cursor.getString(cursor.getColumnIndex(COLUMN_AMOUNT_SERVINGS))
                val energy = cursor.getString(cursor.getColumnIndex(COLUMN_ENERGY))
                val ingredients = cursor.getString(cursor.getColumnIndex(COLUMN_INGREDIENTS))
                val instructions = cursor.getString(cursor.getColumnIndex(COLUMN_INSTRUCTIONS))
                val number = cursor.getInt(cursor.getColumnIndex(COLUMN_NUMBER))
                if (img != null) {
                    result.add(
                        RecipeData(
                            name,
                            time,
                            img,
                            amountServings,
                            energy,
                            ingredients,
                            instructions,
                            number
                        )
                    )
                }
            }
        } catch (e: SQLiteException) {
            // Handle exception
        } finally {
            cursor?.close()
            db.close()
        }

        return result
    }

    @SuppressLint("Range")
    fun getRecipeDataByNumber(number: Int): Recipe_bd? {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var result: Recipe_bd? = null

        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_NAME, $COLUMN_IMG, $COLUMN_TIME FROM $TABLE_NAME WHERE $COLUMN_NUMBER = ?",
                arrayOf(number.toString())
            )

            if (cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                val img = cursor.getString(cursor.getColumnIndex(COLUMN_IMG))
                val time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME))
                result = Recipe_bd(name, img, time)
            }
        } catch (e: SQLiteException) {
            // Handle exception
        } finally {
        }

        return result
    }

    @SuppressLint("Range")
    fun getRecipeDataByImg(img: String): List<RecipeData> {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        val result = mutableListOf<RecipeData>()

        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_NAME, $COLUMN_TIME, $COLUMN_IMG, $COLUMN_AMOUNT_SERVINGS, $COLUMN_ENERGY, $COLUMN_INGREDIENTS, $COLUMN_INSTRUCTIONS, $COLUMN_NUMBER FROM $TABLE_NAME WHERE $COLUMN_IMG = ?",
                arrayOf(img)
            )

            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                val time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME))
                val amountServings = cursor.getString(cursor.getColumnIndex(COLUMN_AMOUNT_SERVINGS))
                val energy = cursor.getString(cursor.getColumnIndex(COLUMN_ENERGY))
                val ingredients = cursor.getString(cursor.getColumnIndex(COLUMN_INGREDIENTS))
                val instructions = cursor.getString(cursor.getColumnIndex(COLUMN_INSTRUCTIONS))
                val number = cursor.getInt(cursor.getColumnIndex(COLUMN_NUMBER))

                result.add(
                    RecipeData(
                        name,
                        time,
                        img,
                        amountServings,
                        energy,
                        ingredients,
                        instructions,
                        number
                    )
                )
            }
        } catch (e: SQLiteException) {
            // Handle exception
        } finally {
            cursor?.close()
            db.close()
        }

        return result
    }
    data class RecipeData(
        val name: String,
        val time: String,
        val img: String,
        val amountServings: String,
        val energy: String,
        val ingredients: String,
        val instructions: String,
        val number: Int
    )
}
