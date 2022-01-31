package com.example.demo.Service;

import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor //Coz we have all fields defined, and we need to inject them in this class. Lombok is going to create a constructor and pass all these args to it.
@Transactional // To avoid calling userRepo to save roles again once addRoleToUser is executed
@Slf4j //Logging
public class UserServiceImplementation implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUserEmail(userEmail);

        if (user ==null){
            log.error("User not found in the database");
            throw new UsernameNotFoundException("User not found");
        }else{
            log.info("User found in the database{}", userEmail);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        });
        return new org.springframework.security.core.userdetails.User(user.getUserEmail(), user.getPassword(), authorities);
    }

    @Override
    public User saveUser(User user) {
        log.info("Saving new User: {} to the database", user.getName());
        //Encoding password from user before saving the user to db
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Role saveRole(Role role) {
        log.info("Saving new Role: {} to the database", role.getRoleName());
        return roleRepository.save(role);
    }

    @Override
    public void addRoleToUser(String userEmail, String roleName) {
        log.info("Adding Role: {} to User: {}", roleName, userEmail);
        Role role = roleRepository.findByRoleName(roleName);
        User user = userRepository.findByUserEmail(userEmail);
        user.getRoles().add(role);
    }

    @Override
    public User getUser(String userEmail) {
        log.info("Fetching User: {}",userEmail);
        return userRepository.findByUserEmail(userEmail);
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Fetching all Users");
        return userRepository.findAll();
    }

}
