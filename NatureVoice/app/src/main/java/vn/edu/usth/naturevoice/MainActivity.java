package vn.edu.usth.naturevoice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import vn.edu.usth.naturevoice.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "PlantPrefs";
    private static final String PLANT_LIST_KEY = "plant_list";
    private static final String LAST_PLANT_ID_KEY = "last_plant_id";
    private Socket mSocket;
    private ArrayList<Plant> plantList = new ArrayList<>(); // ArrayList to store Plant objects
    private ArrayList<Integer> noti_list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int lastPlantId = loadLastPlantId();
        Log.d(TAG, "Last Plant ID: " + lastPlantId);
        // Socket
        mSocket = SocketSingleton.getInstance();
        if (!SocketSingleton.isConnected()) {
            mSocket.connect();
        }

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
//        mSocket.on("alert", onAlert);

        Intent serviceIntent = new Intent(this, AlertService.class);
        serviceIntent.putExtra("plant_list", plantList); // Pass the plant list to the service
        startService(serviceIntent);


        // Load plantList from SharedPreferences
        loadPlantList();

        Intent intent = getIntent();
        Plant plant = (Plant) intent.getSerializableExtra("plant_data");

        if (plant != null) {
            checkLog("Received plant data:Name: " + plant.getName() +"tinh cach :" + plant.getSpecies() +"ID chau:"+plant.getPotId() + "Id cay"+plant.getId());

            // Add the Plant object to the ArrayList
            addPlantToList(plant);

            // Save updated plantList
            savePlantList();

            // Notify HomeFragment to refresh the view
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.main);
            if (homeFragment instanceof HomeFragment) {
                homeFragment.refreshImages(); // Update HomeFragment
            } else {
                replaceFragment(new HomeFragment());
            }
        } else {
            checkLog("No plant data found in Intent. Loading default HomeFragment.");
            replaceFragment(new HomeFragment()); // Default HomeFragment if no data
        }



        //Bottom nav bar
        binding.BottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.newPlant) {
                replaceFragment(new NewPlantFragment());
                return true;
            } else if (itemId == R.id.camera) {
                replaceFragment(new CameraFragment());
                return true;
            } else if (itemId == R.id.menu) {
                replaceFragment(new MenuFragment());
                return true;
            }
            return false;
        });

        EdgeToEdge.enable(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        Socket mSocket = SocketSingleton.getInstance();
        if (mSocket != null) {
            // Disconnect and remove listeners
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT, onConnect);
            mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);

        }
    }



    private final Emitter.Listener onConnect = args -> runOnUiThread(() -> {
        Log.d("SocketIO", "Connected to server");
        Toast.makeText(MainActivity.this, "Connected to server", Toast.LENGTH_SHORT).show();
        // Request sensor data from the server
        mSocket.emit("request_sensor_data");
    });

    // Event listener: Disconnected from server
    private final Emitter.Listener onDisconnect = args -> runOnUiThread(() -> {
        Log.d("SocketIO", "Disconnected from server");
        Toast.makeText(MainActivity.this, "Disconnected from server", Toast.LENGTH_SHORT).show();
    });

//    private final Emitter.Listener onSensorData = args -> runOnUiThread(() -> {
//        if (args.length > 0) {
//            try {
//                JSONObject data = (JSONObject) args[0];
//                double temperature = data.getDouble("temperature");
//                double humidity = data.getDouble("humidity");
//                double light = data.getDouble("light");
//                Log.d("SocketIO", "Sensor data received: " +
//                        "Temperature: " + temperature +
//                        ", Humidity: " + humidity +
//                        ", Light: " + light);
////                temperatureText.setText("Temperature: " + String.format("%.2f", temperature) + " °C");
////                humidityText.setText("Humidity: " + String.format("%.2f", humidity) + " %");
////                lightText.setText("Light: " + String.format("%.2f", light) + " lx");
//
////                Toast.makeText(MainActivity.this,
////                        "Temp: " + temperature + ", Humidity: " + humidity + ", Light: " + light,
////                        Toast.LENGTH_SHORT).show();
//            } catch (JSONException e) {
//                Log.e("SocketIO", "Error parsing sensor data", e);
//            }
//        }
//    });

//    private final Emitter.Listener onAlert = args -> runOnUiThread(() -> {
//        if (args.length > 0) {
//            try {
//                JSONObject data = (JSONObject) args[0];
//                String type = data.getString("type");
//                String message = data.getString("message");
//                Log.d("SocketIO", "Alert received: " + type + " - " + message);
//                alert_plant.setText("Alert:" + type + message);
//
//                //Toast.makeText(MainActivity.this, "ALERT: " + message, Toast.LENGTH_LONG).show();
//
//            } catch (JSONException e) {
//                Log.e("SocketIO", "Error parsing alert data", e);
//            }
//        }
//    });

    private void saveLastPlantId(int plantId) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LAST_PLANT_ID_KEY, plantId);
        editor.apply();
    }

    // Đọc id cây cuối cùng từ SharedPreferences
    private int loadLastPlantId() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getInt(LAST_PLANT_ID_KEY, 0); // Giá trị mặc định là 0 nếu chưa có
    }
    public int getNextPlantId() {
        // Lấy id cây tiếp theo từ SharedPreferences
        int nextPlantId = loadLastPlantId() + 1;  // Tăng lên 1
        saveLastPlantId(nextPlantId);  // Lưu lại id cây tiếp theo
        return nextPlantId;
    }
    /**
     * Replace the current fragment with the specified fragment.
     *
     * @param fragment The fragment to replace the current fragment with.
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Logs a message for debugging purposes.
     *
     * @param message The message to be logged.
     */
    private void checkLog(String message) {
        Log.d(TAG, message); // Debug log
    }

    /**
     * Adds a Plant object to the plantList and logs the updated list.
     *
     * @param plant The Plant object to add.
     */
    private void addPlantToList(Plant plant) {
        plantList.add(plant); // Add the Plant object to the list
        int plantCount = plantList.size(); // Get the current number of Plant objects
        checkLog("Plant added to list. Total plants in list: " + plantCount);
    }

    /**
     * Gets the list of all stored Plant objects.
     *
     * @return The ArrayList containing all Plant objects.
     */
    public ArrayList<Plant> getPlantList() {
        return plantList;
    }

    /**
     * Save the plantList to SharedPreferences.
     */
    private void savePlantList() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(plantList);
        editor.putString(PLANT_LIST_KEY, json);
        editor.apply();
        checkLog("Plant list saved to SharedPreferences.");
    }

    /**
     * Load the plantList from SharedPreferences.
     */
    private void loadPlantList() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(PLANT_LIST_KEY, null);
        Type type = new TypeToken<ArrayList<Plant>>() {}.getType();
        plantList = gson.fromJson(json, type);

        if (plantList == null) {
            plantList = new ArrayList<>();
        }
        checkLog("Plant list loaded from SharedPreferences. Total plants: " + plantList.size());
    }
}
