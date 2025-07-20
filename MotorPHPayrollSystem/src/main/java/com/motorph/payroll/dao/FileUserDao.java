package com.motorph.payroll.dao;

import com.motorph.payroll.exception.DataAccessException;
import com.motorph.payroll.model.User;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUserDao implements UserDao {
    private String filePath;
    private List<User> users;
    
    public FileUserDao(String filePath) {
        this.filePath = filePath;
        this.users = new ArrayList<>();
        loadUsers();
    }
    
    private void loadUsers() {
        File file = new File(filePath);
        
        try (Scanner scanner = new Scanner(file)) {
            // Skip header line
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }
                
                try {
                    String[] data = parseCSVLine(line);
                    
                    if (data.length >= 3) {
                        String username = data[0].trim();
                        String password = data[1].trim();
                        String userType = data[2].trim();
                        
                        User user = new User(username, password, userType);
                        users.add(user);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing user data: " + e.getMessage());
                }
            }
            
        } catch (FileNotFoundException e) {
            throw new DataAccessException("User data file not found: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataAccessException("Unexpected error reading user file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
    
    @Override
    public User getUserByUsername(String username) {
        return users.stream()
            .filter(user -> user.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public User authenticateUser(String username, String password) {
        return users.stream()
            .filter(user -> user.getUsername().equals(username) && user.getPassword().equals(password))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public void addUser(User user) {
        if (getUserByUsername(user.getUsername()) != null) {
            throw new DataAccessException("User with username " + user.getUsername() + " already exists");
        }
        users.add(user);
    }
    
    @Override
    public void updateUser(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(user.getUsername())) {
                users.set(i, user);
                return;
            }
        }
        throw new DataAccessException("User with username " + user.getUsername() + " not found for update");
    }
    
    @Override
    public void deleteUser(String username) {
        boolean removed = users.removeIf(user -> user.getUsername().equals(username));
        if (!removed) {
            throw new DataAccessException("User with username " + username + " not found for deletion");
        }
    }
    
    @Override
    public boolean saveUsers() {
        try (PrintWriter writer = new PrintWriter(new File(filePath))) {
            // Write header
            writer.println("Username,Password,UserType");
            
            // Write data
            for (User user : users) {
                writer.printf("%s,%s,%s\n",
                    user.getUsername(), user.getPassword(), user.getUserType());
            }
            
            System.out.println("User data saved successfully to: " + filePath);
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("Error writing user data: " + e.getMessage());
            return false;
        }
    }
    
    private String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }
}