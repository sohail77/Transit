package com.sohail.transit.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sohail.transit.Models.BusSelectorModel;
import com.sohail.transit.Models.RoutesModel;
import com.sohail.transit.R;


import java.util.ArrayList;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.ViewHolder> {

    Context context;
    ArrayList<RoutesModel> list=new ArrayList<>();
    ArrayList<String> wantedRoutes=new ArrayList<>();
    ArrayList<BusSelectorModel> isCheckedList=new ArrayList<>();

    public BusAdapter(Context context, ArrayList<RoutesModel> list) {
        this.context = context;
        this.list = list;
        for(int i=0;i<list.size();i++){
            BusSelectorModel m=new BusSelectorModel();
            m.setSelected(false);
            isCheckedList.add(m);
        }
    }

    @NonNull
    @Override
    public BusAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bus_item,viewGroup,false);
        return new BusAdapter.ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull final BusAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.name.setText(list.get(i).getRouteId() + ", " + list.get(i).getName());

       viewHolder.checkBox.setChecked(isCheckedList.get(i).isSelected());

//
//        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//                if(isChecked){
//                    wantedRoutes.add(list.get(i).getRouteId());
//                }else{
//                    wantedRoutes.remove(list.get(i).getRouteId());
//                }
//            }
//        });


        viewHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                viewHolder.checkBox.toggle();
                if(viewHolder.checkBox.isChecked()){
                    wantedRoutes.add(list.get(i).getRouteId());
                }else{
                    wantedRoutes.remove(list.get(i).getRouteId());
                }
                isCheckedList.get(i).setSelected(viewHolder.checkBox.isChecked());


//                if(viewHolder.checkBox.isChecked()){
//                    isCheckedList.get(i).setSelected(true);
//                    viewHolder.checkBox.toggle();
//
//                }else{
//                    isCheckedList.get(i).setSelected(false);
//                    viewHolder.checkBox.toggle();
//
//                }
            }
        });
    }

    public ArrayList<String> giveMeRoutes(){
        return wantedRoutes;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout item;
        TextView name;
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item=itemView.findViewById(R.id.checkItem);
            name=itemView.findViewById(R.id.nameTxt);
            checkBox=itemView.findViewById(R.id.busCheck);
        }
    }
}
