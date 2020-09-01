package com.kawcher.mygrocery;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct>productList,filterList;
    private FilterProduct filter;



    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {

        this.context = context;
        this.productList = productList;
        this.filterList=productList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {

        //get data
        ModelProduct modelProduct=productList.get(position);
        String id=modelProduct.getProductId();
        String uid=modelProduct.getUid();
        String  discountAvailable=modelProduct.getDiscountAvailable();
        String discountNote=modelProduct.getDiscountNote();
        String discountPrice=modelProduct.getDiscountPrice();
        String productCategory=modelProduct.getProductCategory();
        String  productDescription=modelProduct.getProductDescription();
        String icon=modelProduct.getProductIcon();
        String quantity=modelProduct.getProductQuantity();
        String title=modelProduct.getProductTitle();
        String timeStamp=modelProduct.getTimestamp();
        String originalPrice=modelProduct.getOriginalPrice();//set data

        holder.titleTV.setText(title);
        holder.quantityTV.setText(quantity);
        holder.discountedNoteTV.setText(discountNote);
        holder.discountedPriceTV.setText(discountPrice);
        holder.originalPriceTV.setText(originalPrice);

        if(discountAvailable.equals("true")){
            //product is on discount
            holder.discountedPriceTV.setVisibility(View.VISIBLE);
            holder.discountedNoteTV.setVisibility(View.VISIBLE);
            holder.originalPriceTV.setPaintFlags(holder.originalPriceTV.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);

        } else {

            //product is  not discount
            holder.discountedPriceTV.setVisibility(View.GONE);
            holder.discountedNoteTV.setVisibility(View.GONE);
        }

        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.productIconIV);
        } catch (Exception e){

            holder.productIconIV.setImageResource(R.drawable.ic_add_shopping_primary);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //add product details
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {

        if(filter==null){

            filter=new FilterProduct(this,filterList);

        }
        return null;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder {
        /* holds views of recycleviews */


        private ImageView productIconIV;
        private TextView discountedNoteTV,titleTV,quantityTV,discountedPriceTV,originalPriceTV;
        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIconIV=itemView.findViewById(R.id.productIconIV);
            discountedNoteTV=itemView.findViewById(R.id.discountedNoteTV);
            titleTV=itemView.findViewById(R.id.titleTV);
            quantityTV=itemView.findViewById(R.id.quantityTV);
            discountedPriceTV=itemView.findViewById(R.id.discountedPriceTV);
            originalPriceTV=itemView.findViewById(R.id.originalPriceTV);



        }
    }
}
