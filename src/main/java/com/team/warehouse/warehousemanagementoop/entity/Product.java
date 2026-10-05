package com.team.warehouse.warehousemanagementoop.entity;

public class Product {
    private long id;
    private String code;
    private String name;
    private long categoryId;
    private int quantity;
    private double price;
    private boolean isActive;

    // Constructors
    public Product() {
    }

    public Product(long id, String code, String name, long categoryId, int quantity, double price, boolean isActive) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.categoryId = categoryId;
        this.quantity = quantity;
        this.price = price;
        this.isActive = isActive;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Optional: toString method for easier debugging
//    @Override
//    public String toString() {
//        return "Product{" +
//                "id=" + id +
//                ", code='" + code + '\'' +
//                ", name='" + name + '\'' +
//                ", categoryId=" + categoryId +
//                ", quantity=" + quantity +
//                ", price=" + price +
//                ", isActive=" + isActive +
//                '}';
//    }
}