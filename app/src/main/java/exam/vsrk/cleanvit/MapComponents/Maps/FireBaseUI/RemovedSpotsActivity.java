package exam.vsrk.cleanvit.MapComponents.Maps.FireBaseUI;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import exam.vsrk.cleanvit.R;

import static exam.vsrk.cleanvit.MapComponents.Maps.AppController.mFirebaseRef;

/**
 * Created by VSRK on 12/31/2015.
 */
public class RemovedSpotsActivity extends AppCompatActivity {

    private FirebaseRecyclerAdapter mAdapter;
    List<RemovedSpotItems> removedSpots;
  private static GeoLocation INITIAL_CENTER = new GeoLocation(26.204675, 78.191340);
    LatLng latLngCenter = new LatLng(INITIAL_CENTER.latitude, INITIAL_CENTER.longitude);
    @Override
    protected void onCreate(Bundle s) {

        super.onCreate(s);
        setContentView(R.layout.removed_spots_list);


        Firebase.setAndroidContext(this);
        final Firebase mFirebaseRef=new Firebase(getResources().getString(R.string.firebase_url));
        GeoFire geoFire=new GeoFire(new Firebase("https://radiant-inferno-7381.firebaseio.com/markers/"));
        GeoQuery geoQuery = geoFire.queryAtLocation(INITIAL_CENTER, 1);
        removedSpots=new ArrayList<>();

       final RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler_view);
       recycler.setHasFixedSize(true);
       recycler.setLayoutManager(new LinearLayoutManager(this));

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

        mAdapter = new FirebaseRecyclerAdapter<RemovedSpotItems, ReomvedSpotViewHolder>(RemovedSpotItems.class,R.layout.removed_spots_row, ReomvedSpotViewHolder.class,mFirebaseRef.child("markers")) {

            @Override
            protected void populateViewHolder(ReomvedSpotViewHolder viewHolder, RemovedSpotItems model, int position) {
                super.populateViewHolder(viewHolder, model, position);

                RemovedSpotItems model1 = removedSpots.get(position);
                if(!TextUtils.isEmpty(model1.getDescription()))
                {
                    Log.v("DESC",model1.getDescription());
                }
              //  System.out.println(model1.getDescription());
                if (!TextUtils.isEmpty(model1.getDescription())&&!TextUtils.isEmpty(model1.getStatus())) {
                    viewHolder.description.setText(model1.getDescription());
                    viewHolder.email.setText(model1.getStatus());
                }


            }

        };

        recycler.setAdapter(mAdapter);

    }
}