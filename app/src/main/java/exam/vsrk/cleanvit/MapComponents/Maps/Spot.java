package exam.vsrk.cleanvit.MapComponents.Maps;

/**
 * Created by VSRK on 12/31/2015.
 */
import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

/**
 * Created by alfainfinity on 30/12/15.
 */
public class Spot implements Serializable {

    public static String SPOT_CLEANED="clean";
    public static String SPOT_DIRTY="dirty";

    private String spotId;
    private Marker marker;
    private String ownerId;
    private String ownerName;
    private String imageUrl;
    private String status;
    private String description;
    private String cleanedBy;
    private String place;

    public Spot(String id,Marker marker, String ownerId, String ownerName, String imageUrl) {
        this.spotId=id;
        this.marker = marker;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.imageUrl = imageUrl;
        this.status=SPOT_DIRTY;
        this.description="None";
        this.cleanedBy=null;
    }

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCleanedBy() {
        return cleanedBy;
    }

    public void setCleanedBy(String cleanedBy) {
        this.cleanedBy = cleanedBy;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }
}