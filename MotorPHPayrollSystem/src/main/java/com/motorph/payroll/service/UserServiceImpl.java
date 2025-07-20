package com.motorph.payroll.service;

import com.motorph.payroll.dao.UserDao;
import com.motorph.payroll.model.User;
import java.util.List;

public class UserServiceImpl implements UserService {
    private UserDao userDao;
    
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }
    
    @Override
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }
    
    @Override
    public User getUserByUsername(String username) {
        return userDao.getUserByUsername(username);
    }
    
    @Override
    public User authenticateUser(String username, String password) {
        return userDao.authenticateUser(username, password);
    }
    
    @Override
    public void addUser(User user) {
        userDao.addUser(user);
    }
    
    @Override
    public void updateUser(User user) {
        userDao.updateUser(user);
    }
    
    @Override
    public void deleteUser(String username) {
        userDao.deleteUser(username);
    }
    
    @Override
    public boolean saveUsers() {
        return userDao.saveUsers();
    }

    @Override
    public User authenticate(String username, String password) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}