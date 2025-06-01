package vn.edu.usth.naturevoice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "naturevoice.db";
    private static final int DATABASE_VERSION = 1;

    // Bảng Plants
    private static final String TABLE_PLANTS = "plants";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SPECIES = "species";
    private static final String COLUMN_PLANT_ID = "plant_id";
    private static final String COLUMN_POT_ID = "pot_id";
    private static final String COLUMN_SENSOR_ID = "sensor_id";

    // Bảng Settings
    private static final String TABLE_SETTINGS = "settings";
    private static final String COLUMN_KEY = "key";
    private static final String COLUMN_VALUE = "value";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng plants
        String createPlantsTable = "CREATE TABLE " + TABLE_PLANTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME + " TEXT," +
                COLUMN_SPECIES + " INTEGER," +
                COLUMN_PLANT_ID + " INTEGER," +
                COLUMN_POT_ID + " INTEGER," +
                COLUMN_SENSOR_ID + " INTEGER" + ")";
        db.execSQL(createPlantsTable);

        // Tạo bảng settings
        String createSettingsTable = "CREATE TABLE " + TABLE_SETTINGS + "(" +
                COLUMN_KEY + " TEXT PRIMARY KEY," +
                COLUMN_VALUE + " TEXT" + ")";
        db.execSQL(createSettingsTable);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu tồn tại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);

        // Tạo lại bảng mới
        onCreate(db);
    }
}