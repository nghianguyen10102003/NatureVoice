package vn.edu.usth.naturevoice;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketSingleton {
    private static io.socket.client.Socket mSocket;
    private static final String PREFS_NAME = "ServerPrefs";
    private static final String SERVER_URL_KEY = "server_url";
    private static final String DEFAULT_URL = "http://192.168.1.157:5000";

    // Private constructor để ngăn khởi tạo từ bên ngoài
    private SocketSingleton() {}

    // Khởi tạo Socket với server URL từ SharedPreferences
    public static Socket getInstance(Context context) {
        if (mSocket == null) {
            try {
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String serverUrl = prefs.getString(SERVER_URL_KEY, DEFAULT_URL);

                IO.Options options = new IO.Options();
                options.query = "client_id=client_1234";
                mSocket = IO.socket(serverUrl, options);
            } catch (Exception e) {
                Log.e("SocketSingleton", "Error initializing socket", e);
            }
        }
        return mSocket;
    }

    // Phương thức để kiểm tra trạng thái kết nối
    public static boolean isConnected() {
        return mSocket != null && mSocket.connected();
    }

    // Reset the socket instance to allow reinitialization
    public static void resetInstance() {
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket = null;
        }
    }
}
