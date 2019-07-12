package com.sohail.transit.Models;

public class RoutesModel {

    //Model class to get the routes from the csv.
    String routeId,name;

    public RoutesModel() {
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
