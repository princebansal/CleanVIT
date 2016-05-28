package com.gdgvitvellore.cleanvit.FireBaseUI;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gdgvitvellore.cleanvit.MainActivity;

import exam.vsrk.cleanvit.R;

public class AllSpotViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView latLng;
    TextView description;
    TextView status;
    TextView place;
    LinearLayout outer;
    AllSpotsActivity context;

    public AllSpotViewHolder(View itemView) {
        super(itemView);
        latLng = (TextView) itemView.findViewById(R.id.lat_lng);
        description = (TextView)  itemView.findViewById(R.id.description);
        status = (TextView)  itemView.findViewById(R.id.status);
        place = (TextView)  itemView.findViewById(R.id.place);
        outer=(LinearLayout)itemView.findViewById(R.id.outer);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String[] pos=((TextView)v.findViewById(R.id.lat_lng)).getText().toString().split("/");
        Intent result=new Intent();
        result.putExtra("lat",pos[0]);
        result.putExtra("lin",pos[1]);
        if(context!=null) {
            context.setResult(MainActivity.POSITION_REDIRECT_CODE,result);
            context.finish();
        }
    }

    public void setContext(AllSpotsActivity c){
        context=c;
    }
}
