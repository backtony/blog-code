package com.security.demospringsecurityform.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
// UserDetailsService의 역할 -> 쓰이는 건 DAO 가지고 인증정보를 읽어와서 인증하는데 쓰임
// 이 인터페이스가 하는 일 자체는 username을 받아와서 이 username에 해당하는 유저 정보는 DB에서 가져와서
// userDetails 타입으로 반환해주는게 하는 일이다.
// 스프링시큐리티 config에서 UserDetailsService 를 구현한 AccountService를 사용하도록 설정해야한다.
// 하지만 이게 빈으로 등록되어 있으면 스프링은 알아서 UserDetailsService를 구현한 AccountService 를 사용한다.
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username); // DB에서 꺼내온다
        if(account == null){
            throw new UsernameNotFoundException(username);
        }
        // 내가 가지고 있는 정보는 Account 타입인데 반환형은 UserDetails 타입이므로 변환시켜줘야한다.
        // 바꾸는데 Spring에서 편리하게 userdetails 클래스를 만들도록
        // User라는 클래스를 제공한다

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole())
                .build();

    }

    public Account createNew(Account account) {
        account.encodePassword(passwordEncoder);
        return this.accountRepository.save(account);
    }
}
