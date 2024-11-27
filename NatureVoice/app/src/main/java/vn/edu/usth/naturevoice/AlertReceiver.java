package vn.edu.usth.naturevoice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        String type = intent.getStringExtra("type");
        String message = intent.getStringExtra("message");

        Log.d("AlertReceiver", "Received broadcast: id=" + id + ", type=" + type + ", message=" + message);

        // Bạn có thể cập nhật giao diện hoặc xử lý dữ liệu tại đây.
    }
}
