package com.arthur.instagramclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnKeyListener,View.OnClickListener {

    private EditText usernameField, pwField;
    private Button loginButton,signupButton;
    static Firebase ref;
    private void setUp(){
        //1-Get Views
        usernameField=(EditText)findViewById(R.id.unameField);
        pwField=(EditText)findViewById(R.id.pwField);
        loginButton=(Button)findViewById(R.id.loginB);
        signupButton=(Button)findViewById(R.id.signupB);

        //2-Add Listeners
        usernameField.setOnKeyListener(this);
        pwField.setOnKeyListener(this);
        findViewById(R.id.relativeLayout).setOnClickListener(this);     //These 2 are to make the softkeyboards disappear
        findViewById(R.id.logoView).setOnClickListener(this);


        //3-Set up Firebase
        Firebase.setAndroidContext(this);
        ref=new Firebase("https://instagram-arthur.firebaseio.com/");   //ref points to the ~ of the databse


    }
    public void logIn(View button){
        //Attempt login with credentials. If fails give a popup error. Need to first grab the username and password entered....
        final String uName=usernameField.getText().toString();
        final String pWord=pwField.getText().toString();
        if(uName.equals("")||pWord.equals("")){
            Toast.makeText(MainActivity.this,"Please enter a username and password",Toast.LENGTH_SHORT).show();

        }else{
            try{
                ref.child("Users/"+uName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String email=dataSnapshot.child("email").getValue(String.class);
                            ref.authWithPassword(email, pWord, new Firebase.AuthResultHandler() {
                                @Override
                                public void onAuthenticated(AuthData authData) {    //Successful login using username..
                                    Toast.makeText(MainActivity.this, "Login-Successful, "+authData.getProviderData().get("email"), Toast.LENGTH_LONG).show();
                                    loginSuccess(uName);
                                }

                                @Override
                                public void onAuthenticationError(FirebaseError firebaseError) {
                                    Toast.makeText(MainActivity.this, "Login-Failed,incorrect username/password!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }catch (Exception e){
                Toast.makeText(MainActivity.this, "Login-Failed,your username doesn't exist!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loginSuccess(String uName) {
        Intent i=new Intent(getApplicationContext(),UserListActivity.class);
        i.putExtra("username",uName);
        startActivity(i);

    }

    public void signUp(View button){
        //Should bring up a sign up activity
        Intent i=new Intent(getApplicationContext(),SignupActivity.class);
        startActivity(i);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUp();

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_ENTER&&event.getAction()==KeyEvent.ACTION_DOWN){
            logIn(null);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        InputMethodManager inM=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inM.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }
}
