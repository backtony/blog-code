package com.mapstruct.domain.repository;

import com.mapstruct.domain.User;

public interface UserStore {

    Long store(User user);

}
