package com.sohail.transit.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.sohail.transit.Models.InfoWindowModel;
import com.sohail.transit.R;


//this class creates a custom info window for each marker
public class MarkerInfoAdapter implements GoogleMap.InfoWindowAdapter {

    Context context;

    public MarkerInfoAdapter(Context context) {
        this.context = context;

    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.marker_info_window, null);

        //setting up the views
        TextView stopTxt=view.findViewById(R.id.stopTxt);
        TextView incomingTxt=view.findViewById(R.id.incomingAtTxt);
        TextView congestionTxt=view.findViewById(R.id.congestionTxt);

        //get data from the marker
        InfoWindowModel model=(InfoWindowModel)marker.getTag();

        //editing the message so that its more understandable
        if(model.getStopTxt().equals("IN_TRANSIT_TO")){
            model.setStopTxt("Bus Status : In Transit");
        }else if(model.getStopTxt().equals("STOPPED_AT")){
            model.setStopTxt("Bus Status : Stopped");
        }

        //displaying the data.
        stopTxt.setText(model.getStopTxt());

        if(model.getCongestionTxt().equals("UNKNOWN_CONGESTION_LEVEL")){
            model.setCongestionTxt("Congestion level: Unknown");
        }

        incomingTxt.setText("Bus Number: "+ model.getIncomingTxt());
        congestionTxt.setText( model.getCongestionTxt());

        return view;
    }
}
