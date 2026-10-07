package com.team.warehouse.warehousemanagementoop.entity;

public class Customer {
    private Long id;
    private String name;
    private String phone;
    private String email;

    // Constructor mặc định
    public Customer() {}

    // Constructor dùng khi thêm mới (chưa có ID)
    public Customer(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Constructor đầy đủ (dùng khi lấy từ Database lên)
    public Customer(Long id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Các Getter và Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}