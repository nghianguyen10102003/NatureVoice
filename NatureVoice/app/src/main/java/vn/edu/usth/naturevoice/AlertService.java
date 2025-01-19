package vn.edu.usth.naturevoice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class AlertService extends Service {

    private static final String TAG = "AlertService";
    private Socket mSocket;
    private ArrayList<Plant> plantList;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AlertService started");
        initializeSocket();
    }

    private void initializeSocket() {
        mSocket = SocketSingleton.getInstance(this);
        if (!SocketSingleton.isConnected()) {
            mSocket.connect();
        }
        mSocket.on("alert", onAlert);
        Log.d(TAG, "Socket initialized in AlertService");
    }

    private void reinitializeSocket() {
        if (mSocket != null) {
            mSocket.off("alert", onAlert);
            mSocket.disconnect();
            mSocket = null;
        }

        mSocket = SocketSingleton.getInstance(this);
        if (!SocketSingleton.isConnected()) {
            mSocket.connect();
        }
        mSocket.on("alert", onAlert);
        Log.d(TAG, "Socket reinitialized in AlertService");
    }

    private final Emitter.Listener onAlert = args -> {
        try {
            JSONObject data = (JSONObject) args[0];
            Log.d("AlertService", "Received alert data: " + data.toString());

            if (data.has("id")) {
                int id = data.getInt("id");
                String type = data.optString("type", "default");
                String message = data.optString("message", "No message");

                Log.d("AlertService", "Parsed alert: id=" + id + ", type=" + type + ", message=" + message);

                Intent intent = new Intent("vn.edu.usth.naturevoice.UPDATE_UI");
                intent.putExtra("id", id);
                intent.putExtra("type", type);
                intent.putExtra("message", message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            } else {
                Log.e("AlertService", "Alert data does not contain 'id'");
            }
        } catch (JSONException e) {
            Log.e("AlertService", "Error parsing alert data", e);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AlertService is running");

        if (intent != null && intent.hasExtra("plant_list")) {
            plantList = (ArrayList<Plant>) intent.getSerializableExtra("plant_list");
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AlertService stopped");

        if (mSocket != null) {
            mSocket.off("alert", onAlert);
            mSocket.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
