package com.example.demo.Controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api") //Adding base url to this class
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok().body(userService.getAllUsers());

    }

    /** READ / Find By ID */
    @GetMapping("/user/{id}")
    public ResponseEntity<User> getEmployeeById(@PathVariable(value = "id") Integer id)
            throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user not found for this id :: " + id));
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/user/save")
    public ResponseEntity<User> saveUser(@RequestBody User user){

        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveUser(user));

    }

    @PostMapping("/role/save")
    public ResponseEntity<Role> saveRole(@RequestBody Role role){

        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveRole(role));

    }

    /** UPDATE */
//     @PathVariable is used in Binding url value to method parameter value
    @PutMapping("/user/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable(value = "id") Integer id,
                                           @RequestBody User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user not found for this id :: " + id));

        user.setName(userDetails.getName());
        user.setUsername(userDetails.getUsername());
        user.setPassword(userDetails.getPassword());

//        user.setEmailId(userDetails.getEmailId());
//        user.setLastName(userDetails.getLastName());
//        user.setFirstName(userDetails.getFirstName());
//        user.setPhoneNo(userDetails.getPhoneNo());
//        user.setGender(userDetails.getGender());

//        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/update").toUriString());
//        return ResponseEntity.created(uri).body(userService.saveUser(user));

        User updatedUser = userService.saveUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    /** DELETE */
    @DeleteMapping("/user/delete/{id}")
    public Map<String, Boolean> deleteUser1(@PathVariable(value = "id") Integer id)
            throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for this id :: " + id));

        userRepository.delete(user);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }

    @PostMapping("/role/addtouser")
    public ResponseEntity<?> addRoleToUser(@RequestBody RoleToUserForm form){

        userService.addRoleToUser(form.getUsername(), form.getRoleName());
        return ResponseEntity.ok().build();

    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //Check if it has authorization  and adding logged-in user as the logged-in/current user in the security context
        //Accessing Authorization header that should be key for the token
        String AuthorizationHeader = request.getHeader(AUTHORIZATION);  //Import static constant
        if(AuthorizationHeader != null && AuthorizationHeader.startsWith("Bearer ")){
            try {
                String refresh_token = AuthorizationHeader.substring("Bearer ".length()); //Removing "Bearer "
                //Defining Algorithm
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes()); //Need to class utility
                //Verifying refresh_token using Algorithm
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                //If refresh_token is valid then we get user's username and roles
                String username = decodedJWT.getSubject();

                User user = userService.getUser(username);

                //Using the user to create the token
                //Access Token
                String access_token = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("roles", user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList())) //with String name and list
                        .sign(algorithm);

                //Sending results response to the user in the frontend

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", access_token);
                tokens.put("refresh_token", refresh_token);
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);

            }catch (Exception exception){
                response.setHeader("error", exception.getMessage());
                response.setStatus(FORBIDDEN.value());

                Map<String, String> error = new HashMap<>();
                error.put("access_token", exception.getMessage());
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);

            }
        }else {
            throw new RuntimeException("Refresh Token is missing");
        }



    }

}

@Data
class RoleToUserForm{
    private String username;
    private String roleName;
}



