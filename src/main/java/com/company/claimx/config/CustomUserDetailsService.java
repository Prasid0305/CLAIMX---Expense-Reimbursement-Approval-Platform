package com.company.claimx.config;

import com.company.claimx.entity.User;
import com.company.claimx.exception.UserInactiveException;
import com.company.claimx.exception.UserNotFoundException;
import com.company.claimx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException("user not found with email "+email));

        if(!user.getIsActive()){
            throw new UserInactiveException("user account si inactive");

        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );



    }

}
