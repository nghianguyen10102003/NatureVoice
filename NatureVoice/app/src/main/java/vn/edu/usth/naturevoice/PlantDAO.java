package vn.edu.usth.naturevoice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class PlantDAO {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public PlantDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Thay thế savePlantList()
    public void savePlantList(ArrayList<Plant> plantList) {
        database.beginTransaction();
        try {
            database.delete("plants", null, null); // Xóa dữ liệu cũ
            for (Plant plant : plantList) {
                ContentValues values = new ContentValues();
                values.put("id", plant.getId());
                values.put("name", plant.getName());
                values.put("species", plant.getSpecies());
                values.put("plant_id", plant.getPlantId());
                values.put("pot_id", plant.getPotId());
                values.put("sensor_id", plant.getSensorId());
                database.insert("plants", null, values);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    // Thay thế loadPlantList()
    public ArrayList<Plant> loadPlantList() {
        ArrayList<Plant> plantList = new ArrayList<>();
        Cursor cursor = database.query("plants", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Plant plant = new Plant(
                        cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getInt(cursor.getColumnIndex("species")),
                        cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getInt(cursor.getColumnIndex("plant_id")),
                        cursor.getInt(cursor.getColumnIndex("pot_id")),
                        cursor.getInt(cursor.getColumnIndex("sensor_id"))
                );
                plantList.add(plant);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return plantList;
    }

    // Thay thế saveLastPlantId() và loadLastPlantId()
    public void saveLastPlantId(int plantId) {
        ContentValues values = new ContentValues();
        values.put("key", "last_plant_id");
        values.put("value", String.valueOf(plantId));
        database.insertWithOnConflict("settings", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int loadLastPlantId() {
        Cursor cursor = database.query("settings", new String[]{"value"},
                "key = ?", new String[]{"last_plant_id"}, null, null, null);
        int lastId = 0;
        if (cursor.moveToFirst()) {
            lastId = Integer.parseInt(cursor.getString(0));
        }
        cursor.close();
        return lastId;
    }

    // Thay thế saveServerUrl() và loadServerUrl()
    public void saveServerUrl(String serverUrl) {
        ContentValues values = new ContentValues();
        values.put("key", "server_url");
        values.put("value", serverUrl);
        database.insertWithOnConflict("settings", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String loadServerUrl() {
        Cursor cursor = database.query("settings", new String[]{"value"},
                "key = ?", new String[]{"server_url"}, null, null, null);
        String serverUrl = "http://192.168.1.157:5000"; // Default
        if (cursor.moveToFirst()) {
            serverUrl = cursor.getString(0);
        }
        cursor.close();
        return serverUrl;
    }
    public void clearAllPlants() {
        database.delete("plants", null, null);
        Log.d("PlantDAO", "All plants cleared from database");
    }
}