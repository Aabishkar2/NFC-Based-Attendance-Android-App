package com.example.satan.nfcattendance;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ViewRoutine extends AppCompatActivity {

    ImageView routineImage;
    Button btn_adit, btn_dit, btn_f1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_routine);

        StorageReference mStorageRef;       //firebase storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();

        routineImage = (ImageView)findViewById(R.id.routineImage);

        btn_adit = (Button)findViewById(R.id.btn_adit);
        btn_dit = (Button)findViewById(R.id.btn_dit);
        btn_f1 = (Button)findViewById(R.id.btn_f1);

        btn_adit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adit();
            }
        });

        btn_dit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dit();
            }
        });

        btn_f1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                f1();
            }
        });

    }

    //get image from firebase storage
    //glide is a third party library used to access image from firebase
    public void adit()
    {
        String Uri1 = "https://firebasestorage.googleapis.com/v0/b/splash-screen-and-login-11d47.appspot.com/o/ADIT1.JPG?alt=media&token=0e0cc214-5aa2-4160-9967-1cc3910eaa6d";
        Glide.with(getApplicationContext()).load(Uri1).into(routineImage);
    }

    public void dit()
    {
        String Uri1 = "https://firebasestorage.googleapis.com/v0/b/splash-screen-and-login-11d47.appspot.com/o/DIT1.JPG?alt=media&token=b201693d-fb0f-4405-96ae-3e89b97f78e1";
        Glide.with(getApplicationContext()).load(Uri1).into(routineImage);
    }

    public void f1()
    {
        String Uri1 = "https://firebasestorage.googleapis.com/v0/b/splash-screen-and-login-11d47.appspot.com/o/F1.JPG?alt=media&token=41023e90-9419-4b54-9f1a-69b7db7b4535";
        Glide.with(getApplicationContext()).load(Uri1).into(routineImage);
    }
}
