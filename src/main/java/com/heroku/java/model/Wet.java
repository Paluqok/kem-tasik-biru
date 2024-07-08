package com.heroku.java.model;

public class Wet extends Activity {
    private String activityEquipment;

    public Wet() {}

    public Wet(Long activityId, String activityName, String activityDuration, double activityPrice, String activityImage, String activityEquipment) {
        super(activityId, activityName, activityDuration, activityPrice, activityImage);
        this.activityEquipment=activityEquipment;
    }

    public String getactivityEquipment(){
        return this.activityEquipment;
    }

    public void setactivityEquipment(String activityEquipment) {
        this.activityEquipment=activityEquipment;
    }
}
