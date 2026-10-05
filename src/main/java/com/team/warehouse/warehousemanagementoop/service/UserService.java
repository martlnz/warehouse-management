package com.team.warehouse.warehousemanagementoop.service;

import com.team.warehouse.warehousemanagementoop.entity.User;
import com.team.warehouse.warehousemanagementoop.dao.UserDAO;
import java.util.List;

public class UserService {
    private final UserDAO userDAO = new UserDAO();
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }
    public void addUser(User user){
        userDAO.addUser(user);
    }
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }
        return userDAO.searchUsers(keyword.trim());
    }
}
