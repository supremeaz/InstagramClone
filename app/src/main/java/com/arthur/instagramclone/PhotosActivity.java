package com.arthur.instagramclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class PhotosActivity extends AppCompatActivity {

    protected Firebase ref;
    protected String profile;
    protected String ownedPhotos;
    protected String[]photoList;
    private void setUp(){
        Intent i=getIntent();
        profile=i.getStringExtra("user");
        ref=MainActivity.ref;

        ref.child("Users/"+profile+"/photos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ownedPhotos=dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        photoList=ownedPhotos.split(",");
        for(int j=0;j<photoList.length;j++){
            
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        setUp();
    }
}
