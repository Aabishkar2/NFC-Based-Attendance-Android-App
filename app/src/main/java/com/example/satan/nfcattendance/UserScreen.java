package com.example.satan.nfcattendance;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UserScreen extends AppCompatActivity {

    private static final String TAG = "UserScreen";

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    //private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    private String userID, Username, date, testing;

    TextView welcome, emailID, groupID, studentID, roomLog, timeView,NFCcheck;

    Button schedule;

    NfcAdapter nfcAdapter;

    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    private Firebase timeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_screen);

        schedule=(Button)findViewById(R.id.schedule);

        timeView = (TextView)findViewById(R.id.timeView);

        // Get system date and time
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("E dd/MM/yyyy HH:mm");
        date = simpleDateFormat.format(calendar.getTime());

        timeView.setText(date);     //display current date and time


        //NFC part

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        roomLog = (TextView)findViewById(R.id.roomLog);

        //checking if NFC is enabled
        NFCcheck =(TextView)findViewById(R.id.NFCcheck);
        if (!nfcAdapter.isEnabled() )
        {
            NFCcheck.setText("Please Turn On your NFC and restart the App!");
        }
        else
        {
            NFCcheck.setText("");
        }

        //Get intent from login page
        Intent intent=getIntent();
        String email=intent.getStringExtra("useremail");
        //welcome = (TextView)findViewById(R.id.welcome);
        //welcome.setText("User: " + email + " logged in.");


        // Firebase part
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();     //UID of authenticated users in firebase

        //compare login data with firebase authentication
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // open schedule activity
        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserScreen.this, ViewRoutine.class);
                startActivity(intent);
            }
        });
    }

    public void showData(DataSnapshot dataSnapshot)
    {
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            // get information of related UID from "user" node of the database
            UserInformation uInfo = dataSnapshot.child("users").child(userID).getValue(UserInformation.class);

            //get all the information
            Log.d(TAG, "showData: Email: " + uInfo.getEmail());
            Log.d(TAG, "showData: GroupID: " + uInfo.getGroupID());
            Log.d(TAG, "showData: Name: " + uInfo.getName());
            Log.d(TAG, "showData: StudentID: " + uInfo.getStudentID());

            //display user information on userscreen
            welcome = (TextView)findViewById(R.id.welcome);
            welcome.setText("Hi, " + uInfo.getName() + " !");

            emailID = (TextView)findViewById(R.id.emailID);
            emailID.setText("Registered Email: " + uInfo.getEmail());

            groupID = (TextView)findViewById(R.id.groupID);
            groupID.setText("Group: " + uInfo.getGroupID());

            studentID = (TextView)findViewById(R.id.studentID);
            studentID.setText("Student ID: " + uInfo.getStudentID());
        }
    }




    @Override
    protected void onResume()
    {
        super.onResume();
        enableForegroundDispatchSystem();   //activate read mode for NFC on device

        //check if NFC is enabled
        NFCcheck =(TextView)findViewById(R.id.NFCcheck);
        if (!nfcAdapter.isEnabled() )
        {
            NFCcheck.setText("Please Turn On your NFC and restart the App!");
        }
        else
        {
            NFCcheck.setText("");
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        disableForegroundDispatchSystem();  //deactivate read mode for NFC on device
    }

    @Override
    protected void onNewIntent(Intent intent)       //the attendance part
    {
        super.onNewIntent(intent);

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG))
        {
            //get data from NFC tag
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            //verify NFC data content
            if (parcelables !=null && parcelables.length>0)
                {
                    readTextFromMessage((NdefMessage) parcelables[0]);      //get classrrom data
                    getdatetime();                                          //get user data and time and date
                    Toast.makeText(this, "NFC tagged in. Attendance Done!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(this, "Tag is empty, Please contact RTE!", Toast.LENGTH_LONG).show();
                }
        }
    }

    public void readTextFromMessage(NdefMessage ndefMessage)        //read text from nfc
    {
        NdefRecord[] ndefRecords = ndefMessage.getRecords();

        if (ndefRecords != null && ndefRecords.length>0)
        {
            NdefRecord ndefRecord = ndefRecords[0];

            String tagContent = getTextFromNdefRecord(ndefRecord);
            testing = getTextFromNdefRecord(ndefRecord);
            roomLog.setText("You are attending on " + tagContent);      //display information on what class is being attended
        }
        else
        {
            Toast.makeText(this, "No NDEF records found! Contact RTE!", Toast.LENGTH_LONG).show();       //when the content of NFC is irrelevant
        }
    }

    private void enableForegroundDispatchSystem()       //when app is active, enable read mode of nfc when app is active
    {
        Intent intent = new Intent(this, UserScreen.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        IntentFilter[] intentFilters = new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null);
    }

    private void disableForegroundDispatchSystem()      //enable read mode of NFC when app is in background
    {
        nfcAdapter.disableForegroundDispatch(this);
    }

   public String getTextFromNdefRecord(NdefRecord ndefRecord)       //get data from NFC
    {
        String tagContent = null;

        try
        {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1, payload.length - languageSize - 1, textEncoding);
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("getTextFromNdefRecord", e.getMessage(),e);
        }

        return tagContent;
    }

    public void getdatetime()       //get system date and time
    {
        Firebase.setAndroidContext(this);
        timeRef = new Firebase("https://splash-screen-and-login-11d47.firebaseio.com/");
        Firebase ref = timeRef.child("AttendanceRecord").push();

        String IDrecord = studentID.getText().toString();
        String timerecord = timeView.getText().toString();
        String roomrecord = testing;
        ref.setValue(roomrecord + " , " + IDrecord + " , " + timerecord);       //set value to upload to database
    }
}
