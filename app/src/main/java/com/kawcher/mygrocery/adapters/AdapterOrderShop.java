package com.kawcher.mygrocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kawcher.mygrocery.FilterOrderShop;
import com.kawcher.mygrocery.R;
import com.kawcher.mygrocery.activities.OrderDetailsSellerActivity;
import com.kawcher.mygrocery.models.ModelOrderShop;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop> implements Filterable {

   private Context context;
   public ArrayList<ModelOrderShop>orderShopArrayList,filterList;
   private FilterOrderShop filter;

    public AdapterOrderShop(Context context, ArrayList<ModelOrderShop> orderShopArrayList) {
        this.context = context;
        this.orderShopArrayList = orderShopArrayList;
        this.filterList=orderShopArrayList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_order_seller,parent,false);

        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {

        //get data at position

        ModelOrderShop modelOrderShop=orderShopArrayList.get(position);
        final String  orderId=modelOrderShop.getOrderId();
        final String orderBy=modelOrderShop.getOrderBy();
        String orderCost=modelOrderShop.getOrderCost();
        String  orderStatus=modelOrderShop.getOrderStatus();
        String orderTime=modelOrderShop.getOrderTime();
        String orderTo=modelOrderShop.getOrderTo();

        //load user/buyer info

        loadUserInfo(modelOrderShop,holder);
        //set data
        holder.amountTV.setText("Ammount: $"+orderCost);
        holder.statusTV.setText(orderStatus);
        holder.orderIdTV.setText("Order ID: "+orderId);

        //change order status text color
        if(orderStatus.equals("In Progress")){
            holder.statusTV.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        else if(orderStatus.equals("Completed")){

            holder.statusTV.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }

        else if(orderStatus.equals("Cancelled")){
            holder.statusTV.setTextColor(context.getResources().getColor(R.color.colorRed));
        }

        //convert time to proper format e.g dd/mm/yyyy

        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formateDate= DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.orderDateTV.setText(formateDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(context, OrderDetailsSellerActivity.class);
                intent.putExtra("orderId",orderId);
                intent.putExtra("orderBy",orderBy);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

    }

    private void loadUserInfo(ModelOrderShop modelOrderShop, final HolderOrderShop holder) {


        //to load email of the user  /buyer modelOrdershop.getOrderBy() contains uid of that user/buyer

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderShop.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String email=""+snapshot.child("email").getValue();
                        holder.emailTV.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



     }

    @Override
    public int getItemCount() {
        return orderShopArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            //init filter
            filter=new FilterOrderShop(this,filterList);

        }
        return filter;
    }

    class HolderOrderShop extends RecyclerView.ViewHolder{

        private TextView orderIdTV,orderDateTV,emailTV,amountTV,statusTV;

        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);

            orderIdTV=itemView.findViewById(R.id.orderIdTV);
            orderDateTV=itemView.findViewById(R.id.orderDateTV);
            emailTV=itemView.findViewById(R.id.emailTV);
            amountTV=itemView.findViewById(R.id.amountTV);
            statusTV=itemView.findViewById(R.id.statusTV);
            }
    }
}
