package com.mapstruct.infrastructure;

import com.mapstruct.domain.User;
import com.mapstruct.domain.repository.UserStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserStoreImpl implements UserStore {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Long store(User user) {
        return userJpaRepository.save(user).getId();
    }
}
