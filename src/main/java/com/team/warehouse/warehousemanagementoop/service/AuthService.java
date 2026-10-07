package com.team.warehouse.warehousemanagementoop.service;
import com.team.warehouse.warehousemanagementoop.dao.UserDAO;
import com.team.warehouse.warehousemanagementoop.entity.User;
import com.team.warehouse.warehousemanagementoop.util.SessionManager;
public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public boolean login(String username, String rawPassword) throws Exception {
        User user = userDAO.findByUsername(username);

        if (user == null) {
            throw new Exception("Tài khoản không tồn tại!");
        }
        if (!user.isActive()) {
            throw new Exception("Tài khoản đã bị khóa!");
        }

        if (rawPassword.equals(user.getPassword())) {
            SessionManager.setCurrentUser(user);
            return true;
        } else {
            throw new Exception("Sai mật khẩu!");
        }
    }
}
