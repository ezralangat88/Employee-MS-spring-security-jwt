package com.example.demo.Service;

import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;

import java.util.List;

public interface UserService {

    User saveUser(User user);

    Role saveRole(Role role);

    void addRoleToUser(String userEmail, String roleName);

    User getUser(String userEmail);

    List<User> getAllUsers();
}
