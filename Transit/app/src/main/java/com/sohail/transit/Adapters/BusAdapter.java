package com.sohail.transit.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sohail.transit.Models.BusSelectorModel;
import com.sohail.transit.Models.RoutesModel;
import com.sohail.transit.R;
import java.util.ArrayList;

//This class creates a custom adapter for the selecting the buses
public class BusAdapter extends RecyclerView.Adapter<BusAdapter.ViewHolder> {

    Context context;
    //this list contains list of routes from the routes.csv file
    ArrayList<RoutesModel> list=new ArrayList<>();

    //this list will return all the selected buses when asked
    ArrayList<String> wantedRoutes=new ArrayList<>();

    //this list will make sure the check boxes are maintained because of Recycler views behaviour
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


        viewHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                viewHolder.checkBox.toggle();
                if(viewHolder.checkBox.isChecked()){
                    //store the routes that are checked
                    wantedRoutes.add(list.get(i).getRouteId());
                }else{

                    //remove the routes from the list if unchecked
                    wantedRoutes.remove(list.get(i).getRouteId());
                }
                isCheckedList.get(i).setSelected(viewHolder.checkBox.isChecked());

            }
        });
    }

    //returns all the routes that are checked
    public ArrayList<String> giveMeRoutes(){
        return wantedRoutes;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        //setting up the views
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
