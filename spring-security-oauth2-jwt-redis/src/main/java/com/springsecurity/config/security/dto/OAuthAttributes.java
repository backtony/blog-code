package com.springsecurity.config.security.dto;

import com.springsecurity.common.exception.security.NotSupportRegistrationIdException;
import com.springsecurity.user.domain.AuthProvider;
import com.springsecurity.user.domain.Role;
import com.springsecurity.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class OAuthAttributes {

    private Map<String,Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private AuthProvider authProvider;


    public static OAuthAttributes of(String registrationId,
                                     String userNameAttributeName,
                                     Map<String,Object> attributes){

        switch (registrationId){
            case "google":
                return ofGoogle(userNameAttributeName,attributes);
            case "naver":
                return ofNaver("id",attributes);
            case "kakao":
                return ofKakao("id",attributes);
            default:
                throw new NotSupportRegistrationIdException();
        }
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String,Object> attributes){
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .authProvider(AuthProvider.google)
                .build();
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String,Object> attributes) {
        Map<String,Object> response = (Map<String,Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) attributes.get("profile_image"))
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .authProvider(AuthProvider.naver)
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String,Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");


        return OAuthAttributes.builder()
                .name((String) profile.get("nickname"))
                .email((String) account.get("email"))
                .picture((String) profile.get("profile_image_url"))
                .attributes(account)
                .nameAttributeKey(userNameAttributeName)
                .authProvider(AuthProvider.kakao)
                .build();
    }



    public User toUserEntity(){
        return User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.USER)
                .authProvider(authProvider)
                .build();
    }
}
