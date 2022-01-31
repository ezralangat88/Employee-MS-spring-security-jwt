package com.example.demo.Filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    //AuthenticationManager will be called to authenticate user by attemptAuthentication and parse credentials
    private AuthenticationManager authenticationManager;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;


    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String userEmail = request.getParameter("UserEmail") ;
        String password = request.getParameter("password") ;
        log.info("UserEmail is: {}", userEmail);
        log.info("Password is: {}", password );
        //Creating object of UsernamePasswordAuthenticationToken
        //Wrapping info that is already coming with the request and then passing it to UsernamePasswordAuthenticationToken
        //and calling authenticationManager to authenticate the user that is logging in the request
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userEmail, password );
        return  authenticationManager.authenticate(authenticationToken);
    }

    // successfulAuthentication method is called once login is successful
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        //Getting the authenticated user info for generating jwt using User from core.user-details
        User user = (User) authentication.getPrincipal();
        //Algorithm for signing jwt and refresh tokens that are going to be assigned to logged-in user
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        //Access Token
        String access_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .withIssuer(request.getRequestURI().toString())
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority ::getAuthority).collect(Collectors.toList())) //with String name and list
                .sign(algorithm);

        //Refresh Token
        String refresh_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .withIssuer(request.getRequestURI().toString())
                .sign(algorithm);
        //Sending results response to the user in the frontend
        response.setHeader("access_token", access_token);
        response.setHeader("refresh_token", refresh_token);
    }


}