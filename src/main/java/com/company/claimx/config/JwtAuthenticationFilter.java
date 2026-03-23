package com.company.claimx.config;




import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    @Autowired
    private JwtUtil jwtUtil;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        String authHeader = request.getHeader("Authorization");  //std header name
        String token = null;
        String email = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.substring(7);

            try{
                email = jwtUtil.extractEmail(token);

            }
            catch (Exception e){
                System.out.println("Invalid token:" +e.getMessage());
            }
        }

        if (email !=null && SecurityContextHolder.getContext().getAuthentication() ==  null){
            //if token is valid and no authentication si set yet -- if already set then we must not overwrite it

            if (jwtUtil.validateToken(token, email)){
                String role = jwtUtil.extractRole(token);

                //authentication object creation
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
