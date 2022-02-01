package com.example.demo.Filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
//OncePerRequestFilter will intercept all the requests coming to app
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //Checking if this is not login path and letting it go to the next filter in the filter-chain
        if(request.getServletPath().equals("/api/login") || request.getServletPath().equals("/api/token/refresh")){
            filterChain.doFilter(request, response);

        }else{
            //Check if it has authorization  and adding logged-in user as the logged-in/current user in the security context
            //Accessing Authorization header that should be key for the token
            String AuthorizationHeader = request.getHeader(AUTHORIZATION);  //Import static constant
            if(AuthorizationHeader != null && AuthorizationHeader.startsWith("Bearer ")){
                try {
                    String token = AuthorizationHeader.substring("Bearer ".length()); //Removing "Bearer "
                    //Defining Algorithm
                    Algorithm algorithm = Algorithm.HMAC256("secret".getBytes()); //Need to class utility
                    //Verifying token using Algorithm
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    DecodedJWT decodedJWT = verifier.verify(token);
                    //If token is valid then we get user's username and roles
                    String username = decodedJWT.getSubject();
                    String [] roles = decodedJWT.getClaim("roles").asArray(String.class);
                    //Conversion by getting roles and converting them into something that extend GrantAuthority coz that's what
                    //Spring security is expecting as the roles of  the user
                    Collection<SimpleGrantedAuthority> authorities = new HashSet<>();
                    stream(roles).forEach(role -> {
                        authorities.add(new SimpleGrantedAuthority(role));
                    });
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request, response);

                }catch (Exception exception){
                    log.error("Error logging in: ", exception.getMessage());
                    response.setHeader("error", exception.getMessage());
                    response.setStatus(FORBIDDEN.value());

                    Map<String, String> error = new HashMap<>();
                    error.put("access_token", exception.getMessage());
                    response.setContentType(APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), error);

                }
            }else {
                filterChain.doFilter(request, response);
            }
        }
    }
}
