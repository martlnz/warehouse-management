package com.team.warehouse.warehousemanagementoop.util;

import com.team.warehouse.warehousemanagementoop.entity.User;

public class SessionManager {
    private static User currentUser;
    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser() { return currentUser; }
    public static void clear() { currentUser = null; }
}
