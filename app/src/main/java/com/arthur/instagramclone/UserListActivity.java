package com.arthur.instagramclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {
    private String currentUser, currentUserEmail;
    private ListView userListV;
    protected ArrayList<String>userArrayList=new ArrayList<String>();
    protected ArrayAdapter<String>arrayAdapter;
    protected Firebase ref=MainActivity.ref;
    private int indexPhoto;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.Upload:
                uploadImg();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void uploadImg() {
        Intent imageIntent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(imageIntent,1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_list_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setUp(){
        //1-Setup Views
        userListV=(ListView)findViewById(R.id.userList);
        arrayAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,userArrayList);
        userListV.setAdapter(arrayAdapter);

        //2-Listeners
        userListV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        ref.child("Images/total").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                indexPhoto=dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        //3.0 - Get the current Username
        Intent i=getIntent();
        currentUser=i.getStringExtra("username");
        //3-Get the usernames from firebase and add to the arraylist..
        Query users=ref.child("Users").orderByKey();
        users.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(!dataSnapshot.getKey().equals(currentUser)){ //if it isn't the current user...
                    userArrayList.add(dataSnapshot.getKey());
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1&&resultCode==RESULT_OK&&data!=null){
            try{
                Uri selectedImg=data.getData();
                Log.i("AppProgress", "Uri User Data= " + selectedImg.getUserInfo());
                Bitmap bitmapImg=MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImg);
                Toast.makeText(UserListActivity.this,"Image selected",Toast.LENGTH_SHORT).show();

                //Now need to upload image to firebase with correct username.
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                bitmapImg.compress(Bitmap.CompressFormat.PNG, 100, stream);
                final byte[]byteArray=stream.toByteArray();
                final String imageSRC= Base64.encodeToString(byteArray, Base64.DEFAULT);

                ref.child("Images/"+String.valueOf(indexPhoto)+"/src").setValue(imageSRC, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if(firebaseError==null){
                            Log.i("AppProgress", "Currently Uploading...");
                            ref.child("Images/"+String.valueOf(indexPhoto)+"/Owner").setValue(currentUser);
                            ref.child("Users/"+currentUser+"/photos").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        ref.child("Users/" + currentUser + "/photos").setValue(String.valueOf(indexPhoto));
                                    } else {
                                        String temp=dataSnapshot.getValue(String.class);
                                        temp=temp+","+indexPhoto;
                                        ref.child("Users/" + currentUser + "/photos").setValue(temp);
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });
                            ref.child("Images/total").setValue(indexPhoto + 1);


                        } else {
                            Toast.makeText(UserListActivity.this,"Upload Unsuccessful",Toast.LENGTH_LONG).show();
                            Log.i("AppProgress","Error: "+firebaseError.getMessage());
                        }
                    }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        getSupportActionBar().setTitle("User Lists");
        setUp();
    }
}
