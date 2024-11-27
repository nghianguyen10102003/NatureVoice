package vn.edu.usth.naturevoice;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    private LinearLayout linearLayout;
    private Socket mSocket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        linearLayout = view.findViewById(R.id.image_container);
        mSocket = SocketSingleton.getInstance();
        if (!SocketSingleton.isConnected()) {
            mSocket.connect();
        }
        IntentFilter filter = new IntentFilter("vn.edu.usth.naturevoice.UPDATE_UI");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(updateReceiver, filter);

//        mSocket.on("alert", onAlert);
        // Refresh images based on plant list
        refreshImages();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateReceiver);

    }

    /**
     * Refresh the image container using the plant list from MainActivity.
     */
    void refreshImages() {
        linearLayout.removeAllViews();

        // Get the plant list from MainActivity
        ArrayList<Plant> plantList = ((MainActivity) requireActivity()).getPlantList();

        if (plantList != null && !plantList.isEmpty()) {
            for (Plant plant : plantList) {
                addImageView(plant);
            }
        }
    }

    /**
     * Add an ImageView for a specific Plant object.
     *
     * @param plant The Plant object to display.
     */


    private void addImageView(Plant plant) {
        // Tạo một FrameLayout để chứa toàn bộ cây và thông báo
        FrameLayout plantContainer = new FrameLayout(getActivity());
        plantContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        plantContainer.setPadding(10, 10, 10, 10);

        // Tạo LinearLayout ngang để chứa icon và tên cây
        LinearLayout horizontalLayout = new LinearLayout(getActivity());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams horizontalLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        horizontalLayout.setLayoutParams(horizontalLayoutParams);

        // Tạo ImageView cho icon cây
        ImageView imageView = new ImageView(getActivity());
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(250, 250); // Kích thước icon
        imageView.setLayoutParams(imageLayoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        // Gán icon dựa trên thuộc tính của cây
        String selectIcon = "i" + plant.getPlantId() + "_p" + plant.getPotId();
        switch (selectIcon) {
            case "i1_p1":
                imageView.setImageResource(R.drawable.cay1chau1);
                break;
            case "i1_p2":
                imageView.setImageResource(R.drawable.cay1chau2);
                break;
            case "i1_p3":
                imageView.setImageResource(R.drawable.cay1chau3);
                break;
            case "i2_p1":
                imageView.setImageResource(R.drawable.cay2chau1);
                break;
            case "i2_p2":
                imageView.setImageResource(R.drawable.cay2chau2);
                break;
            case "i2_p3":
                imageView.setImageResource(R.drawable.cay2chau3);
                break;
            case "i3_p1":
                imageView.setImageResource(R.drawable.cay3chau1);
                break;
            case "i3_p2":
                imageView.setImageResource(R.drawable.cay3chau2);
                break;
            case "i3_p3":
                imageView.setImageResource(R.drawable.cay3chau3);
                break;
            default:
                imageView.setImageResource(R.drawable.ava);
                break;
        }

        // Thêm sự kiện khi nhấn vào icon cây
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PlantDetailActivity.class);
            intent.putExtra("tree_name", plant.getName());
            intent.putExtra("tree_image", getImageResourceForPlant(plant));
            startActivity(intent);
        });

        // Tạo TextView cho tên cây
        TextView nameTextView = new TextView(getActivity());
        LinearLayout.LayoutParams nameLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        nameLayoutParams.setMargins(100, 0, 0, 0); // Khoảng cách giữa icon và tên cây
        nameTextView.setLayoutParams(nameLayoutParams);
        nameTextView.setText(plant.getName());
        nameTextView.setTextSize(16); // Kích thước chữ
        nameTextView.setTextColor(getResources().getColor(R.color.black)); // Màu chữ
        nameTextView.setGravity(Gravity.CENTER_VERTICAL);

        // Thêm ImageView và TextView vào LinearLayout ngang
        horizontalLayout.addView(imageView);
        horizontalLayout.addView(nameTextView);

        // Tạo bubble TextView để hiển thị thông báo
        TextView bubbleTextView = new TextView(getActivity());
        FrameLayout.LayoutParams bubbleLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        bubbleLayoutParams.setMargins(180, -10, 0, 0); // Đặt vị trí thông báo (bên phải icon cây)
        bubbleTextView.setLayoutParams(bubbleLayoutParams);
        bubbleTextView.setBackgroundResource(R.drawable.bubble_background); // Hình nền bubble
        bubbleTextView.setTextSize(12);
        bubbleTextView.setTextColor(getResources().getColor(R.color.black));
        bubbleTextView.setPadding(10, 5, 10, 5);

        // Hiển thị nội dung thông báo từ Plant
        if (plant.getNoti_message() != null && !plant.getNoti_message().isEmpty() && !plant.getNoti_type().equals("default")) {
            bubbleTextView.setText(plant.getNoti_type());
            bubbleTextView.setVisibility(View.VISIBLE);
        } else {
            bubbleTextView.setVisibility(View.GONE);
        }
        // Lưu bubbleTextView vào Plant
        plant.setBubbleTextView(bubbleTextView);

        // Thêm LinearLayout và bubbleTextView vào FrameLayout
        plantContainer.addView(horizontalLayout);
        plantContainer.addView(bubbleTextView);

        // Thêm FrameLayout vào LinearLayout chính
        linearLayout.addView(plantContainer);
    }


    private int getImageResourceForPlant(Plant plant) {
        String selectIcon = "i" + plant.getPlantId() + "_p" + plant.getPotId();
        switch (selectIcon) {
            case "i1_p1":
                return R.drawable.cay1chau1;
            case "i1_p2":
                return R.drawable.cay1chau2;
            case "i1_p3":
                return R.drawable.cay1chau3;
            case "i2_p1":
                return R.drawable.cay2chau1;
            case "i2_p2":
                return R.drawable.cay2chau2;
            case "i2_p3":
                return R.drawable.cay2chau3;
            case "i3_p1":
                return R.drawable.cay3chau1;
            case "i3_p2":
                return R.drawable.cay3chau2;
            case "i3_p3":
                return R.drawable.cay3chau3;
            default:
                return R.drawable.ava;
        }
    }


    //    private final Emitter.Listener onAlert = args -> {
//        if (args.length > 0) {
//            try {
//                JSONObject data = (JSONObject) args[0];
//                int id = data.getInt("id");
//                String type = data.getString("type");
//                String message = data.getString("message");
//
//                // Chuyển sang UI thread
//
//                Log.d("SocketIO", "Alert received: " + id + " - " + type + " - " + message);
//
//                    // Tìm cây có id tương ứng
//                ArrayList<Plant> plantList = ((MainActivity) requireActivity()).getPlantList();
//                for (Plant plant : plantList) {
//                    if (plant.getPlantId() == id) {
//                        plant.setNoti_type(type);
//                        plant.setNoti_message(message);
//                    }
//                }
//                requireActivity().runOnUiThread(this::refreshImages);
//
//            } catch (JSONException e) {
//                Log.e("SocketIO", "Error parsing alert data", e);
//            }
//
//        }
//
//    };
    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("id", -1);
            String type = intent.getStringExtra("type");
            String message = intent.getStringExtra("message");

            Log.d(TAG, "Received alert update: " + id + " - " + type + " - " + message);

            // Cập nhật danh sách cây
            ArrayList<Plant> plantList = ((MainActivity) requireActivity()).getPlantList();
            if (plantList != null) {
                for (Plant plant : plantList) {
                    if (plant.getPlantId() == id) {
                        plant.setNoti_type(type);
                        plant.setNoti_message(message);
                    }
                }
            }

            // Làm mới giao diện
            refreshImages();
        }
    };
}