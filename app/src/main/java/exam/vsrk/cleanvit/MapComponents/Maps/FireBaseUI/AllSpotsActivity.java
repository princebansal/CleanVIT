package exam.vsrk.cleanvit.MapComponents.Maps.FireBaseUI;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import exam.vsrk.cleanvit.MapComponents.Maps.MainActivity;
import exam.vsrk.cleanvit.MapComponents.Maps.Spot;
import exam.vsrk.cleanvit.R;

import static exam.vsrk.cleanvit.MapComponents.Maps.AppController.mFirebaseRef;

/**
 * Created by VSRK on 12/31/2015.
 */
public class AllSpotsActivity extends AppCompatActivity {

    private RecyclerView recycler;

    private FirebaseRecyclerAdapter mAdapter;
    List<SpotItem> removedSpots;


    @Override
    protected void onCreate(Bundle s) {

        super.onCreate(s);
        setContentView(R.layout.activity_all_spots);


        Firebase.setAndroidContext(this);

        recycler = (RecyclerView) findViewById(R.id.recycler_view);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(this));
/*
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                String hash = mFirebaseRef.child("markers").getKey();
                mFirebaseRef.child("markers").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();


                        System.out.println("Description: " + newPost.get("description"));
                        System.out.println("Status: " + newPost.get("status"));
                        RemovedSpotItems items = new RemovedSpotItems();

                        items.setDescription((String) newPost.get("description"));
                        items.setStatus((String) newPost.get("status"));
                        removedSpots.add(items);


                    }

                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });

            }

            @Override
            public void onKeyExited(String key) {
                System.out.println("Key :" + key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("fire");

            }

            @Override
            public void onGeoQueryError(FirebaseError error) {
                System.out.println("Error loading Firebase");
            }
        });
*/
        mAdapter = new FirebaseRecyclerAdapter<SpotItem, AllSpotViewHolder>(SpotItem.class, R.layout.activity_all_spots_recycler_row, AllSpotViewHolder.class, mFirebaseRef.child("markers")) {

            @Override
            public void onBindViewHolder(AllSpotViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.setContext(AllSpotsActivity.this);
            }

            @Override
            protected void populateViewHolder(AllSpotViewHolder viewHolder, SpotItem model, int position) {
                super.populateViewHolder(viewHolder, model, position);

                try {
                    viewHolder.latLng.setText(model.getL().get(0) + "/" + model.getL().get(1));
                    viewHolder.status.setText(model.getStatus());
                    viewHolder.description.setText(model.getDescription());
                    viewHolder.place.setText(model.getPlace());
                    if (model.getStatus().equals(Spot.SPOT_DIRTY))
                        viewHolder.outer.setBackgroundColor(Color.RED);
                    else
                        viewHolder.outer.setBackgroundColor(Color.GREEN);
                }catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }

        };

        recycler.setAdapter(mAdapter);

    }

}