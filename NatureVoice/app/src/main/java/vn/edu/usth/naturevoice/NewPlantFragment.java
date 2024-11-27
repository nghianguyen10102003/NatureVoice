package vn.edu.usth.naturevoice;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Objects;

public class NewPlantFragment extends Fragment {

    private EditText plantNameInput;
    private Spinner plantCharacterSpinner;
    private String selectedCharacter;
    private String selectedPlantIconId = null; // Lưu ID của cây được chọn
    private String selectedPlantPotId = null; // Lưu ID của chậu được chọn
    private String selectIcon = null;
    private static int plantIdCounter = 1; // ID tự tăng, bắt đầu từ 1

    // Phương thức để lấy ID tiếp theo
    public static int getNextPlantId() {
        return plantIdCounter++;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_plant, container, false);

        plantNameInput = view.findViewById(R.id.plant_name_input);
        plantCharacterSpinner = view.findViewById(R.id.plant_character_spinner);

        // Setup character Spinner
        setupCharacterSpinner();

        // Plant and Pot selection logic
        FrameLayout plantIcon1 = view.findViewById(R.id.Plant_icon_1);
        FrameLayout plantIcon2 = view.findViewById(R.id.Plant_icon_2);
        FrameLayout plantIcon3 = view.findViewById(R.id.Plant_icon_3);
        FrameLayout plantPot1 = view.findViewById(R.id.Plant_pot_1);
        FrameLayout plantPot2 = view.findViewById(R.id.Plant_pot_2);
        FrameLayout plantPot3 = view.findViewById(R.id.Plant_pot_3);

        Button letsPlantButton = view.findViewById(R.id.lets_plant_button);

        // Handle plant icon selection
        plantIcon1.setOnClickListener(v -> {
            resetPlantIcons(view);
            plantIcon1.setSelected(true);
            selectedPlantIconId = "1";
        });

        plantIcon2.setOnClickListener(v -> {
            resetPlantIcons(view);
            plantIcon2.setSelected(true);
            selectedPlantIconId = "2";
        });

        plantIcon3.setOnClickListener(v -> {
            resetPlantIcons(view);
            plantIcon3.setSelected(true);
            selectedPlantIconId = "3";
        });

        // Handle pot selection
        plantPot1.setOnClickListener(v -> {
            resetPlantPots(view);
            plantPot1.setSelected(true);
            selectedPlantPotId = "1";
        });

        plantPot2.setOnClickListener(v -> {
            resetPlantPots(view);
            plantPot2.setSelected(true);
            selectedPlantPotId = "2";
        });

        plantPot3.setOnClickListener(v -> {
            resetPlantPots(view);
            plantPot3.setSelected(true);
            selectedPlantPotId = "3";
        });

        // Handle "Let's Plant" button click
        letsPlantButton.setOnClickListener(v -> {
            String plantName = plantNameInput.getText().toString().trim();

            if (plantName.isEmpty()) {
                plantNameInput.setError("Please enter the plant name");
                return;
            }
            int charId =0;
             if (Objects.equals(selectedCharacter, "Vui vẻ")){
                 charId = 1;
             } else if (Objects.equals(selectedCharacter, "Buồn bã")) {
                 charId = 2;
             }else if (Objects.equals(selectedCharacter, "Tức giận")) {
                 charId = 3;
             }

            if (selectedPlantIconId == null || selectedPlantPotId == null) {
                Toast.makeText(getActivity(), "Please select both a plant and a pot!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Dynamically set the selectIcon
            selectIcon = selectedPlantIconId + "_" + selectedPlantPotId;
            int plantId = getNextPlantId();
            // Create the Plant object with appropriate parameters
            Plant plant = new Plant(plantName, charId, plantId,
                    Integer.parseInt(selectedPlantIconId),
                    Integer.parseInt(selectedPlantPotId),'1'
            );

            // Pass the Plant object via Intent
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.putExtra("plant_data", plant);
            startActivity(intent);

            // Navigate to HomeFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.newPlant, new HomeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void setupCharacterSpinner() {
        String[] characterOptions = {"Vui vẻ", "Buồn bã", "Tức giận"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                characterOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        plantCharacterSpinner.setAdapter(adapter);

        plantCharacterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCharacter = characterOptions[position]; // Save the selected character
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCharacter = characterOptions[0]; // Default to the first character
            }
        });
    }

    private void resetPlantIcons(View view) {
        view.findViewById(R.id.Plant_icon_1).setSelected(false);
        view.findViewById(R.id.Plant_icon_2).setSelected(false);
        view.findViewById(R.id.Plant_icon_3).setSelected(false);
    }

    private void resetPlantPots(View view) {
        view.findViewById(R.id.Plant_pot_1).setSelected(false);
        view.findViewById(R.id.Plant_pot_2).setSelected(false);
        view.findViewById(R.id.Plant_pot_3).setSelected(false);
    }
}
