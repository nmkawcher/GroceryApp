package com.kawcher.mygrocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kawcher.mygrocery.R;
import com.kawcher.mygrocery.activities.ShopDetailsActivity;
import com.kawcher.mygrocery.models.ModelCartItem;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context context;
    private ArrayList<ModelCartItem>cartitems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartitems) {
        this.context = context;
        this.cartitems = cartitems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_cart_item,parent,false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, final int position) {

        //get data

        ModelCartItem modelCartItem=cartitems.get(position);
        final String id=modelCartItem.getId();
        String getPid=modelCartItem.getpId();
        String  title=modelCartItem.getName();
        final String cost=modelCartItem.getCost();
        String price=modelCartItem.getPrice();
        String quantity=modelCartItem.getQuantity();

        //set data

        holder.itemTitleTV.setText(""+title);
        holder.itemPriceTV.setText(""+cost);
        holder.itemQuantityTV.setText("["+quantity+"]"); //e.g.[3]
        holder.itemPriceEachTV.setText(""+price);

        //handle  remove  click listener, delete item from cart

        holder.itemRemoveTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //will create table if not exist ,but in that case will must exist
                EasyDB  easyDB=EasyDB.init(context,"ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_id",new String[]{"text","unique"}))
                        .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1,id);//column number 1 is item_id
                Toast.makeText(context, "Removed from cart....", Toast.LENGTH_SHORT).show();

            //refresh list
                cartitems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double tx=Double.parseDouble((((ShopDetailsActivity)context).allTotalPriceTV.getText().toString().trim().replace("$","")));

                double totalPrice=tx-Double.parseDouble(cost.replace("$",""));
                double deliveryFee=Double.parseDouble((((ShopDetailsActivity)context).deliveryFee.replace("$","")));
                double sTotalPrice=Double.parseDouble(String.format("%.2f",totalPrice))-Double.parseDouble(String.format("%.2f",deliveryFee));
                ((ShopDetailsActivity)context).allTotalPrice=0.00;
                ((ShopDetailsActivity)context).sTotalTV.setText("$"+String.format("%.2f",sTotalPrice));
                ((ShopDetailsActivity)context).allTotalPriceTV.setText("$"+String.format("%.2f",Double.parseDouble(String.format("%.2f",totalPrice))));



                //after removinng item from cart,update cart count

                ((ShopDetailsActivity)context).cartCount();


            }
        });


    }

    @Override
    public int getItemCount() {
        return cartitems.size();
    }

    class HolderCartItem extends RecyclerView.ViewHolder{

        //ui views of  row_cartitems.xml

        private TextView itemTitleTV,itemPriceTV,itemPriceEachTV,itemQuantityTV,itemRemoveTV;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTV=itemView.findViewById(R.id.itemTitleTV);
            itemPriceTV=itemView.findViewById(R.id.itemPriceTV);
            itemPriceEachTV=itemView.findViewById(R.id.itemPriceEachTV);
            itemQuantityTV=itemView.findViewById(R.id.itemQuantityTV);
            itemRemoveTV=itemView.findViewById(R.id.itemRemoveTV);


        }
    }
}
