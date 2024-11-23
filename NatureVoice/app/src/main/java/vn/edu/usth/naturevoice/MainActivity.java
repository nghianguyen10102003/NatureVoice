package vn.edu.usth.naturevoice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import vn.edu.usth.naturevoice.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "PlantPrefs";
    private static final String PLANT_LIST_KEY = "plant_list";

    private ArrayList<Plant> plantList = new ArrayList<>(); // ArrayList to store Plant objects

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load plantList from SharedPreferences
        loadPlantList();

        Intent intent = getIntent();
        Plant plant = (Plant) intent.getSerializableExtra("plant_data");

        if (plant != null) {
            checkLog("Received plant data: " + plant.getName() + ", ID: " + plant.getPlantId());

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
