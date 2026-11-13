
package com.scc.model;

public class Order {

    private final int orderId;
    private final String customerName;
    private final String cakeName;
    private final int quantity;
    private final String celebrationDate;
    private final String pickupOrDelivery;
    private final String address;
    private final double totalAmount;
    private final String orderStatus;
    private final String orderDate;

    public Order(
            int orderId,
            String customerName,
            String cakeName,
            int quantity,
            String celebrationDate,
            String pickupOrDelivery,
            String address,
            double totalAmount,
            String orderStatus,
            String orderDate) {

        this.orderId = orderId;
        this.customerName = customerName;
        this.cakeName = cakeName;
        this.quantity = quantity;
        this.celebrationDate = celebrationDate;
        this.pickupOrDelivery = pickupOrDelivery;
        this.address = address;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCakeName() {
        return cakeName;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCelebrationDate() {
        return celebrationDate;
    }

    public String getPickupOrDelivery() {
        return pickupOrDelivery;
    }

    public String getAddress() {
        return address;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getOrderDate() {
        return orderDate;
    }
}

