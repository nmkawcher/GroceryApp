package com.kawcher.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kawcher.mygrocery.R;
import com.kawcher.mygrocery.adapters.AdapterOrderedItem;
import com.kawcher.mygrocery.models.ModelOrderdItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUserActivity extends AppCompatActivity {

    private String orderTo, orderId;

    //ui views
    private ImageButton backBtn,writeReviewBtn;
    private TextView orderIdTV, dateTV, orderStatusTV, shopNameTV, totalItemsTV,
            amountTV, addressTV;

    private RecyclerView itemsRV;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderdItem>orderdItemArrayList;

    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_user);

        //init views
        backBtn = findViewById(R.id.backBtn);
        writeReviewBtn=findViewById(R.id.writeReviewBtn);

        orderIdTV = findViewById(R.id.orderIdTV);
        dateTV = findViewById(R.id.dateTV);
        orderStatusTV = findViewById(R.id.orderStatusTV);
        shopNameTV = findViewById(R.id.shopNameTV);
        totalItemsTV = findViewById(R.id.totalItemsTV);
        amountTV = findViewById(R.id.amountTV);
        addressTV = findViewById(R.id.addressTV);


        itemsRV = findViewById(R.id.itemsRV);

        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo");//orderTo contains uid of the shop where  we placed order
        orderId = intent.getStringExtra("orderId");

        firebaseAuth = FirebaseAuth.getInstance();

        loadShopInfor();
        loadOrderDetails();
        loadOrdereditems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(OrderDetailsUserActivity.this,WriteReviewActivity.class);
                intent.putExtra("shopUid",orderTo);//to write review to shop we must have uid of shop
                startActivity(intent);
            }
        });

    }

    private void loadOrdereditems() {
        //init list
        orderdItemArrayList=new ArrayList<>();

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        orderdItemArrayList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){

                            ModelOrderdItem modelOrderdItem=ds.getValue(ModelOrderdItem.class);
                            //add to list

                            orderdItemArrayList.add(modelOrderdItem);
                        }

                        //all items added to list
                        //setup adapter
                        adapterOrderedItem=new AdapterOrderedItem(getApplicationContext(),orderdItemArrayList);
                        //set rv to adapter

                        itemsRV.setAdapter(adapterOrderedItem);

                        //set items count

                       totalItemsTV.setText(""+snapshot.getChildrenCount());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {

        //load order details

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String orderBy = "" + snapshot.child("orderBy").getValue();
                        String orderCost = "" + snapshot.child("orderCost").getValue();
                        String orderId = "" + snapshot.child("orderId").getValue();
                        String orderStatus = "" + snapshot.child("orderStatus").getValue();
                        String orderTime = "" + snapshot.child("orderTime").getValue();
                        String orderTo = "" + snapshot.child("orderTo").getValue();
                        String deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                        String latitude = "" + snapshot.child("latitude").getValue();
                        String longitude = "" + snapshot.child("longitude").getValue();

                        //convert timestamp to proper format

                        Calendar calendar=Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatedDate= DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString();//e.g 20/05/2020 12.01 pm

                        if(orderStatus.equals("In Progress")){

                            orderStatusTV.setTextColor(getResources().getColor(R.color.colorPrimary));

                        }
                        else if(orderStatus.equals("Completed")){
                            orderStatusTV.setTextColor(getResources().getColor(R.color.colorGreen));
                        } else if(orderStatus.equals("Cancelled")){
                            orderStatusTV.setTextColor(getResources().getColor(R.color.colorRed));
                        }

                        //set data

                        orderIdTV.setText(orderId);
                        orderStatusTV.setText(orderStatus);
                        amountTV.setText("$"+orderCost+"(including delivery fee $"+deliveryFee+")");
                        dateTV.setText(formatedDate);

                        findAddress(latitude,longitude);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void loadShopInfor() {
        //get shop info

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String  shopName=""+snapshot.child("shopName").getValue();
                        shopNameTV.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {


        double lat=Double.parseDouble(latitude);
        double lon=Double.parseDouble(longitude);

        //find address ,country,state,city

        Geocoder geocoder;
        List<Address>addresses;
        geocoder=new Geocoder(this, Locale.getDefault());

        try {
            addresses=geocoder.getFromLocation(lat,lon,1);
            String address=addresses.get(0).getAddressLine(0);//complete address
            addressTV.setText(address);
        } catch (Exception e){


        }


    }

}