package com.kawcher.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kawcher.mygrocery.Constatns;
import com.kawcher.mygrocery.R;
import com.kawcher.mygrocery.adapters.AdapterOrderedItem;
import com.kawcher.mygrocery.models.ModelOrderdItem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    private ImageButton backBtn, editBtn, mapBtn;

    private TextView orderIdTV, dateTV, orderStatusTV, emailTV, phoneTV, totalItemsTV,
            amountTV, addressTV;

    private RecyclerView itemsRV;

    String orderId, orderBy;

    //to open destination in map
    String sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude;

    private FirebaseAuth firebaseAuth;


    private ArrayList<ModelOrderdItem> orderdItemArrayList;

    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);

        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        mapBtn = findViewById(R.id.mapBtn);

        orderIdTV = findViewById(R.id.orderIdTV);
        dateTV = findViewById(R.id.dateTV);
        orderStatusTV = findViewById(R.id.orderStatusTV);
        emailTV = findViewById(R.id.emailTV);
        phoneTV = findViewById(R.id.phoneTV);
        totalItemsTV = findViewById(R.id.totalItemsTV);
        amountTV = findViewById(R.id.amountTV);
        addressTV = findViewById(R.id.addressTV);

        itemsRV = findViewById(R.id.itemsRV);


        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");


        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrdereditems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openMap();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editOrderStatusDialog();
            }
        });
    }

    private void editOrderStatusDialog() {

        //option to display in dialog

        final String[]options={"In Progress","Completed","Cancelled"};
        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //handle item click
                        String selectedOption=options[which];
                        editOrderStatus(selectedOption);
                    }
                })
                .show();

    }

    private void editOrderStatus(final String selectedOption) {

        //set up data to put  in firebase db
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("orderStatus",""+selectedOption);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        String message="Order is now"+selectedOption;
                        //status updated
                        Toast.makeText(OrderDetailsSellerActivity.this, "Order is now "+selectedOption, Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(orderId,message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OrderDetailsSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void loadOrderDetails() {

        //load order details

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
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

    private void findAddress(String latitude, String longitude) {


        double lat=Double.parseDouble(latitude);
        double lon=Double.parseDouble(longitude);

        //find address ,country,state,city

        Geocoder geocoder;
        List<Address> addresses;
        geocoder=new Geocoder(this, Locale.getDefault());

        try {
            addresses=geocoder.getFromLocation(lat,lon,1);
            String address=addresses.get(0).getAddressLine(0);//complete address
            addressTV.setText(address);
        } catch (Exception e){


        }


    }
    private void loadOrdereditems() {
        //init list
        orderdItemArrayList=new ArrayList<>();

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
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
    private void openMap() {

        //saddr means source addess
        ///daddr means destination address

        String address = "https://maps.google.com/maps?saddr=" + sourceLatitude + "," + sourceLongitude + "&daddr=" + destinationLatitude + "," + destinationLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        sourceLatitude = "" + snapshot.child("latitude").getValue();
                        sourceLongitude = "" + snapshot.child("longitude").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        destinationLatitude = "" + snapshot.child("latitude").getValue();
                        destinationLongitude = "" + snapshot.child("longitude").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String phone = "" + snapshot.child("phone").getValue();

                        emailTV.setText(email);
                        phoneTV.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void prepareNotificationMessage(String orderId,String message){

        //when user seller change  order  status InProgress/Cancelled/completed, send notification to buyer


        //prepare data for notification

        String NOTIFICATION_TOPIC="/topics/"+Constatns.FCM_TOPIC ;

        String  NOTIFICATION_TITLE="Your Order"+orderId;
        String NOTIFICATION_MESSAGE=""+message;
        String NOTIFICATION_TYPE="OrderStatusChanged";

        //prepare json (what to send and where to send)

        JSONObject notificationJo=new JSONObject();
        JSONObject notificationBodyJo=new JSONObject();

        try{
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",orderBy);
            notificationBodyJo.put("sellerUid",firebaseAuth.getUid());//since we are logged in as seller to change  order status so current user uid is seller uid
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notifictionMessage",NOTIFICATION_MESSAGE);

            //where to send
            notificationBodyJo.put("to",NOTIFICATION_TOPIC);//to all who subscribed to this topic
            notificationBodyJo.put("data",notificationBodyJo);

        }catch (Exception e){

            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo);


    }

    private void sendFcmNotification(JSONObject notificationJo) {

        //send volley request
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //notification send
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //after sending fcm start order details activity

                //after placing order open order  details page

            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //put request headers
                Map<String,String>headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key"+Constatns.FCM_KEY);

                return headers;
            }
        };

        //enque the  volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}