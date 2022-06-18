package com.security.demospringsecurityform.account;


import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지, 런타임에 애노테이션 참고하기 때문
@WithMockUser(username = "backtony",roles = "USER")
public @interface WithUser {
}
