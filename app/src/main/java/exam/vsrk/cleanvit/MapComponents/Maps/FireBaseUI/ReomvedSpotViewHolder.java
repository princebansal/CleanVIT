package exam.vsrk.cleanvit.MapComponents.Maps.FireBaseUI;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import exam.vsrk.cleanvit.R;


/**
 * Created by VSRK on 1/1/2016.
 */
public class ReomvedSpotViewHolder extends RecyclerView.ViewHolder {
    TextView email;
    TextView description;

    public ReomvedSpotViewHolder(View itemView) {
        super(itemView);
        email = (TextView) itemView.findViewById(R.id.email);
        description = (TextView)  itemView.findViewById(R.id.description);
    }
}
