package com.example.userservice.config.security;

import com.example.userservice.domain.User;
import com.example.userservice.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저"));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),user.getPwd(),
                true,true,true,true
                ,new ArrayList<>()
        );
    }
}
