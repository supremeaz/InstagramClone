package com.arthur.instagramclone;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;

public class SignupActivity extends AppCompatActivity implements View.OnKeyListener,View.OnClickListener{
    EditText emailField,pwField,confirmpwField,usernameField;
    Button confirmButton;
    Firebase ref=MainActivity.ref;
    boolean valideU=false;
    private void setUp(){
        usernameField=(EditText)findViewById(R.id.usernameField);
        emailField=(EditText)findViewById(R.id.emailField);
        pwField=(EditText)findViewById(R.id.pwField);
        confirmpwField=(EditText)findViewById(R.id.confirmpwField);
        confirmButton=(Button)findViewById(R.id.confirmB);

        confirmpwField.setOnKeyListener(this);
        findViewById(R.id.relativeLayout).setOnClickListener(this);
        findViewById(R.id.iconView).setOnClickListener(this);
        usernameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //Everytime the username field is updated, Run a method with its text on the validateUsername method to check if username is available.. By checking firebase.
                if(s.length()<6){
                    usernameField.setTextColor(Color.BLACK);
                } else validateUsername(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }
    public void confirmSignup(View button){
        final String email=emailField.getText().toString();
        String pWord=pwField.getText().toString();
        String cPW=confirmpwField.getText().toString();
        final String username=usernameField.getText().toString();

        if(pWord.length()<6||email.equals("")||email.length()<6||username.length()<6||!valideU){
            Toast.makeText(SignupActivity.this, "Username and password need to be at least 6 characters long!", Toast.LENGTH_SHORT).show();
        }else{
            if(!pWord.equals(cPW)){
                Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            }else{
                ref.createUser(email, pWord, new Firebase.ValueResultHandler<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> stringObjectMap) {
                        //also make the user Index...
                        ref.child("Users/" + username+"/email").setValue(email);


                        Toast.makeText(SignupActivity.this, "User Successfully created", Toast.LENGTH_LONG).show();
                        signupSuccess(username);
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        Toast.makeText(SignupActivity.this,firebaseError.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
            }
        }

    }

    private void signupSuccess(String uName) {
        Intent i=new Intent(getApplicationContext(),UserListActivity.class);
        i.putExtra("username",uName);
        startActivity(i);
    }

    private void validateUsername(final String username) {
        ref.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.child(username).exists()){
                    Log.i("AppProgress","Username "+username+" exists... and Username is"+dataSnapshot.exists()+" and child exists: "+dataSnapshot.child(username).exists());
                    usernameField.setTextColor(Color.RED);
                    valideU=false;
                }else {
                    Log.i("AppProgress","Username "+username+" doesn't exist!and Username is"+dataSnapshot.exists()+" and child exists: "+dataSnapshot.child(username).exists());
                    usernameField.setTextColor(Color.GREEN);
                    valideU=true;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Signup Page");
        setContentView(R.layout.activity_signup);


        setUp();
    }

    @Override
    public void onClick(View v) {
        InputMethodManager inM=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inM.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_ENTER&&event.getAction()==KeyEvent.ACTION_DOWN){
            confirmSignup(null);
        }
        return false;
    }
}
