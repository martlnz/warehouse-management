package com.team.warehouse.warehousemanagementoop.entity;

public enum Role {
    ADMIN("Admin"),
    QUAN_LY_KHO("Quản lý kho"),
    NHAN_VIEN_KHO("Nhân viên kho");

    private final String displayName;
    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    @Override
    public String toString() {
        return displayName;
    }
    public static Role fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return NHAN_VIEN_KHO;
        }
        for (Role role : Role.values()) {
            if (role.displayName.equalsIgnoreCase(text) || role.name().equalsIgnoreCase(text)) {
                return role;
            }
        }
        return NHAN_VIEN_KHO;
    }
}
