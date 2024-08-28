package com.example.myapplication.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Helper.ServiceUtils;
import com.example.myapplication.Models.ListModel;
import com.example.myapplication.UI.MediaPlayerActivity;
import com.example.myapplication.databinding.RowBinding;
import com.example.myapplication.service.BackgroundService;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.myViewHolder> implements Filterable {


    Context context;
    ArrayList<ListModel> listModels;
    ArrayList<ListModel> filterlist;



    public Adapter(Context context, ArrayList<ListModel> arrayList){

        this.context = context;
        this.listModels = arrayList;
        this.filterlist = listModels;
    }


    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RowBinding rowBinding = RowBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new myViewHolder(rowBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        ListModel model = listModels.get(position);
        holder.rowBinding.title.setText(model.getTitle());
        holder.rowBinding.name.setText(model.getName());


//        holder.rowBinding.image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                context.startActivity(new Intent(context, MediaPlayerActivity.class));
//            }
//        });

        holder.rowBinding.cardlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, MediaPlayerActivity.class));

                if (ServiceUtils.isServiceRunning(context, BackgroundService.class)){

                    context.stopService(MediaPlayerActivity.serviceIntent);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return filterlist.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                FilterResults filterResults = new FilterResults();
                if (charSequence == null || charSequence.length() == 0) {
                    filterResults.values = listModels;
                } else {
                    ArrayList<ListModel> filteredList = new ArrayList<>();
                    for (ListModel item : listModels) {
                        if (item.getTitle().contains(charSequence.toString().toLowerCase())) {
                            filteredList.add(item);

                        }


                        filterResults.values = filteredList;
                    }


                }
                return filterResults;
            }


            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filterlist = (ArrayList<ListModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class myViewHolder extends RecyclerView.ViewHolder{


        RowBinding rowBinding;
        public myViewHolder(@NonNull RowBinding rowBinding) {
            super(rowBinding.getRoot());
            this.rowBinding = rowBinding;
        }
    }
}
