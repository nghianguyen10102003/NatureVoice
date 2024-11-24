package vn.edu.usth.naturevoice_socket;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String SOCKET_SERVER_URL = "http://192.168.1.76:5000"; // Replace with your server's IP
    private static final String API_URL = "http://192.168.1.76:5000/api/plant_chat"; // Flask API URL
    private Socket mSocket;

    private TextView temperatureText;
    private TextView humidityText;
    private TextView lightText;
    private EditText inputMessage;
    private Button sendButton;
    private TextView chatResponse;
    private TextView alert_plant;

    ChatHistory chatHistory = new ChatHistory();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        lightText = findViewById(R.id.lightText);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        chatResponse = findViewById(R.id.chatResponse);
        alert_plant = findViewById(R.id.alert_plant);

        try {
            // Configure socket options
            IO.Options options = new IO.Options();
            options.query = "client_id=client_1234"; // Attach client_id in query string

            // Initialize socket with options
            mSocket = IO.socket(SOCKET_SERVER_URL, options);

            // Add socket event listeners
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            mSocket.on("sensor_data", onSensorData);
            mSocket.on("alert", onAlert);

            // Connect to the server
            mSocket.connect();
        } catch (Exception e) {
            Log.e("SocketIO", "Error initializing socket with query", e);
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = inputMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendChatMessage(message);
                    chatHistory.addUserMessage(message); // Send message to the server
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Demo: Send chat message to the server
        //sendChatMessage("hello");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            // Disconnect and remove listeners
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT, onConnect);
            mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
            mSocket.off("sensor_data", onSensorData);
            mSocket.off("alert", onAlert);
        }
    }

    // Event listener: Connection established
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

    // Event listener: Receive sensor data
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

//                Toast.makeText(MainActivity.this,
//                        "Temp: " + temperature + ", Humidity: " + humidity + ", Light: " + light,
//                        Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Log.e("SocketIO", "Error parsing sensor data", e);
            }
        }
    });

    // Event listener: Receive alert message
    private final Emitter.Listener onAlert = args -> runOnUiThread(() -> {
        if (args.length > 0) {
            try {
                JSONObject data = (JSONObject) args[0];
                String type = data.getString("type");
                String message = data.getString("message");
                Log.d("SocketIO", "Alert received: " + type + " - " + message);
                alert_plant.setText("Alert:" + type + message);
                //Toast.makeText(MainActivity.this, "ALERT: " + message, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Log.e("SocketIO", "Error parsing alert data", e);
            }
        }
    });

    // Method to send chat message using Flask API
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
    // In toàn bộ nội dung của chatHistory vào chatResponse
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
