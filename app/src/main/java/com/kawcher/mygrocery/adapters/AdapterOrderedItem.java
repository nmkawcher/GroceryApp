package com.kawcher.mygrocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kawcher.mygrocery.R;
import com.kawcher.mygrocery.models.ModelCartItem;
import com.kawcher.mygrocery.models.ModelOrderdItem;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderItem>{

    private Context context;
    private ArrayList<ModelOrderdItem>orderedItemsArrayList;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrderdItem> orderedItemsArrayList) {
        this.context = context;
        this.orderedItemsArrayList = orderedItemsArrayList;
    }

    @NonNull
    @Override
    public HolderOrderItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_ordereditem,parent,false);
        return new HolderOrderItem(view);


    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderItem holder, int position) {

        //get data  at position
      ModelOrderdItem modelOrderdItem=orderedItemsArrayList.get(position);

      String pid=modelOrderdItem.getpId();
      String name=modelOrderdItem.getName();
      String  cost=modelOrderdItem.getCost();
      String price=modelOrderdItem.getPrice();
      String quantity=modelOrderdItem.getQuantity();

      //set data
        holder.itemTitleTV.setText(name);
        holder.itemPriceEachTV.setText("$"+price);
        holder.itemPriceTV.setText("$"+cost);
        holder.itemTitleTV.setText("["+quantity+"]");

    }

    @Override
    public int getItemCount() {
        return orderedItemsArrayList.size();
    }

    //view holder class
    class HolderOrderItem extends RecyclerView.ViewHolder{

        //views of row_ordereditem.xml
        private TextView itemTitleTV,itemPriceTV,itemPriceEachTV,itemQuantityTV;

        public HolderOrderItem(@NonNull View itemView) {
            super(itemView);

            //init views
            itemTitleTV=itemView.findViewById(R.id.itemTitleTV);
            itemPriceTV=itemView.findViewById(R.id.itemPriceTV);
            itemPriceEachTV=itemView.findViewById(R.id.itemPriceEachTV);
            itemQuantityTV=itemView.findViewById(R.id.itemQuantityTV);
        }
    }
}
