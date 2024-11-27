package vn.edu.usth.naturevoice;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
                if (sensorDataLayout.getVisibility() == View.VISIBLE) {
                    // Hiệu ứng trượt xuống khi ẩn
                    Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_down);
                    slideOut.setAnimationListener(new Animation.AnimationListener() {
                        public void onAnimationStart(Animation animation) {
                            // Không cần xử lý
                        }

                        public void onAnimationEnd(Animation animation) {
                            sensorDataLayout.setVisibility(View.GONE);
                        }

                        public void onAnimationRepeat(Animation animation) {
                            // Không cần xử lý
                        }
                    });
                    sensorDataLayout.startAnimation(slideOut);
                } else {
                    // Hiển thị trước khi bắt đầu hiệu ứng
                    sensorDataLayout.setVisibility(View.VISIBLE);
                    // Hiệu ứng trượt lên khi hiển thị
                    Animation slideIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_up);
                    sensorDataLayout.startAnimation(slideIn);
                }
            }
        });


        // Set tree image and name from intent
        ImageView treeImage = findViewById(R.id.plant_image);
        TextView treeName = findViewById(R.id.plant_name);
        ImageView tree_avatar = findViewById(R.id.plant_avatar);

        int imageResId = getIntent().getIntExtra("tree_image", R.drawable.ava);
        String name = getIntent().getStringExtra("tree_name");
        int tree_ava = getIntent().getIntExtra("tree_avatar", R.drawable.ava);
        treeImage.setImageResource(imageResId);
        treeName.setText(name);
        tree_avatar.setImageResource(tree_ava);
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
