package vn.edu.usth.naturevoice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import vn.edu.usth.naturevoice.PlantDAO;
import vn.edu.usth.naturevoice.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private Socket mSocket;
    private ArrayList<Plant> plantList = new ArrayList<>();
    private ArrayList<Integer> noti_list = new ArrayList<>();
    private PlantDAO plantDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize PlantDAO
        plantDAO = new PlantDAO(this);
        plantDAO.open();

        MyApplication app = (MyApplication) getApplication();
        if (!app.hasRunOnce()) {
            clearPlantList(); // Chỉ xóa khi ứng dụng chạy lần đầu
            app.setHasRunOnce(true); // Đánh dấu đã chạy
            Log.d(TAG, "First time running the app. Database cleared.");
        }

        int lastPlantId = loadLastPlantId();
        Log.d(TAG, "Last Plant ID: " + lastPlantId);

        // Socket
        mSocket = SocketSingleton.getInstance(this);
        if (!SocketSingleton.isConnected()) {
            mSocket.connect();
        }

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);

        Intent serviceIntent = new Intent(this, AlertService.class);
        serviceIntent.putExtra("plant_list", plantList);
        startService(serviceIntent);

        // Load plantList from SQLite database
        loadPlantList();

        Intent intent = getIntent();
        Plant plant = (Plant) intent.getSerializableExtra("plant_data");

        if (plant != null) {
            checkLog("Received plant data: Name: " + plant.getName() +
                    " tinh cach: " + plant.getSpecies() +
                    " ID chau: " + plant.getPotId() +
                    " Id cay: " + plant.getId());

            // Add the Plant object to the ArrayList
            addPlantToList(plant);

            // Save updated plantList
            savePlantList();

            // Notify HomeFragment to refresh the view
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.main);
            if (homeFragment instanceof HomeFragment) {
                homeFragment.refreshImages();
            } else {
                replaceFragment(new HomeFragment());
            }
        } else {
            checkLog("No plant data found in Intent. Loading default HomeFragment.");
            replaceFragment(new HomeFragment());
        }

        // Bottom nav bar
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close database connection
        if (plantDAO != null) {
            plantDAO.close();
        }

        Socket mSocket = SocketSingleton.getInstance(this);
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

    // Plant ID management using SQLite
    private void saveLastPlantId(int plantId) {
        plantDAO.saveLastPlantId(plantId);
    }

    private int loadLastPlantId() {
        return plantDAO.loadLastPlantId();
    }

    public int getNextPlantId() {
        int nextPlantId = loadLastPlantId() + 1;
        saveLastPlantId(nextPlantId);
        return nextPlantId;
    }

    /**
     * Replace the current fragment with the specified fragment.
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Logs a message for debugging purposes.
     */
    private void checkLog(String message) {
        Log.d(TAG, message);
    }

    /**
     * Adds a Plant object to the plantList and logs the updated list.
     */
    private void addPlantToList(Plant plant) {
        plantList.add(plant);
        savePlantList();
        int plantCount = plantList.size();
        checkLog("Plant added to list. Total plants in list: " + plantCount);
    }

    /**
     * Gets the list of all stored Plant objects.
     */
    public ArrayList<Plant> getPlantList() {
        return plantList;
    }

    /**
     * Save the plantList to SQLite database.
     */
    private void savePlantList() {
        plantDAO.savePlantList(plantList);
        checkLog("Plant list saved to SQLite database.");
    }

    /**
     * Load the plantList from SQLite database.
     */
    private void loadPlantList() {
        plantList = plantDAO.loadPlantList();
        checkLog("Plant list loaded from SQLite database. Total plants: " + plantList.size());
    }

    private void clearPlantList() {
        plantDAO.clearAllPlants();
        plantList.clear();
        Log.d(TAG, "All plant data cleared from database.");
    }

    // Server URL management using SQLite
    public void saveServerUrl(String serverUrl) {
        plantDAO.saveServerUrl(serverUrl);
        // Reset and reinitialize the socket with the new URL
        SocketSingleton.resetInstance();
        SocketSingleton.getInstance(this).connect();
        Log.d(TAG, "Server URL updated to: " + serverUrl);
    }

    public String loadServerUrl() {
        return plantDAO.loadServerUrl();
    }

    public void restartAlertService() {
        Intent serviceIntent = new Intent(this, AlertService.class);
        stopService(serviceIntent);
        startService(serviceIntent);
        Log.d(TAG, "AlertService restarted");
    }
}