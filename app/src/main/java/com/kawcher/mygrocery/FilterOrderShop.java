package com.kawcher.mygrocery;

import android.widget.Filter;

import com.kawcher.mygrocery.adapters.AdapterOrderShop;
import com.kawcher.mygrocery.adapters.AdapterProductSeller;
import com.kawcher.mygrocery.models.ModelOrderShop;
import com.kawcher.mygrocery.models.ModelProduct;

import java.util.ArrayList;
import java.util.Collection;

public class FilterOrderShop extends Filter {

    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop>filterList;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();
        //validate data for  search query
        if(constraint!=null && constraint.length()>0){

            //search filed not empty, searching , perform search

            //change to upper case, to make  case insensitive
            constraint=constraint.toString().toUpperCase();
            //store our filtered list

            ArrayList<ModelOrderShop>filteredModels=new ArrayList<>();
            for(int  i=0;i<filterList.size();i++){
                //check
                if(filterList.get(i).getOrderStatus().toUpperCase().contains(constraint) ){

                    //add filtered data to list
                    filteredModels.add(filterList.get(i));

                }

            }

            results.count=filteredModels.size();
            results.values=filteredModels;


        }

        else {
            //search filed empty, not searching ,return original/all/complete list

            results.count=filterList.size();
            results.values=filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.orderShopArrayList= new ArrayList<ModelOrderShop>((Collection<? extends ModelOrderShop>) results.values);
        //refresh adapter
        adapter.notifyDataSetChanged();

    }
}
