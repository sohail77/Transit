package com.sohail.transit.Models;

public class BusSelectorModel {

    //Model class to maintain the state of recycler view's checkboxes
    boolean isSelected;

    public BusSelectorModel() {
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
