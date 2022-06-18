package com.springsecurity.unit.user.domain;

import com.springsecurity.user.domain.User;
import org.junit.jupiter.api.Test;

import static com.springsecurity.support.UserGivenHelper.givenGoogleUser;
import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    @Test
    void 소셜_로그인_시도_프로필이_업데이트된_경우() throws Exception{
        //given
        User user = givenGoogleUser();
        String name = "testName";
        String picture = "testPicture";

        //when
        user.updateBySocialProfile(name,picture);

        //then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getPicture()).isEqualTo(picture);
    }
}
