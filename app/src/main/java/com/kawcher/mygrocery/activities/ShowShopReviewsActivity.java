package com.kawcher.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kawcher.mygrocery.R;
import com.kawcher.mygrocery.adapters.AdapterReview;
import com.kawcher.mygrocery.models.ModelReview;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ShowShopReviewsActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ImageView profileIV;
    private TextView shopNameTV, ratingsTV;
    private RatingBar ratingBar;
    private RecyclerView reviewsRV;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelReview>reviewArrayList;// will contain list of all user

    private AdapterReview adapterReview;

    private String shopUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_shop_reviews);

        backBtn = findViewById(R.id.backBtn);
        profileIV = findViewById(R.id.profileIV);
        shopNameTV = findViewById(R.id.shopNameTV);
        ratingBar = findViewById(R.id.ratingBar);
        ratingsTV = findViewById(R.id.ratingsTV);
        reviewsRV = findViewById(R.id.reviewsRV);

        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth=FirebaseAuth.getInstance();
        loadShopDetails();// for shop name,image,and rating
        loadReviews();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



    }

    private float ratingSum=0;
    private void loadReviews() {

        reviewArrayList=new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //clear list before adding data into it
                        reviewArrayList.clear();

                        ratingSum=0;
                        for(DataSnapshot ds:snapshot.getChildren()){

                            float rating=Float.parseFloat(""+ds.child("ratings").getValue());//e.g 4.3
                            ratingSum=ratingSum+rating;//for average rating,ad(addition of) all ratings,later will divide it  by number of reviews

                            ModelReview modelReview=ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);
                        }
                        //set up adapter
                        adapterReview=new AdapterReview(getApplicationContext(),reviewArrayList);
                        reviewsRV.setAdapter(adapterReview);

                        long numberOfReviews=snapshot.getChildrenCount();
                        float avgRating=ratingSum/numberOfReviews;

                        ratingsTV.setText(String.format("%.2f",avgRating)+"("+numberOfReviews+")");//e.g 4.7(10)
                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadShopDetails() {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String shopName=""+snapshot.child("shopName").getValue();
                        String profileImage=""+snapshot.child("profileImage").getValue();

                        shopNameTV.setText(shopName);

                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIV);
                        } catch (Exception e){
                            //if anything goes wrong setting image (exception occurs), set defult image

                            profileIV.setImageResource(R.drawable.ic_store_gray);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}