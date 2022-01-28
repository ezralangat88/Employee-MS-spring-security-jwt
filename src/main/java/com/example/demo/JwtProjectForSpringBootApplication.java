package com.example.demo;

import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;
import com.example.demo.Service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;

@SpringBootApplication
public class JwtProjectForSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(JwtProjectForSpringBootApplication.class, args);
	}
	@Bean
	CommandLineRunner run(UserService userService) {
		return args -> {
			userService.saveRole(new Role(1, "ROLE_USER"));
			userService.saveRole(new Role(2, "ROLE_MANAGER"));
			userService.saveRole(new Role(3, "ROLE_ADMIN"));
			userService.saveRole(new Role(4, "ROLE_SUPER_ADMIN"));

			userService.saveUser(new User(1, "John Travolta", "john", "1234", new ArrayList<>()));
			userService.saveUser(new User(2, "Will Smith", "will", "1234", new ArrayList<>()));
			userService.saveUser(new User(3, "Jim Carry", "jim", "1234", new ArrayList<>()));
			userService.saveUser(new User(4, "Arnold Schwarzenegger", "arnold", "1234", new ArrayList<>()));

			userService.addRoleToUser("john", "ROLE_USER");
			userService.addRoleToUser("will", "ROLE_MANAGER");
			userService.addRoleToUser("jim", "ROLE_ADMIN");
			userService.addRoleToUser("arnold", "ROLE_SUPER_ADMIN");
			userService.addRoleToUser("arnold", "ROLE_ADMIN");
			userService.addRoleToUser("arnold", "ROLE_USER");
		};
	}


}
