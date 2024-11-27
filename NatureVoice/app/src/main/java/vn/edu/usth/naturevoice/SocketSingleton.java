package vn.edu.usth.naturevoice;

import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketSingleton {
    private static final String SOCKET_SERVER_URL = "http://192.168.1.117:5000";
    private static io.socket.client.Socket mSocket;

    // Private constructor để ngăn khởi tạo từ bên ngoài
    private SocketSingleton() {}

    // Khởi tạo Socket
    public static Socket getInstance() {
        if (mSocket == null) {
            try {
                IO.Options options = new IO.Options();
                options.query = "client_id=client_1234";
                mSocket = IO.socket(SOCKET_SERVER_URL, options);
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
}
