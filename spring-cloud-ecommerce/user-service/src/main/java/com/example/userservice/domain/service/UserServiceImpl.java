package com.example.userservice.domain.service;

import com.example.userservice.domain.User;
import com.example.userservice.domain.UserRepository;
import com.example.userservice.domain.service.dto.request.UserSaveRequestDto;
import com.example.userservice.domain.service.dto.response.OrderResponseDto;
import com.example.userservice.domain.service.dto.response.UserInfoResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderServiceClient orderServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;


    @Override
    public UserInfoResponseDto createUser(UserSaveRequestDto userSaveRequestDto) {
        userSaveRequestDto.setPwd(passwordEncoder.encode(userSaveRequestDto.getPwd()));
        User user = userRepository.save(userSaveRequestDto.toEntity());
        return UserInfoResponseDto.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        UserInfoResponseDto userInfoResponseDto = UserInfoResponseDto.from(user);
        // feignClient를 사용한 통신에서 다른 마이크로서비스가 죽어버린 상태일 때 circuitbreaker를 사용하여 응답을 컨트롤한다.
        // run의 인자로 수행할 동작, 예외시 발생할 동작을 명시한다.
        log.info("before call order microservice");
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        List<OrderResponseDto> orders = circuitbreaker.run(() -> orderServiceClient.getOrders(userId),
                                                            throwable -> new ArrayList<>());
        log.info("after call order microservice");
        userInfoResponseDto.setOrders(orders);
        return userInfoResponseDto;
    }
}
