package com.kawcher.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kawcher.mygrocery.Constatns;
import com.kawcher.mygrocery.R;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat fcbSwitch;
    private TextView notificationStatusTV;
    private ImageButton backBtn;

    private static final String enableMessage="NOTIFICTION ARE enabled";
    private static final String disableMessage="NOTIFICTION ARE disabled";

    private boolean isChecked=false;

    private FirebaseAuth firebaseAuth;

    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        fcbSwitch=findViewById(R.id.fcmSwitch);
        notificationStatusTV=findViewById(R.id.notificationStatusTV);
        backBtn=findViewById(R.id.backBtn);
        firebaseAuth=FirebaseAuth.getInstance();

        //init shared preference
        sp=getSharedPreferences("SETTINGS_SP",MODE_PRIVATE);
        //check last selected state true/false

        isChecked=sp.getBoolean("FCM_ENABLED",false);

        fcbSwitch.setChecked(isChecked);
        if(isChecked){
            //was enabled
            notificationStatusTV.setText(enableMessage);
        } else {
            //was desabled
            notificationStatusTV.setText(disableMessage);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
onBackPressed();
            }
        });

        //add switch check change listener to enable disable notification

        fcbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    //checked ,enable notification
                    subscribedToTopic();
                }else {
                    //unchecked ,disable notification
                    unSubscribedToTopic();
                }
            }
        });

    }

    private void subscribedToTopic(){

        FirebaseMessaging.getInstance().subscribeToTopic(Constatns.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //subscribed successfully
                        //save setting ins shared preference
                        spEditor=sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",true);
                        spEditor.apply();
                        Toast.makeText(SettingsActivity.this, ""+enableMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTV.setText(enableMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed subscribing

                    }
                });
    }

    private void unSubscribedToTopic(){

        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constatns.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //unsubsribed

                        //save setting ins shared preference
                        spEditor=sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",false);
                        spEditor.apply();
                        Toast.makeText(SettingsActivity.this, ""+disableMessage, Toast.LENGTH_SHORT).show();
                        notificationStatusTV.setText(disableMessage);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //failed to unsubscribing
                        Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

//we will work with topic based firebase messageing ,for a user  to receive topic based message/notification have to subscribe to that topic
//requirment FCM Server key, in copying my

//now we need to user state.what he choose
