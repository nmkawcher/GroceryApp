package com.kawcher.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kawcher.mygrocery.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ImageView profileIV;
    private TextView shopNameTv;
    private RatingBar ratingBar;
    private EditText reviewET;
    private FloatingActionButton submitBtn;

    private String shopUid;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        backBtn=findViewById(R.id.backBtn);
        profileIV=findViewById(R.id.profileIV);
        shopNameTv=findViewById(R.id.shopNameTV);
        ratingBar=findViewById(R.id.ratingBar);
        reviewET=findViewById(R.id.reviewET);
        submitBtn=findViewById(R.id.fab);
        shopUid=getIntent().getStringExtra("shopUid");

        firebaseAuth=FirebaseAuth.getInstance();

        //load shop info: shop name,shop image
        loadShopInfo();
        //if user has written review to this shop,load it
       loadMyReview();
   //get shop uid from



        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                inputData();


            }
        });


    }
 /*   String  shopName="null";
    String  shopImage="null"; */
    private void loadShopInfo() {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //get shop info
                String  shopName=""+snapshot.child("shopName").getValue();
                 String shopImage=""+snapshot.child("profileImages").getValue();

                //set shop info to ui
                shopNameTv.setText(shopName);

                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_store_gray).into(profileIV);
                } catch (Exception e){

                    profileIV.setImageResource(R.drawable.ic_store_gray);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadMyReview() {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){


                            //my review is available  in this shop

                            //get review details
                            String  uid=""+snapshot.child("uid").getValue();
                            String  ratings=""+snapshot.child("ratings").getValue();
                            String  review=""+snapshot.child("review").getValue();
                            String  timestamp=""+snapshot.child("timestamp").getValue();

                            //set review details to our ui




                                float myRating=Float.parseFloat(ratings);
                                //   float myRating=Float.parseFloat(ratings);
                                ratingBar.setRating(myRating);
                                reviewET.setText(review);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {

        String ratings=""+ratingBar.getRating();
        String review=reviewET.getText().toString().trim();


        //for time of review
        String timestamp=""+System.currentTimeMillis();

        //set up data to hashmap

        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("uid",""+firebaseAuth.getUid()) ;
        hashMap.put("ratings",""+ratings);
        hashMap.put("review",""+review);
        hashMap.put("timestamp",""+timestamp);



        //put to db: DB>users>shopUid>Ratings

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //review added to db
                        Toast.makeText(WriteReviewActivity.this, "Review published successfully....", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(WriteReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }
}
