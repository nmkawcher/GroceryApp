package com.kawcher.mygrocery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Splash_Activity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //make full screen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                ,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);

        firebaseAuth=FirebaseAuth.getInstance();


        //start lgoin activity after  2 second

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

                if(firebaseUser==null){
                    startActivity(new Intent(Splash_Activity.this,LoginActivity.class));
                    finish();
                } else {

                    //user  logger  in , check user type

                    checkUserType();
                }
            }

        },2000);
    }

    private void checkUserType() {

        //if user is seller , start seller main screen
//if user is buyer  , start user  main screen

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");

        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){

                            String accountType =""+dataSnapshot.child("accountType").getValue();

                            if(accountType.equals("Seller")){


                                //user is  seller
                                startActivity(new Intent(Splash_Activity.this,MainSellerActivity.class));
                                finish();
                            } else {


                                //user is  buyer
                                startActivity(new Intent(Splash_Activity.this,MainUserActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}