package com.ceojun7.wooricalendar.security;

import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.MemberEntity;
import com.ceojun7.wooricalendar.model.ShareEntity;
import com.ceojun7.wooricalendar.persistence.MemberRepository;
import com.ceojun7.wooricalendar.service.CalendarService;
import com.ceojun7.wooricalendar.service.MemberService;
import com.ceojun7.wooricalendar.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Objects;

/**
 * @author : DGeon
 * @packageName : com.ceojun7.wooricalendar.security
 * @fileName : OAuthMemberServiceImpl
 * @date : 2023-06-01
 * @description :
 * ===========================================================
 * DATE           AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-06-01        DGeon             최초 생성
 **/
@Service
@Slf4j
public class OAuthMemberServiceImpl extends DefaultOAuth2UserService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private ShareService shareService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException, NullPointerException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        try {
            log.info("OAuth2 member Info {} ", new ObjectMapper().writeValueAsString(oAuth2User));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        final String authProvider = userRequest.getClientRegistration().getClientName();
        log.info("{}", authProvider);

        final String language = oAuth2User.getAttribute("locale");
        log.warn("language::::::{}", language);
        String email = null;
        if (authProvider.equalsIgnoreCase("google")) {
            email = (String) oAuth2User.getAttribute("email");
        }

        // github 로그인 시 추가
//        if (authProvider.equalsIgnoreCase("github")) {
//            username = (String) oAuth2User.getAttribute("login");
//        } else

        // DB작업
        MemberEntity memberEntity = null;
        // db탐색 후 중복되는 id가 없을시 신규 id로 등록
        if (!memberRepository.existsByEmail(email)) {
            memberEntity = MemberEntity.builder().email(email).auth_Provider(authProvider).language(language).build();
            memberEntity = memberRepository.save(memberEntity);
            CalendarEntity calendar = CalendarEntity.builder()
                    .name(Objects.requireNonNull(email).substring(0, email.indexOf("@")))
                    .regdate(new Date())
                    .updatedate(new Date())
                    // .timezone()
                    .build();
            calendarService.create(calendar);

            ShareEntity shareEntity = ShareEntity.builder().calendarEntity(calendar)
                    .memberEntity(MemberEntity.builder().email(email).build()).checked(true).build();
            shareService.create(shareEntity);
            if(language.equals("ko")) {

                ShareEntity koreaCalendar = null;
                koreaCalendar = ShareEntity.builder()
                        .calendarEntity(CalendarEntity.builder().calNo(90L).build())
                        .memberEntity(MemberEntity.builder().email(email).build()).checked(true)
                        .build();
                shareService.create(koreaCalendar);
                koreaCalendar = ShareEntity.builder()
                        .calendarEntity(CalendarEntity.builder().calNo(98L).build())
                        .memberEntity(MemberEntity.builder().email(email).build()).checked(true)
                        .build();

                shareService.create(koreaCalendar);
            }
        } else {
            memberEntity = memberRepository.findByEmail(email);
        }

        log.info("success pulled member info member {}, authProvider {}", email, authProvider);
        return new ApplicationOAuth2User(memberEntity.getEmail(), oAuth2User.getAttributes());
    }

}
