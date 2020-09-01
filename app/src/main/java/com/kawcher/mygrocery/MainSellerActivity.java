package com.kawcher.mygrocery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSellerActivity extends AppCompatActivity {

    private TextView nameTV, shopNameTV, emailTV, tabProductTV, tabOrderTV, filteredProductsTV;
    private EditText searchProductET;
    private ImageButton logoutBtn, editProfileBtn, addProductBtn, filterProductBtn;
    private ImageView profileIV;
    private RelativeLayout productsRL, ordersRL;
    private RecyclerView productRV;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);


        nameTV = findViewById(R.id.nameTV);
        emailTV = findViewById(R.id.emailTV);
        shopNameTV = findViewById(R.id.shopNameTV);
        tabProductTV = findViewById(R.id.tabProductsTV);
        tabOrderTV = findViewById(R.id.tabOrdersTV);
        filteredProductsTV = findViewById(R.id.filteredProductsTV);


        searchProductET = findViewById(R.id.searchProductET);
        filterProductBtn = findViewById(R.id.filterProductBtn);

        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        addProductBtn = findViewById(R.id.addProductBtn);

        productRV = findViewById(R.id.productsRV);


        profileIV = findViewById(R.id.profileIV);

        productsRL = findViewById(R.id.productRL);
        ordersRL = findViewById(R.id.orderRL);


        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("logout....");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();
        showProductsUI();
        loadAllProducts();


        searchProductET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adapterProductSeller.getFilter().filter(s.toString());
                }catch (Exception e ){

                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                makeMeOfiline();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit profile activity


                startActivity(new Intent(MainSellerActivity.this, ProductEditSellerActivity.class));
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit add product activity

                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));

            }
        });

        tabProductTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //load product

                showProductsUI();
            }
        });

        tabOrderTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load order
                showOrdersUI();

            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);

                builder.setTitle("Choose Category: ")
                        .setItems(Constatns.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //get selected item
                                String selected = Constatns.productCategories1[which];
                                filteredProductsTV.setText(selected);

                                if (selected.equals("All")) {
                                    //load All
                                    loadAllProducts();

                                } else {

                                    //load filtered

                                    loadFilteredProducts(selected);
                                }

                            }
                        })
                .show()
                ;
            }
        });
    }

    private void loadFilteredProducts(final String selected) {

        productList = new ArrayList<>();
        //get all product
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //before getting reset list

                        productList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String productCategory=""+ds.child("productCategory").getValue();

                            //if selected category matches product category then add in list
                            if(selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);

                            }

                        }
                        //setup adapter

                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        //set Adapter
                        productRV.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(getApplicationContext(),"error:"+error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });



    }

    private void loadAllProducts() {


        productList = new ArrayList<>();
        //get all product
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //before getting reset list

                        productList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        //setup adapter

                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        //set Adapter

                         productRV.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showProductsUI() {
        //show products ui and hide orders ui

        productsRL.setVisibility(View.VISIBLE);
        ordersRL.setVisibility(View.GONE);

        tabProductTV.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductTV.setBackgroundResource(R.drawable.shape_rect_004);

        tabOrderTV.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrderTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void showOrdersUI() {
        productsRL.setVisibility(View.GONE);
        ordersRL.setVisibility(View.VISIBLE);

        tabProductTV.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrderTV.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrderTV.setBackgroundResource(R.drawable.shape_rect_004);

    }

    private void makeMeOfiline() {

        progressDialog.setMessage("making offline");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
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

                        Toast.makeText(MainSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {

            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            finish();
        } else {

            loadMyInfor();
        }
    }

    private void loadMyInfor() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            String name = "" + dataSnapshot.child("name").getValue();
                            String accountType = "" + dataSnapshot.child("accountType").getValue();

                            String email = "" + dataSnapshot.child("email").getValue();
                            String shopName = "" + dataSnapshot.child("shopName").getValue();
                            String profileImage = "" + dataSnapshot.child("profileImages").getValue();

                            //set data to ui
                            nameTV.setText(name);
                            shopNameTV.setText(shopName);
                            emailTV.setText(email);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIV);

                            } catch (Exception e) {

                                Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
                                profileIV.setImageResource(R.drawable.ic_store_gray);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}