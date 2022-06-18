package com.springsecurity.config.security.service;

import com.springsecurity.common.exception.user.UserNotFoundException;
import com.springsecurity.config.security.dto.UserPrincipal;
import com.springsecurity.user.domain.AuthProvider;
import com.springsecurity.user.domain.User;
import com.springsecurity.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndAuthProvider(username, AuthProvider.local)
                .orElseThrow(() -> new UserNotFoundException());
        return UserPrincipal.from(user);
    }
}
