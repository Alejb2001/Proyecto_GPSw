package mx.tecnm.chih2.proyecto_gpsw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CalorieTracker.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_FOOD_ENTRIES = "food_entries";
    private static final String TABLE_DAILY_GOAL = "daily_goal";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FOOD_NAME = "food_name";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_GOAL_CALORIES = "goal_calories";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FOOD_TABLE = "CREATE TABLE " + TABLE_FOOD_ENTRIES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FOOD_NAME + " TEXT,"
                + COLUMN_CALORIES + " INTEGER,"
                + COLUMN_DATE + " INTEGER)";
        
        String CREATE_GOAL_TABLE = "CREATE TABLE " + TABLE_DAILY_GOAL + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GOAL_CALORIES + " INTEGER)";
        
        db.execSQL(CREATE_FOOD_TABLE);
        db.execSQL(CREATE_GOAL_TABLE);
        
        // Insertar meta predeterminada
        ContentValues defaultGoal = new ContentValues();
        defaultGoal.put(COLUMN_GOAL_CALORIES, 2000);
        db.insert(TABLE_DAILY_GOAL, null, defaultGoal);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD_ENTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_GOAL);
        onCreate(db);
    }

    public long addFoodEntry(FoodEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FOOD_NAME, entry.getFoodName());
        values.put(COLUMN_CALORIES, entry.getCalories());
        values.put(COLUMN_DATE, entry.getDate().getTime());

        long id = db.insert(TABLE_FOOD_ENTRIES, null, values);
        db.close();
        return id;
    }

    public List<FoodEntry> getAllFoodEntries() {
        List<FoodEntry> entries = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_FOOD_ENTRIES + " ORDER BY " + COLUMN_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FoodEntry entry = new FoodEntry(
                        cursor.getString(1),
                        cursor.getInt(2)
                );
                entry.setId(cursor.getInt(0));
                entry.setDate(new Date(cursor.getLong(3)));
                entries.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }

    public void deleteFoodEntry(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOOD_ENTRIES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public int getTotalCalories() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_CALORIES + ") FROM " + TABLE_FOOD_ENTRIES, null);
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    public void updateDailyGoal(int calories) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GOAL_CALORIES, calories);
        db.update(TABLE_DAILY_GOAL, values, COLUMN_ID + " = 1", null);
        db.close();
    }

    public int getDailyGoal() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DAILY_GOAL, new String[]{COLUMN_GOAL_CALORIES},
                COLUMN_ID + " = 1", null, null, null, null);
        int goal = 2000; // valor por defecto
        if (cursor.moveToFirst()) {
            goal = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return goal;
    }
}
