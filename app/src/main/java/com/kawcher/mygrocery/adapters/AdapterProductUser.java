package com.kawcher.mygrocery.adapters;

import android.app.AlertDialog;
import android.content.Context;
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

import com.kawcher.mygrocery.FilterProductUser;
import com.kawcher.mygrocery.R;
import com.kawcher.mygrocery.activities.ShopDetailsActivity;
import com.kawcher.mygrocery.models.ModelProduct;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser  extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct>productList,filterList;
    private FilterProductUser filter;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList=productList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View  view= LayoutInflater.from(context).inflate(R.layout.row_product_user,parent,false);

        return  new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {

        //get data

        final ModelProduct modelProduct=productList.get(position);

        String discountAvailable=modelProduct.getDiscountAvailable();
        String discountNote=modelProduct.getDiscountNote();
        String  originalPrice=modelProduct.getOriginalPrice();
        String discountPrice=modelProduct.getDiscountPrice();
        String productCategory=modelProduct.getProductCategory();
        String productDescription=modelProduct.getProductDescription();
        String productTitle=modelProduct.getProductTitle();
        String productQuantity=modelProduct.getProductQuantity();
        String  productId=modelProduct.getProductId();
        String  timestamp=modelProduct.getTimestamp();
        String  productIcon=modelProduct.getProductIcon();

        //set data

        holder.titleTV.setText(productTitle);
        holder.discountedNoteTV.setText(discountNote);
        holder.descriptionTV.setText(productDescription);
        holder.originalPriceTV.setText("$"+originalPrice);
        holder.discountedPriceTV.setText("$"+discountPrice);

        if(discountAvailable.equals("true")){

            //product is on discount
            holder.discountedPriceTV.setVisibility(View.VISIBLE);
            holder.discountedNoteTV.setVisibility(View.VISIBLE);
            holder.originalPriceTV.setPaintFlags(holder.originalPriceTV.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);//add.
        } else {

            holder.discountedPriceTV.setVisibility(View.GONE);
            holder.discountedNoteTV.setVisibility(View.GONE);
            holder.originalPriceTV.setPaintFlags(0);
        }

        try {
            Picasso.get().load(productIcon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.productIconIV);
        } catch (Exception e){

            holder.productIconIV.setImageResource(R.drawable.ic_add_shopping_primary);

        }

        holder.addToCartTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //add product to cart
                showQuantityDialog(modelProduct);

            }
        });
    }

    private double cost=0;
    private double finalCost=0;
    private int quantity=0;
    private void showQuantityDialog(ModelProduct modelProduct) {

        //inflate layout for  dialog

        View view=LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);

        //init views

        ImageView productIV=view.findViewById(R.id.productIV);
        final TextView titleTV=view.findViewById(R.id.titleTV);
        TextView pQuantityTV=view.findViewById(R.id.pQuantityTV);
        TextView descriptionTV=view.findViewById(R.id.descriptionTV);
        TextView discountedNoteTV=view.findViewById(R.id.discountedNoteTV);
        final TextView originalPriceTV=view.findViewById(R.id.originalPriceTV);
        TextView priceDiscountedTV=view.findViewById(R.id.priceDiscountedTV);
        final TextView finalPriceTV=view.findViewById(R.id.finalPriceTV);
        ImageButton decrementBtn=view.findViewById(R.id.decrementBtn);
        final TextView quantityTV=view.findViewById(R.id.quantityTV);
        ImageButton incrementBtn=view.findViewById(R.id.incrementBtn);
        TextView continueBtn=view.findViewById(R.id.continueBtn);

        //get data from model

        final String productId=modelProduct.getProductId();
        String title=modelProduct.getProductTitle();
        String productQuantity=modelProduct.getProductQuantity();
        String description=modelProduct.getProductDescription();
        String  discountNote=modelProduct.getDiscountNote();
        String image=modelProduct.getProductIcon();

        final String price;

        if(modelProduct.getDiscountAvailable().equals("true")){
            //product have discount

            price=modelProduct.getDiscountPrice();
            discountedNoteTV.setVisibility(View.VISIBLE);
            originalPriceTV.setPaintFlags(originalPriceTV.getPaintFlags()|Paint.CURSOR_AT);//add strike through on original price

        } else {

            //product don't have discount
            discountedNoteTV.setVisibility(View.GONE);
            priceDiscountedTV.setVisibility(View.GONE);
            price=modelProduct.getOriginalPrice();
        }

        cost=Double.parseDouble(price.replaceAll("$",""));
        finalCost=Double.parseDouble(price.replaceAll("$",""));
        quantity=1;

        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setView(view);

        //set data

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_cart_gray).into(productIV);
        } catch (Exception e){

            productIV.setImageResource(R.drawable.ic_cart_gray);
        }

            titleTV.setText(""+title);
            pQuantityTV.setText(""+productQuantity);
            descriptionTV.setText(""+description);
            discountedNoteTV.setText(""+discountNote);
            quantityTV.setText(""+quantity);
            originalPriceTV.setText("$"+modelProduct.getOriginalPrice());
            priceDiscountedTV.setText("$"+modelProduct.getDiscountPrice());
            finalPriceTV.setText("$"+finalCost);

            final AlertDialog dialog=builder.create();
            dialog.show();

            //increase quantity of  the product

            incrementBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finalCost=finalCost+cost;
                    quantity++;

                    finalPriceTV.setText("$"+finalCost);
                    quantityTV.setText(""+quantity);


                }
            });

            //decrement quantity of product , only if quantity is>1
        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(quantity>1){

                    finalCost=finalCost-cost;
                    quantity--;

                    finalPriceTV.setText("$"+finalCost);
                    quantityTV.setText(""+quantity);
                }
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title=titleTV.getText().toString().trim();
                String  priceEach=price;
                String  totalPrice=finalPriceTV.getText().toString().trim().replace("$","");
                String  quantity=quantityTV.getText().toString().trim();

                //add to db(Sqlite)

                addToCart(productId,title,priceEach,totalPrice,quantity);

                dialog.dismiss();
            }
        });



    }

    private int  itemId=1;
    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {

       itemId++;

        EasyDB  easyDB=EasyDB.init(context,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_id",new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        Boolean b=easyDB.addData("Item_id",itemId)
                .addData("Item_PID",productId)
                .addData("Item_Name",title)
                .addData("Item_Price_Each",priceEach)
                .addData("Item_Price",price)
                .addData("Item_Quantity",quantity)
                .doneDataAdding();

        Toast.makeText(context,"Added to cart...",Toast.LENGTH_SHORT).show();


        //update cart count
        ((ShopDetailsActivity)context).cartCount();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {

        if(filter==null){
            filter=new FilterProductUser(this,filterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder{

        private ImageView productIconIV;
        private TextView discountedNoteTV,titleTV,descriptionTV,addToCartTV,discountedPriceTV,originalPriceTV;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);

            productIconIV=itemView.findViewById(R.id.productIconIV);
            discountedNoteTV=itemView.findViewById(R.id.discountedNoteTV);
            titleTV=itemView.findViewById(R.id.titleTV);
            descriptionTV=itemView.findViewById(R.id.descriptionTV);
            addToCartTV=itemView.findViewById(R.id.addToCartTV);
            discountedPriceTV=itemView.findViewById(R.id.discountedPriceTV);
            originalPriceTV=itemView.findViewById(R.id.originalPriceTV);



        }
    }
}
