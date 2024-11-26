package vn.edu.usth.naturevoice;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        mSocket.on("alert", onAlert);
        // Refresh images based on plant list
        refreshImages();
        return view;
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
        ImageView imageView = new ImageView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(170, 170);

        // Set margins for the LayoutParams
        layoutParams.setMargins(0, 0, 0, 20); // Left, Top, Right, Bottom

        // Apply the LayoutParams to the ImageView
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        // Select the appropriate image based on plant attributes
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

                break;
        }
        imageView.setOnClickListener(v -> {
            // Mở màn hình chi tiết cây và truyền thông tin cây
            Intent intent = new Intent(getActivity(), PlantDetailActivity.class);

            intent.putExtra("tree_name", plant.getName());
            intent.putExtra("tree_image", getImageResourceForPlant(plant));
            startActivity(intent);
        });

        linearLayout.addView(imageView);
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
    private final Emitter.Listener onAlert = args -> {
        if (args.length > 0) {
            try {
                JSONObject data = (JSONObject) args[0];
                int id = data.getInt("id");
                String type = data.getString("type");
                String message = data.getString("message");

                // Chuyển sang UI thread bằng requireActivity().runOnUiThread()
                requireActivity().runOnUiThread(() -> {
                    Log.d("SocketIO", "Alert received: " + id +" - " + type + " - " + message);

                    // Ví dụ: Cập nhật giao diện
                    if (id == 1) {
                        TextView alertPlantTextView = requireView().findViewById(R.id.note1);
                        alertPlantTextView.setText("Note1:" + type + " - " + message);
                    } else if(id == 2){
                        TextView alertPlantTextView = requireView().findViewById(R.id.note2);
                        alertPlantTextView.setText("Note2: " + type + " - " + message);
                    }else if(id == 3){
                        TextView alertPlantTextView = requireView().findViewById(R.id.note3);
                        alertPlantTextView.setText("Note3: " + type + " - " + message);
                    }else if(id == 4){
                        TextView alertPlantTextView = requireView().findViewById(R.id.note4);
                        alertPlantTextView.setText("Note4: " + type + " - " + message);
                    }else if(id == 5){
                        TextView alertPlantTextView = requireView().findViewById(R.id.note5);
                        alertPlantTextView.setText("Note5: " + type + " - " + message);
                    }


                    // Hoặc hiển thị Toast
                    //Toast.makeText(requireActivity(), "ALERT: " + message, Toast.LENGTH_LONG).show();
                });

            } catch (JSONException e) {
                Log.e("SocketIO", "Error parsing alert data", e);
            }
        }
    };

}

