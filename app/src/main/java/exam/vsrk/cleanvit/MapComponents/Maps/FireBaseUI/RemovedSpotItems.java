package exam.vsrk.cleanvit.MapComponents.Maps.FireBaseUI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by VSRK on 1/1/2016.
 */
@JsonIgnoreProperties(ignoreUnknown=true)

public  class RemovedSpotItems {
   public String description;

   public String owner;
    public String status;


   public String getDescription()
   {
       return  description;
   }

    public String getStatus()
    {
        return status;
    }
    public String getOwner() { return owner;}
    public void setDescription(String description){description=description; }
    public void setStatus(String status){ status=status; }


    }

