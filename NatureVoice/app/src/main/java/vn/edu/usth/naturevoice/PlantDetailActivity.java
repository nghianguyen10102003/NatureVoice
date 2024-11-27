package vn.edu.usth.naturevoice;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlantDetailActivity extends AppCompatActivity {

    private Socket mSocket;
    private TextView temperatureText;
    private TextView humidityText;
    private TextView lightText;
    private LinearLayout sensorDataLayout;  // Add this line

    private static final String API_URL = "http://192.168.1.140:5000/api/plant_chat";
    ChatHistory chatHistory = new ChatHistory();
    private EditText inputMessage;
    private Button sendButton;
    private TextView chatResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_detail);

        // Initialize views
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        lightText = findViewById(R.id.lightText);
        sensorDataLayout = findViewById(R.id.sensor_data);  // Initialize sensor data layout

        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        chatResponse = findViewById(R.id.chatResponse);

        // Set up socket connection
        mSocket = SocketSingleton.getInstance();
        if (mSocket != null) {
            mSocket.on("sensor_data", onSensorData);
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = inputMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendChatMessage(message);
                    chatHistory.addUserMessage(message); // Send message to the server
                } else {
                    Toast.makeText(PlantDetailActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

    private void sendChatMessage(String message) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("message", message);
        } catch (JSONException e) {
            Log.e("API", "Error creating JSON", e);
        }

        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API", "Failed to send chat message", e);
                runOnUiThread(() -> chatResponse.setText("Error: Could not connect to server"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String serverResponse = jsonResponse.getString("response");

                        Log.d("API", "Chat response: " + serverResponse);
                        runOnUiThread(() -> {

                            chatHistory.addBotMessage(serverResponse);
                            // Update UI with response
                            updateChatResponse();
                            inputMessage.setText(""); // Clear the input field
                        });
                    } catch (JSONException e) {
                        Log.e("API", "Error parsing server response", e);
                    }
                } else {
                    Log.e("API", "Failed to get valid response");
                    runOnUiThread(() -> chatResponse.setText("Error: Invalid response from server"));
                }
            }
        });
    }
    private void updateChatResponse() {
        StringBuilder chatDisplay = new StringBuilder();

        // Lấy danh sách tin nhắn user và bot
        List<String> userMessages = chatHistory.getUserMessages();
        List<String> botMessages = chatHistory.getBotMessages();

        int maxLength = Math.max(userMessages.size(), botMessages.size());

        // Duyệt qua danh sách xen kẽ user và bot
        for (int i = 0; i < maxLength; i++) {
            if (i < userMessages.size()) {
                chatDisplay.append("User: ").append(userMessages.get(i)).append("\n");
            }
            if (i < botMessages.size()) {
                chatDisplay.append("Bot: ").append(botMessages.get(i)).append("\n");
            }
        }

        // Cập nhật chatResponse để hiển thị toàn bộ lịch sử
        chatResponse.setText(chatDisplay.toString());
    }
}
