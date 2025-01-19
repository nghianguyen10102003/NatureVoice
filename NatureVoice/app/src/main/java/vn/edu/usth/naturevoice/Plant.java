package vn.edu.usth.naturevoice;

import android.widget.TextView;

import java.io.Serializable;

public class Plant implements Serializable {
    private String name;
    private int id;
    private int plantId;
    private int potId;
    private int species; // Changed Species to species for naming consistency
    private int sensorId;
    private TextView bubbleTextView;
    private String noti_type;
    private String noti_message;
    // Constructor
    public Plant(String name, int species, int id, int plantId, int potId, int sensorId) {
        this.name = String.valueOf(name);
        this.id = id;
        this.plantId = plantId;
        this.potId = potId;
        this.species = species;
        this.sensorId = sensorId;

        //id, name, species, char, pot_icon_id, sensorID
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
    public TextView getBubbleTextView() {
        return bubbleTextView;
    }


    public void setBubbleTextView(TextView bubbleTextView) {
        this.bubbleTextView = bubbleTextView;
    }

    public void setNoti_type(String noti_type) {this.noti_type = noti_type;}

    public String getNoti_type() {return noti_type;}

    public void setNoti_message(String noti_message) {this.noti_message = noti_message;}

    public String getNoti_message() {return noti_message;}

    public int getPlantId() {
        return plantId;
    }

    public int getPotId() {
        return potId;
    }

    public int getSensorId() {
        return sensorId;
    }

    public int getSpecies() {
        return species;
    }

    // Setter methods
    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPlantId(int plantId) {
        this.plantId = plantId;
    }

    public void setPotId(int potId) {
        this.potId = potId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public void setSpecies(int species) {
        this.species = species;
    }
}
