package com.kawcher.mygrocery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct>productList,filterList;
    private FilterProduct filter;



    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList=new ArrayList<>(productList);
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
        final ModelProduct modelProduct=productList.get(position);
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
        String originalPrice=modelProduct.getOriginalPrice();


        //set data

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

                detailsBottomSheet(modelProduct);//here model product details of clicked product
            }
        });
    }

    private void detailsBottomSheet(ModelProduct modelProduct) {
        //bottom sheet

        final BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(context);
        //inflate view for bottomsheet
        View view=LayoutInflater.from(context).inflate(R.layout.bs_product_details,null);
        //set v iew to bottomsheet
        bottomSheetDialog.setContentView(view);
        //init views of bottom sheet
        ImageButton backBtn=view.findViewById(R.id.backBtn);
        ImageButton deleteBtn=view.findViewById(R.id.deletedBtn);
        ImageButton editBtn=view.findViewById(R.id.editBtn);
        ImageView productIconIV=view.findViewById(R.id.productIconIV);
        TextView discountNoteTV=view.findViewById(R.id.discountNoteTV);
        TextView titleTV=view.findViewById(R.id.titleTV);
        TextView descriptionTV=view.findViewById(R.id.descriptionTV);
        TextView categoryTV=view.findViewById(R.id.categoryTV);
        TextView quantityTV=view.findViewById(R.id.quantityTV);
        TextView discountedPriceTV=view.findViewById(R.id.discountedPriceTV);
        TextView originalPriceTV=view.findViewById(R.id.originalPriceTV);

        //get data

        final String id=modelProduct.getProductId();
        String uid=modelProduct.getUid();
        String  discountAvailable=modelProduct.getDiscountAvailable();
        String discountNote=modelProduct.getDiscountNote();
        String discountPrice=modelProduct.getDiscountPrice();
        String productCategory=modelProduct.getProductCategory();
        String  productDescription=modelProduct.getProductDescription();
        String icon=modelProduct.getProductIcon();
        String quantity=modelProduct.getProductQuantity();
        final String title=modelProduct.getProductTitle();
        String timeStamp=modelProduct.getTimestamp();
        String originalPrice=modelProduct.getOriginalPrice();

        //set data

        titleTV.setText(title);
        descriptionTV.setText(productDescription);
        categoryTV.setText(productCategory);
        quantityTV.setText(quantity);
        discountNoteTV.setText(discountNote);
        discountedPriceTV.setText("$ "+discountPrice);
        originalPriceTV.setText("$ "+originalPrice);


        if(discountAvailable.equals("true")){
            //product is on discount
            discountedPriceTV.setVisibility(View.VISIBLE);
            discountNoteTV.setVisibility(View.VISIBLE);
           originalPriceTV.setPaintFlags(originalPriceTV.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);

        } else {

            //product is  not discount
           discountedPriceTV.setVisibility(View.GONE);
           discountNoteTV.setVisibility(View.GONE);
        }

        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(productIconIV);
        } catch (Exception e){

            productIconIV.setImageResource(R.drawable.ic_add_shopping_primary);
        }

        //show  dialog
        bottomSheetDialog.show();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottomSheetDialog.dismiss();

            }
        });

        //edit button
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open eidt product activity pass id of  product

                bottomSheetDialog.dismiss();
                Intent intent=new Intent(context,EditProductActivity.class);
                intent.putExtra("productId",id);
                context.startActivity(intent);



            }
        });

        //delete click

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //show delete confirm dialoge

                bottomSheetDialog.dismiss();

                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete product "+title+" )")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //delete
                                deleteProduct(id);//id is  the  product id
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel, dismiss dialog
                                dialog.dismiss();
                            }
                        }).show();


            }
        });




    }

    private void deleteProduct(String id) {

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //product deleted
                        Toast.makeText(context, "Product deleted...", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //failed deleting product
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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
        return filter;
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
