package vn.edu.usth.naturevoice;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PlantDetailActivity extends AppCompatActivity {

    private Socket mSocket;
    private TextView temperatureText;
    private TextView humidityText;
    private TextView lightText;
    private LinearLayout sensorDataLayout;  // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_detail);

        // Initialize views
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        lightText = findViewById(R.id.lightText);
        sensorDataLayout = findViewById(R.id.sensor_data);  // Initialize sensor data layout

        // Set up socket connection
        mSocket = SocketSingleton.getInstance();
        if (mSocket != null) {
            mSocket.on("sensor_data", onSensorData);
        }

        // Set up the click event for status ImageView
        ImageView statusImage = findViewById(R.id.status);
        statusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle visibility of sensor data layout
                if (sensorDataLayout.getVisibility() == View.VISIBLE) {
                    sensorDataLayout.setVisibility(View.GONE);  // Hide it
                } else {
                    sensorDataLayout.setVisibility(View.VISIBLE);  // Show it
                }
            }
        });

        // Set tree image and name from intent
        ImageView treeImage = findViewById(R.id.plant_image);
        TextView treeName = findViewById(R.id.plant_name);

        int imageResId = getIntent().getIntExtra("tree_image", R.drawable.ava);
        String name = getIntent().getStringExtra("tree_name");

        treeImage.setImageResource(imageResId);
        treeName.setText(name);
    }

    private final Emitter.Listener onSensorData = args -> runOnUiThread(() -> {
        if (args.length > 0) {
            try {
                JSONObject data = (JSONObject) args[0];
                double temperature = data.getDouble("temperature");
                double humidity = data.getDouble("humidity");
                double light = data.getDouble("light");
                Log.d("SocketIO", "Sensor data received: " +
                        "Temperature: " + temperature +
                        ", Humidity: " + humidity +
                        ", Light: " + light);

                // Update sensor data text
                temperatureText.setText("Temperature: " + String.format("%.2f", temperature) + " °C");
                humidityText.setText("Humidity: " + String.format("%.2f", humidity) + " %");
                lightText.setText("Light: " + String.format("%.2f", light) + " lx");
            } catch (JSONException e) {
                Log.e("SocketIO", "Error parsing sensor data", e);
            }
        }
    });
}
