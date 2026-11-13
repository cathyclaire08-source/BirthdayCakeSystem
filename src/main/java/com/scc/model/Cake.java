
package com.scc.model;

public class Cake {

    private int cakeId;
    private String cakeName;
    private String flavor;
    private String size;
    private double price;
    private String availabilityStatus;

    public Cake(
            int cakeId,
            String cakeName,
            String flavor,
            String size,
            double price,
            String availabilityStatus) {

        this.cakeId = cakeId;
        this.cakeName = cakeName;
        this.flavor = flavor;
        this.size = size;
        this.price = price;
        this.availabilityStatus = availabilityStatus;
    }

    public int getCakeId() {
        return cakeId;
    }

    public String getCakeName() {
        return cakeName;
    }

    public String getFlavor() {
        return flavor;
    }

    public String getSize() {
        return size;
    }

    public double getPrice() {
        return price;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }
}

