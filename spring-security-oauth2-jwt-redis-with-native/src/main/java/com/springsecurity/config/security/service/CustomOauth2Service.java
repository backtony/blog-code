package com.springsecurity.config.security.service;

import com.springsecurity.config.security.dto.OAuthAttributes;
import com.springsecurity.config.security.dto.UserPrincipal;
import com.springsecurity.user.domain.User;
import com.springsecurity.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class CustomOauth2Service extends DefaultOAuth2UserService  {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest){
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId,userNameAttributeName,oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);

        return UserPrincipal.of(user,attributes.getAttributes());

    }

    private User saveOrUpdate(OAuthAttributes attributes) {

        Optional<User> userOptional = userRepository.findByEmail(attributes.getEmail());
        User user;
        if (userOptional.isPresent()){
            user = userOptional.get();
            user.updateBySocialProfile(attributes.getName(),attributes.getPicture());
        }
        else{
            user = userRepository.save(attributes.toUserEntity());
        }

        return user;
    }
}
