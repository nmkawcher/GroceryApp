package com.kawcher.mygrocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import com.kawcher.mygrocery.adapters.AdapterCartItem;
import com.kawcher.mygrocery.adapters.AdapterProductUser;
import com.kawcher.mygrocery.adapters.AdapterReview;
import com.kawcher.mygrocery.models.ModelCartItem;
import com.kawcher.mygrocery.models.ModelProduct;
import com.kawcher.mygrocery.models.ModelReview;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    //delare ui views
    private ImageView shopIV;
    private TextView shopNameTV, phoneTV, emailTV, openCloseTV, deliveryFeeTV, addressTV, filteredproductsTV, cartCountTV;
    private ImageButton callBtn, mapBtn, addcartBtn, backBtn, filterProductBtn,showShopReviewIB;
    private EditText searchProductET;

    private RecyclerView productsRV;

    private RatingBar ratingBar;

    private String shopUid;
    private String myLatitude, myLongitude, myPhone;
    private String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;
    public String deliveryFee;
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;

    private AdapterProductUser adapterProductUser;

    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //inti ui views

        shopIV = findViewById(R.id.shopIV);
        shopNameTV = findViewById(R.id.shopNameTv);
        phoneTV = findViewById(R.id.phoneTv);
        emailTV = findViewById(R.id.emailTv);
        openCloseTV = findViewById(R.id.openCloseTv);
        deliveryFeeTV = findViewById(R.id.deliveryFeeTv);
        addressTV = findViewById(R.id.addressTV);
        filteredproductsTV = findViewById(R.id.filteredProductsTV);
        cartCountTV = findViewById(R.id.cartCountTV);

        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        addcartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        showShopReviewIB=findViewById(R.id.showShopReview);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        searchProductET = findViewById(R.id.searchProductET);

        productsRV = findViewById(R.id.productsRV);

        ratingBar=findViewById(R.id.ratingBar);

        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait....");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();//avg rating ,set on rating


        //declar it  to class level and  init in onCreate
        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();
        //each shop have its own products and orders so if  user add items to cart and got back and Open cart in Different shop then cart should be different
        //so delete cart data whenever user open this activity
        deleteCartData();
        cartCount();

        searchProductET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                try {
                    adapterProductUser.getFilter().filter(s.toString());
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addcartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //show cart dialog
                showCartDialog();
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialPhone();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openMap();
            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);

                builder.setTitle("Choose Category: ")
                        .setItems(Constatns.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //get selected item
                                String selected = Constatns.productCategories1[which];
                                filteredproductsTV.setText(selected);

                                if (selected.equals("All")) {
                                    //load All

                                    loadShopProducts();

                                } else {

                                    //load filtered

                                    adapterProductUser.getFilter().filter(selected);
                                }

                            }
                        })
                        .show()
                ;
            }
        });

        showShopReviewIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(ShopDetailsActivity.this,ShowShopReviewsActivity.class);
                intent.putExtra("shopUid",shopUid);
                startActivity(intent);
            }
        });

    }
    private float ratingSum=0;
    private void loadReviews() {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        ratingSum=0;
                        for(DataSnapshot ds:snapshot.getChildren()){

                            float rating=Float.parseFloat(""+ds.child("ratings").getValue());//e.g 4.3
                            ratingSum=ratingSum+rating;//for average rating,ad(addition of) all ratings,later will divide it  by number of reviews

                              }

                        long numberOfReviews=snapshot.getChildrenCount();
                        float avgRating=ratingSum/numberOfReviews;

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void deleteCartData() {


        easyDB.deleteAllDataFromTable();//delete all records from cart

    }

    public void cartCount() {

        //keep it public so we can access in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count <= 0) {
            //no item in cart,hide cart count textview
            cartCountTV.setVisibility(View.GONE);
        } else {

            //have items in cart, s how cart count textview and set count
            cartCountTV.setVisibility(View.VISIBLE);
            cartCountTV.setText("" + count);//concaten with string,because we can't set integer in textview

        }
    }

    public double allTotalPrice = 0.00;
    //need to access these views in adapter so making public

    public TextView sTotalTV, dFeeTV, allTotalPriceTV;

    private void showCartDialog() {

        //init list
        cartItemList = new ArrayList<ModelCartItem>();

        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        //init views

        TextView shopNameTV = view.findViewById(R.id.shopNameTV);
        final RecyclerView cartItemsRV = view.findViewById(R.id.cartItemsRV);
        sTotalTV = view.findViewById(R.id.sTotalTV);
        dFeeTV = view.findViewById(R.id.dFeeTV);
        allTotalPriceTV = view.findViewById(R.id.totalTV);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog

        builder.setView(view);

        shopNameTV.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //get all data from db

        Cursor res = easyDB.getAllData();
        while ((res.moveToNext())) {

            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);
            ModelCartItem modelCartItem = new ModelCartItem(
                    "" + id,
                    "" + pId,
                    "" + name,
                    "" + price,
                    "" + cost,
                    "" + quantity

            );

            cartItemList.add(modelCartItem);
        }

        //set up adapter

        adapterCartItem = new AdapterCartItem(this, cartItemList);
        //set to recyclerview
        cartItemsRV.setAdapter(adapterCartItem);

        dFeeTV.setText("$" + deliveryFee);
        sTotalTV.setText("$" + String.format("%.2f", allTotalPrice));
        allTotalPriceTV.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("$", ""))));

        //reset total price on dialog dismiss

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first validate delivery address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in your profile before placing order...", Toast.LENGTH_SHORT).show();
                    return; //don't proceede farther
                }
                if (myPhone.equals("") || myPhone.equals("null")) {
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your phone number in your profile before placing order...", Toast.LENGTH_SHORT).show();
                    return; //don't proceede farther
                }
                if (cartItemList.size() == 0) {

                    //cart list is empty
                    Toast.makeText(ShopDetailsActivity.this, "No item in cart", Toast.LENGTH_SHORT).show();
                    return;//don't proceed further
                }

                submitOrder();
            }
        });

    }

    private void submitOrder() {

        //show progress dialog
        progressDialog.setMessage("Placing order..");
        progressDialog.show();

        //for order id and order time

        final String timestamp = "" + System.currentTimeMillis();
        String cost = allTotalPriceTV.getText().toString().trim().replace("$", "");//remove $ if contains

       //add latitude ,longitude of  user to each order i delete | delete previous  orders from firebase or a dd manually to them


        //setup order data

        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" +timestamp);
        hashMap.put("orderTime", "" +timestamp);
        hashMap.put("orderStatus", "In Progress");//In progress/completed/cancelled
        hashMap.put("orderCost", "" + cost);
        hashMap.put("orderBy", ""+firebaseAuth.getUid());
        hashMap.put("orderTo", ""+shopUid);
        hashMap.put("latitude", ""+myLatitude);
        hashMap.put("longitude", ""+myLongitude);

        //add to db

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //order info added now add order items
                        for (int i = 0; i < cartItemList.size(); i++) {

                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String name = cartItemList.get(i).getName();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }

                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed Successfully", Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(timestamp);



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void openMap() {

        //saddr means source addess
        ///daddr means destination address

        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {

        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            String name = "" + dataSnapshot.child("name").getValue();
                            String email = "" + dataSnapshot.child("email").getValue();
                            myPhone = "" + dataSnapshot.child("phone").getValue();
                            String profileImage = "" + dataSnapshot.child("profileImage").getValue();
                            String city = "" + dataSnapshot.child("city").getValue();
                            String accountType = "" + dataSnapshot.child("accountType").getValue();
                            myLatitude = "" + dataSnapshot.child("latitude").getValue();
                            myLongitude = "" + dataSnapshot.child("longitude").getValue();


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadShopDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = "" + snapshot.child("name").getValue();
                shopName = "" + snapshot.child("shopName").getValue();
                shopEmail = "" + snapshot.child("email").getValue();
                shopPhone = "" + snapshot.child("phone").getValue();
                shopLatitude = "" + snapshot.child("latitude").getValue();
                shopLongitude = "" + snapshot.child("longitude").getValue();
                shopAddress = "" + snapshot.child("address").getValue();
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                String shopOpen = "" + snapshot.child("shopOpen").getValue();


                //set data

                shopNameTV.setText(shopName);
                emailTV.setText(shopEmail);
                deliveryFeeTV.setText("DeliveryFee: $" + deliveryFee);
                addressTV.setText(shopAddress);
                phoneTV.setText(shopPhone);

                if (shopOpen.equals("true")) {

                    openCloseTV.setText("Open");
                } else {

                    openCloseTV.setText("Closed");
                }

                try {
                    Picasso.get().load(profileImage).into(shopIV);

                } catch (Exception e) {


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopProducts() {

        productList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //clear list before adding  items

                        productList.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            ModelProduct modelProduct = dataSnapshot.getValue(ModelProduct.class);

                            productList.add(modelProduct);
                        }

                        //set up adapter

                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productList);
                        //set Adapter

                        productsRV.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void prepareNotificationMessage(String orderId){

        //when user  places order, send notification to seller

        //prepare data for notification

        String NOTIFICATION_TOPIC="/topics/"+Constatns.FCM_TOPIC ;

        String  NOTIFICATION_TITLE="New Order"+orderId;
        String NOTIFICATION_MESSAGE="Congratulations...! You have new order.";
        String NOTIFICATION_TYPE="NewOrder";

        //prepare json (what to send and where to send)

        JSONObject notificationJo=new JSONObject();
        JSONObject notificationBodyJo=new JSONObject();

        try{
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",firebaseAuth.getUid());//since we a re  logged in as buyer to place order so current user  uid is  buyer is buyer  uid
            notificationBodyJo.put("sellerUid",shopUid);
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notifictionMessage",NOTIFICATION_MESSAGE);

            //where to send
            notificationBodyJo.put("to",NOTIFICATION_TOPIC);//to all who subscribed to this topic
            notificationBodyJo.put("data",notificationBodyJo);

        }catch (Exception e){

            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo,orderId);


    }

    private void sendFcmNotification(JSONObject notificationJo, final String orderId) {

        //send volley request
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //after sending fcm start order details activity

                Intent intent = new Intent(getApplicationContext(), OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                //after sending fcm start order details activity

                //after placing order open order  details page
                Intent intent = new Intent(getApplicationContext(), OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
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