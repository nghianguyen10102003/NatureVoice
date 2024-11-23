package vn.edu.usth.naturevoice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link NewPlantFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class NewPlantFragment extends Fragment {

//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    public NewPlantFragment() {
        // Required empty public constructor
    }

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment NewPlantFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static NewPlantFragment newInstance(String param1, String param2) {
//        NewPlantFragment fragment = new NewPlantFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }
    private String selectedPlantIconId = null; // Lưu ID của cây được chọn
    private String selectedPlantPotId = null; // Lưu ID của chậu được chọn
    private String selectIcon = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_plant, container, false);

        FrameLayout plantIcon1 = view.findViewById(R.id.Plant_icon_1);
        FrameLayout plantIcon2 = view.findViewById(R.id.Plant_icon_2);
        FrameLayout plantIcon3 = view.findViewById(R.id.Plant_icon_3);
        FrameLayout plantPot1 = view.findViewById(R.id.Plant_pot_1);
        FrameLayout plantPot2 = view.findViewById(R.id.Plant_pot_2);
        FrameLayout plantPot3 = view.findViewById(R.id.Plant_pot_3);
        Button letsPlantButton = view.findViewById(R.id.lets_plant_button);// Xử lý chọn cây
        plantIcon1.setOnClickListener(v -> {
            resetPlantIcons(view);
            plantIcon1.setBackgroundColor(Color.LTGRAY);
            selectedPlantIconId = "1";
        });

        plantIcon2.setOnClickListener(v -> {
            resetPlantIcons(view);
            plantIcon2.setBackgroundColor(Color.LTGRAY);
            selectedPlantIconId = "2";
        });

        plantIcon3.setOnClickListener(v -> {
            resetPlantIcons(view);
            plantIcon3.setBackgroundColor(Color.LTGRAY);
            selectedPlantIconId = "3";
        });

        // Xử lý chọn chậu
        plantPot1.setOnClickListener(v -> {
            resetPlantPots(view);
            plantPot1.setBackgroundColor(Color.LTGRAY);
            selectedPlantPotId = "1";
        });

        plantPot2.setOnClickListener(v -> {
            resetPlantPots(view);
            plantPot2.setBackgroundColor(Color.LTGRAY);
            selectedPlantPotId = "2";
        });

        plantPot3.setOnClickListener(v -> {
            resetPlantPots(view);
            plantPot3.setBackgroundColor(Color.LTGRAY);
            selectedPlantPotId = "3";
        });


        // Xử lý khi nhấn nút "Let's plant"
        // Inside the "Let's plant" button click listener
        letsPlantButton.setOnClickListener(v -> {
            if (selectedPlantIconId == null || selectedPlantPotId == null) {
                Toast.makeText(getActivity(), "Please select both a plant and a pot!", Toast.LENGTH_SHORT).show();
            } else {
                // Dynamically set the selectIcon
                selectIcon = selectedPlantIconId + "_" + selectedPlantPotId;


                // Create the Plant object with appropriate parameters
                Plant plant = new Plant("Cây Bonsai", 1, 101, Integer.parseInt(selectedPlantIconId), Integer.parseInt(selectedPlantPotId), 401);

                // Pass the Plant object via Intent
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.putExtra("plant_data", plant);
                startActivity(intent);


                // Navigate to HomeFragment
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.newPlant, new HomeFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;

    }
    private void resetPlantIcons(View view) {
        view.findViewById(R.id.Plant_icon_1).setBackgroundColor(Color.TRANSPARENT);
        view.findViewById(R.id.Plant_icon_2).setBackgroundColor(Color.TRANSPARENT);
        view.findViewById(R.id.Plant_icon_3).setBackgroundColor(Color.TRANSPARENT);
    }

    private void resetPlantPots(View view) {
        view.findViewById(R.id.Plant_pot_1).setBackgroundColor(Color.TRANSPARENT);
        view.findViewById(R.id.Plant_pot_2).setBackgroundColor(Color.TRANSPARENT);
        view.findViewById(R.id.Plant_pot_3).setBackgroundColor(Color.TRANSPARENT); }
}