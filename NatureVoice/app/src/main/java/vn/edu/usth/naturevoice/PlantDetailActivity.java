package vn.edu.usth.naturevoice;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    private LinearLayout sensorDataLayout;
    private LinearLayout detailchat;



    private static final String API_URL = "http://192.168.1.10:5000/api/plant_chat";
    ChatHistory chatHistory = new ChatHistory();
    private EditText inputMessage;
    private Button sendButton;
    private TextView chatResponse;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_detail);
        EdgeToEdge.enable(this);

        // Initialize views
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        lightText = findViewById(R.id.lightText);
        sensorDataLayout = findViewById(R.id.sensor_data);
        detailchat = findViewById(R.id.detail_chat);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        chatResponse = findViewById(R.id.chatResponse);
        scrollView = findViewById(R.id.scrollView);
         // Root layout của giao diện

        // Đặt OnTouchListener để phát hiện khi người dùng nhấn ra ngoài


        detailchat.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                detailchat.getWindowVisibleDisplayFrame(r);

                int screenHeight = detailchat.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    // Bàn phím xuất hiện
                    detailchat.setTranslationY(-keypadHeight); // Đẩy toàn bộ giao diện lên
                } else {
                    // Bàn phím ẩn
                    detailchat.setTranslationY(0); // Đưa giao diện trở về vị trí cũ
                }
            }
        });



        // Scroll to bottom initially
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

        // Set up socket connection
        mSocket = SocketSingleton.getInstance(this);
        if (mSocket != null) {
            mSocket.on("sensor_data", onSensorData);
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = inputMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendChatMessage(message);
                    chatHistory.addUserMessage(message); // Add user message
                    updateChatResponse(); // Update chat UI
                    scrollToBottom(); // Scroll to the bottom
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
                Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_down);
                Animation slideIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_up);
                Animation zoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
                Animation zoomOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);
                if (sensorDataLayout.getVisibility() == View.VISIBLE) {
                    slideOut.setAnimationListener(new Animation.AnimationListener() {
                        public void onAnimationStart(Animation animation) {}

                        public void onAnimationEnd(Animation animation) {
                            sensorDataLayout.setVisibility(View.GONE);
                        }

                        public void onAnimationRepeat(Animation animation) {}
                    });
                    sensorDataLayout.startAnimation(slideOut);
                    detailchat.startAnimation(slideIn);
                    statusImage.startAnimation(zoomOut);
                } else {
                    sensorDataLayout.setVisibility(View.VISIBLE);
                    statusImage.startAnimation(zoomIn);
                    sensorDataLayout.startAnimation(slideIn);
                    detailchat.startAnimation(slideOut);
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
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null) {
                hideKeyboard(this);
            }
        }
        return super.dispatchTouchEvent(event);
    }
    private void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private final Emitter.Listener onSensorData = args -> runOnUiThread(() -> {
        if (args.length > 0) {
            try {
                JSONObject data = (JSONObject) args[0];
                double temperature = data.getDouble("temperature");
                double humidity = data.getDouble("humidity");
                double light = data.getDouble("light");

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

                        if (jsonResponse.has("response")) {
                            String serverResponse = jsonResponse.getString("response");
                            runOnUiThread(() -> {
                                chatHistory.addBotMessage(serverResponse);
                                updateChatResponse();
                                scrollToBottom(); // Scroll to bottom after receiving response
                                inputMessage.setText(""); // Clear the input field
                            });
                        }
                    } catch (JSONException e) {
                        Log.e("API", "Error parsing server response", e);
                    }
                }
            }
        });
    }

    private void updateChatResponse() {
        StringBuilder chatDisplay = new StringBuilder();
        List<String> userMessages = chatHistory.getUserMessages();
        List<String> botMessages = chatHistory.getBotMessages();

        int maxLength = Math.max(userMessages.size(), botMessages.size());

        for (int i = 0; i < maxLength; i++) {
            if (i < userMessages.size()) {
                chatDisplay.append("User: ").append(userMessages.get(i)).append("\n");
            }
            if (i < botMessages.size()) {
                chatDisplay.append("Bot: ").append(botMessages.get(i)).append("\n");
            }
        }

        chatResponse.setText(chatDisplay.toString());
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }
}
