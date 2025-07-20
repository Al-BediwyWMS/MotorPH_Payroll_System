package com.motorph.payroll.dao;

import com.motorph.payroll.model.User;
import java.util.List;

public interface UserDao {
    List<User> getAllUsers();
    User getUserByUsername(String username);
    User authenticateUser(String username, String password);
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(String username);
    boolean saveUsers();
}