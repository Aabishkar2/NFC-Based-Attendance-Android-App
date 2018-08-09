package com.example.satan.nfcattendance;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    RelativeLayout rellay1, rellay;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            rellay1.setVisibility(View.VISIBLE);
        }
    };

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText Email, Password;
    private Button signin;

    boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rellay1 = (RelativeLayout)findViewById(R.id.rellay1);

        //splash screen delay
        handler.postDelayed(runnable, 3000);
        //signin=(Button)findViewById(R.id.signin);
        //signin.setOnClickListener();



        //Now begins the Firebase Email-password login code

        Email = (EditText) findViewById(R.id.email);
        Password = (EditText) findViewById(R.id.password);
        signin = (Button) findViewById(R.id.signin);

        // Checking network connection
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
            signin.setText("Sign In");  //if network is connected
        }
        else {
            connected = false;
            signin.setText("Network error!");   //if network is not connected
        }

        mAuth = FirebaseAuth.getInstance();

        //check login credentials
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null)
                {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    toastMessage("Signed in with: " + user.getEmail());

                    Intent intent = new Intent(MainActivity.this, UserScreen.class);
                    intent.putExtra("useremail",Email.getText().toString());
                    startActivity(intent);
                    //startActivity(new Intent(MainActivity.this,UserScreen.class));
                }
                else
                {
                    Log.d(TAG, "onAuthStateChanged:signed_out:");
                }
            }
        };

        //when sign in button is clicked
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Email.getText().toString();
                String pass = Password.getText().toString();

                if (!email.equals("") && !pass.equals("") && email.contains("@"))   //check login parameters are correct
                {
                    mAuth.signInWithEmailAndPassword(email,pass);   //send values for login
                }
                else {
                    toastMessage("Please enter your correct login credentials!");   //when login parameters are incorrect
                }

            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);  //check previous login
    }

    @Override
    public void onStop()
    {
        super.onStop();     //discontinue the authentication
        if (mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public  void  onResume()
    {
        super.onResume();   //check network connectivity on activity resume of login screen
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
            signin.setText("Sign In");
        }
        else {
            connected = false;
            signin.setText("Network error!");
        }
    }

    private void toastMessage(String message)
    {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }
}
