package com.kawcher.mygrocery.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kawcher.mygrocery.R;
import com.kawcher.mygrocery.adapters.AdapterOrderUser;
import com.kawcher.mygrocery.adapters.AdapterShop;
import com.kawcher.mygrocery.models.ModelOrderUser;
import com.kawcher.mygrocery.models.ModelShop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTV,emailTV,phoneTV,tabShopsTV,tabOrdersTV;
    private ImageButton logoutBtn,editProfileBtn,settingBtn;
    private RelativeLayout shopsRL,ordersRL;
    private ImageView profileIV;

    private RecyclerView shopsRV,ordersRV;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelShop>shopList;
    private AdapterShop   adapterShop;

    private ArrayList<ModelOrderUser>ordersList;
    private AdapterOrderUser adpterOrderUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTV=findViewById(R.id.nameTV);
        phoneTV=findViewById(R.id.phoneTV);
        emailTV=findViewById(R.id.emailTV);
        tabShopsTV=findViewById(R.id.tabShopsTV);
        tabOrdersTV=findViewById(R.id.tabOrdersTV);

       shopsRL=findViewById(R.id.shopsRL);
        ordersRL=findViewById(R.id.ordersRL);
        logoutBtn=findViewById(R.id.logoutBtn);
        editProfileBtn=findViewById(R.id.editProfileBtn);
        settingBtn=findViewById(R.id.settingBtn);

        profileIV=findViewById(R.id.profileIV);
        shopsRV=findViewById(R.id.shopsRV);
        ordersRV=findViewById(R.id.ordersRV);





        firebaseAuth =FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("logout....");
        progressDialog.setCanceledOnTouchOutside(false);


        checkUser();

        showShopsUI();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //logout
                //make  offline
                makeMeOfiline();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainUserActivity.this,ProductEditUserActivity.class));

                finish();
            }
        });

        tabShopsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show  shops
                showShopsUI();
            }
        });

        tabOrdersTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show orders

                showOrdersUI();
            }
        });

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showShopsUI() {

        shopsRL.setVisibility(View.VISIBLE);
        ordersRL.setVisibility(View.GONE);

        tabShopsTV.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTV.setBackgroundResource(R.drawable.shape_rect_004);

        tabOrdersTV.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }


    private void showOrdersUI() {

        ordersRL.setVisibility(View.VISIBLE);
        shopsRL.setVisibility(View.GONE);

        tabOrdersTV.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTV.setBackgroundResource(R.drawable.shape_rect_004);

        tabShopsTV.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }
    private void makeMeOfiline() {

        progressDialog.setMessage("loging out....");

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("online","false");

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();

                        Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void checkUser(){

        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

        if(firebaseUser==null){

            startActivity(new Intent(MainUserActivity.this,LoginActivity.class));
            finish();
        } else {

            loadMyInfor();
        }
    }

    private void loadMyInfor() {

        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){

                            String name=""+dataSnapshot.child("name").getValue();
                            String email=""+dataSnapshot.child("email").getValue();
                            String phone=""+dataSnapshot.child("phone").getValue();
                            String profileImage=""+dataSnapshot.child("profileImage").getValue();
                            String city=""+dataSnapshot.child("city").getValue();
                            String accountType=""+dataSnapshot.child("accountType").getValue();

                            //set user  data
                            nameTV.setText(name);
                            emailTV.setText(email);
                            phoneTV.setText(phone);

                            try{

                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person).into(profileIV);


                            } catch (Exception e){

                                profileIV.setImageResource(R.drawable.ic_person);
                            }
                            
                            //load only those shops  that are in the city of user
                            
                            loadShops(city);
                            loadOrders();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrders() {

        //init order list
        ordersList=new ArrayList<>();

        //get orders

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ordersList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){

                    String uid=""+ds.getRef().getKey();

                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if(snapshot.exists()){
                                        for (DataSnapshot ds: snapshot.getChildren()){

                                            ModelOrderUser modelOrderUser=ds.getValue(ModelOrderUser.class);

                                            //add to list
                                            ordersList.add(modelOrderUser);
                                        }
                                        //setup adapter
                                        adpterOrderUser=new AdapterOrderUser(getApplicationContext(),ordersList);
                                        //set to recyclerview
                                        ordersRV.setAdapter(adpterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                    Toast.makeText(MainUserActivity.this, ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(MainUserActivity.this, ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadShops(final String myCity) {

        //init list
        shopList=new ArrayList<>();


        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //clear list before adding

                        shopList.clear();

                        for(DataSnapshot ds: snapshot.getChildren()){

                            ModelShop modelShop=ds.getValue(ModelShop.class);

                            String shopCity=""+ds.child("city").getValue();

                            //show only user city shops

                            if(shopCity.equals(myCity)){

                                shopList.add(modelShop);
                            }
                        }

                        //setup adapter
                        adapterShop=new AdapterShop(MainUserActivity.this,shopList);

                        //set Adapter recyclerview

                        shopsRV.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
