package com.motorph.payroll.service;

import com.motorph.payroll.model.User;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserByUsername(String username);
    User authenticateUser(String username, String password);
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(String username);
    boolean saveUsers();

    public User authenticate(String username, String password);
}