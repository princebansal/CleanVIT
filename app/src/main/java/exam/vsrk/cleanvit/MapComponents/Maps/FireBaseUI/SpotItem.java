package exam.vsrk.cleanvit.MapComponents.Maps.FireBaseUI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * Created by VSRK on 1/1/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

public class SpotItem {
    private String description;
    private String owner;
    private String status;
    private String g;
    private String place;
    private ArrayList<String> l;

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getOwner() {
        return owner;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public ArrayList<String> getL() {
        return l;
    }

    public void setL(ArrayList<String> l) {
        this.l = l;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }
}

