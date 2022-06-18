package com.springsecurity.support;

import com.springsecurity.config.security.dto.UserPrincipal;
import com.springsecurity.user.domain.AuthProvider;
import com.springsecurity.user.domain.Role;
import com.springsecurity.user.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;

public class UserGivenHelper {

    public static User givenUser() {
        return User.builder()
                .email("backtony@gmail.com")
                .authProvider(AuthProvider.google)
                .name("backtony")
                .picture("url")
                .role(Role.USER)
                .build();
    }

    public static Authentication createAuthentication(User user) {
        List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority(user.getRoleKey()));
        UserPrincipal principal = UserPrincipal.from(user);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}
