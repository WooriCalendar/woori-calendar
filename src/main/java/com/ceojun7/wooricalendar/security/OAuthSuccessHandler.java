package com.ceojun7.wooricalendar.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
/**
 * @author : DGeon
 * @packageName : com.ceojun7.wooricalendar.security
 * @fileName : OAuthSuccessHandler
 * @date : 2023-06-01
 * @description :
 * ===========================================================
 * DATE           AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-06-01        DGeon             최초 생성
 **/
@Service
@Slf4j
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final String LOCAL_REDIRECT_URL = "http://localhost:3000";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String token = new TokenProvider().create(authentication);
//        log.info(token);

        Optional<Cookie> oCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(RedirectUrlCookieFilter.REDIRECT_URI_PARAM)).findFirst();
        Optional<String> redirectUri = oCookie.map(Cookie::getValue);

        response.sendRedirect(redirectUri.orElseGet(() -> LOCAL_REDIRECT_URL) + "/socialLogin?token=" + token);
    }
}