package com.kawcher.mygrocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kawcher.mygrocery.R;
import com.kawcher.mygrocery.activities.ShopDetailsActivity;
import com.kawcher.mygrocery.models.ModelShop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> {

    private Context context;
    private ArrayList<ModelShop>shopList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopList) {
        this.context = context;
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);

        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {

        //get data

        ModelShop  modelShop=shopList.get(position);

        String accountType=modelShop.getAccountType();
        String address=modelShop.getAddress();
        String city=modelShop.getCity();
        String country=modelShop.getCountry();
        String deliveryFee=modelShop.getDeliveryFee();
        String email=modelShop.getEmail();
        String latitude=modelShop.getLatitude();
        String longitude=modelShop.getLongitude();
        String online=modelShop.getOnline();
        String name=modelShop.getName();
        String phone=modelShop.getPhone();
        final String uid=modelShop.getUid();
        String timestamp=modelShop.getTimestamp();
        String shopOpen=modelShop.getShopOpen();
        String state=modelShop.getState();
        String profileImage=modelShop.getProfileImage();
        String shopName=modelShop.getShopName();

        loadReviews(modelShop,holder);//load avg rating,set to ratingbar
        //set Data

        holder.shopNameTV.setText(shopName);
        holder.phoneTV.setText(phone);
        holder.addressTV.setText(address);
        //check if online

        if(online.equals("true")){

            //shop owner is  online
            holder.onlineIV.setVisibility(View.VISIBLE);
        } else {

            //shop owner  is  offline
            holder.onlineIV.setVisibility(View.GONE);
        }

        //check if shop open

        if(shopOpen.equals("true")){

            //shop open
            holder.shopClosedTV.setVisibility(View.GONE);
        } else {

            //shop closed
            holder.shopClosedTV.setVisibility(View.VISIBLE);
        }

        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(holder.shopIV);
        } catch (Exception e){

            holder.shopIV.setImageResource(R.drawable.ic_store_gray);
        }

        //handle click listener, show shop details

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);
            }
        });
    }

    private float ratingSum=0;
    private void loadReviews(ModelShop modelShop, final HolderShop holder) {

        String  shopUid=modelShop.getUid();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
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

                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    @Override
    public int getItemCount() {
        return shopList.size();
    }

    //view  holder
    class HolderShop extends RecyclerView.ViewHolder{

        //ui views of row_shop.xml

        private ImageView shopIV,onlineIV;
        private TextView shopClosedTV,shopNameTV,phoneTV,addressTV;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            //init uid views

            shopIV=itemView.findViewById(R.id.shopIV);
            onlineIV=itemView.findViewById(R.id.onlineIV);
            shopClosedTV=itemView.findViewById(R.id.shopClosedTV);
            shopNameTV=itemView.findViewById(R.id.shopNameTV);
            phoneTV=itemView.findViewById(R.id.phoneTV);
            addressTV=itemView.findViewById(R.id.addressTV);
            ratingBar=itemView.findViewById(R.id.ratingBar);
        }
    }
}
