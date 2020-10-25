package com.maseletrico.remotecontrolkey;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by root on 14/02/18.
 */

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List mDataset;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextViewBtname;
        public ImageView imageBtCharacteristic;
        public ImageView imageIconCar;

        public ViewHolder(View v, final OnItemClickListener listener) {
            super(v);
            mTextViewBtname = v.findViewById(R.id.textView_bt_name);
            imageBtCharacteristic = v.findViewById(R.id.iv_bt_hardware);

//            imageBtCharacteristic.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log.i("OnClick ","ok");
//
//                }
//            });

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }

                    }
                }
            });

        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List myDataset) {
        mDataset = myDataset;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetooth_info, parent, false);
        // set the view's size, margins, paddings and layout parameters

        final ViewHolder vh = new ViewHolder(v,mListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Log.i("RECYCLER ", (String) mDataset.get(position));
        holder.mTextViewBtname.setText(mDataset.get(position).toString());
        //holder.imageBtCharacteristic.setImageResource(R.mipmap.ic_car;
        holder.imageBtCharacteristic.setVisibility(View.GONE);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
