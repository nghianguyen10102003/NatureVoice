package vn.edu.usth.naturevoice;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PlantDetailActivity extends AppCompatActivity {

    private Socket mSocket;
    private TextView temperatureText;
    private TextView humidityText;
    private TextView lightText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plant_detail);
        mSocket = SocketSingleton.getInstance();
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        lightText = findViewById(R.id.lightText);
//        inputMessage = findViewById(R.id.inputMessage);

        if (mSocket != null) {
            mSocket.on("sensor_data", onSensorData);
        }
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
                temperatureText.setText("Temperature: " + String.format("%.2f", temperature) + " °C");
                humidityText.setText("Humidity: " + String.format("%.2f", humidity) + " %");
                lightText.setText("Light: " + String.format("%.2f", light) + " lx");

//                Toast.makeText(PlantDetailActivity.this,
//                        "Temp: " + temperature + ", Humidity: " + humidity + ", Light: " + light,
//                        Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Log.e("SocketIO", "Error parsing sensor data", e);
            }
        }
    });


}