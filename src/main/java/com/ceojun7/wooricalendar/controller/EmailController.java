package com.ceojun7.wooricalendar.controller;

import com.ceojun7.wooricalendar.dto.CalendarDTO;
import com.ceojun7.wooricalendar.dto.EmailPostDTO;
import com.ceojun7.wooricalendar.dto.EmailResponseDTO;
import com.ceojun7.wooricalendar.dto.InviteDTO;
import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.EmailMessageEntity;
import com.ceojun7.wooricalendar.service.CalendarService;
import com.ceojun7.wooricalendar.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.spi.CalendarNameProvider;

import javax.mail.MessagingException;

/**
 * @author : DGeon
 * @packageName : com.ceojun7.wooricalendar.controller
 * @fileName : EmailController
 * @date : 2023-06-12
 * @description :
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023-06-12 DGeon 최초 생성
 *              2023-06-19 박현민 캘린더 공유 이메일 발송
 **/
@RequestMapping("/sendmail")
@RestController
@RequiredArgsConstructor
@Slf4j
public class EmailController {
    private final EmailService emailService;

    /**
     * methodName : sendPasswordMail
     * comment : 임시비밀번호 발급 및 전송 메서드
     * author : DGeon
     * date : 2023-06-12
     * description :
     *
     * @param emailPostDto the email post dto
     * @return response entity
     * @throws MessagingException the messaging exception
     */
    // 임시 비밀번호 발급
    @PostMapping("/password")
    public ResponseEntity<?> sendPasswordMail(@RequestBody EmailPostDTO emailPostDto) throws MessagingException {

        String code = null;
        if(emailPostDto.getLanguage().toLowerCase().equals("ko-kr")) {
            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                    .to(emailPostDto.getEmail())
                    .subject("[Woori] 비밀번호 변경을 위한 인증 코드 발송")
                    .build();
            code = emailService.sendMail(emailMessage, "ko-password");
        }else if(emailPostDto.getLanguage().toLowerCase().equals("ja")) {
            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                    .to(emailPostDto.getEmail())
                    .subject("[Woori] パスワード変更のための認証コードの送信")
                    .build();
            code = emailService.sendMail(emailMessage, "ja-password");
        }else{
            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                    .to(emailPostDto.getEmail())
                    .subject("[Woori] Send verification code for password change")
                    .build();
            code = emailService.sendMail(emailMessage, "en-password");
        }

        EmailResponseDTO emailResponseDto = new EmailResponseDTO();
        emailResponseDto.setCode(code);
        return ResponseEntity.ok(emailResponseDto);
    }

    /**
     * methodName : sendJoinMail
     * comment : 회원가입시 인증코드 보내는 메서드
     * author : DGeon
     * date : 2023-06-12
     * description :
     *
     * @param emailPostDto the email post dto
     * @return response entity
     * @throws MessagingException the messaging exception
     */
    @PostMapping("/email")
    public ResponseEntity<?> sendJoinMail(@RequestBody EmailPostDTO emailPostDto) throws MessagingException {

        String code = null;
        if(emailPostDto.getLanguage().toLowerCase().equals("ko-kr")) {
            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                    .to(emailPostDto.getEmail())
                    .subject("[Woori] 이메일 인증을 위한 인증 코드 발송")
                    .build();
            code = emailService.sendMail(emailMessage, "ko-email");
        }else if(emailPostDto.getLanguage().toLowerCase().equals("ja")) {
            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                    .to(emailPostDto.getEmail())
                    .subject("[Woori] 電子メール認証用の認証コードを送")
                    .build();
            code = emailService.sendMail(emailMessage, "ja-email");
        }else{
            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                    .to(emailPostDto.getEmail())
                    .subject("[Woori] Send verification code for email verification")
                    .build();
            code = emailService.sendMail(emailMessage, "en-email");
        }



        EmailResponseDTO emailResponseDto = new EmailResponseDTO();
        emailResponseDto.setCode(code);
        return ResponseEntity.ok(emailResponseDto);
    }

    /**
     * methodName : sendInviteMail
     * comment : 캘린더 공유 메일 발송
     * author : 박현민
     * date : 2023-06-19
     * description :
     *
     * @param emailPostDto the email post dto
     * @param inviteDTO
     * @return response entity
     * @throws MessagingException the messaging exception
     */
    @PostMapping("/invite")
    public ResponseEntity<?> sendInviteMail(@RequestBody InviteDTO inviteDTO, @AuthenticationPrincipal String email)
            throws MessagingException {


        EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                .to(inviteDTO.getEmail())
                .subject(email + "님이 " + "[ " + inviteDTO.getName() + " ] 캘린더를 공유했습니다.")
                .build();
        Map<String, Object> data = null;
        if(inviteDTO.getEmail().toLowerCase().equals("ko")) {
             data = emailService.sendInviteMail(emailMessage, "ko-invite", inviteDTO, email);
        }else if(inviteDTO.getEmail().toLowerCase().equals("ja")) {
            data = emailService.sendInviteMail(emailMessage, "ja-invite", inviteDTO, email);
        }else{
            data = emailService.sendInviteMail(emailMessage, "en-invite", inviteDTO, email);
        }



        // EmailResponseDTO emailResponseDTO = new EmailResponseDTO();
        // emailResponseDTO.setCode(code);
//        log.warn("code:::::", data);
        // log.warn(code);
//        log.warn("==================테스트==================");
//        log.warn(inviteDTO.getEmail());
//        log.warn(inviteDTO.getName());
//        log.warn("calNo:" + inviteDTO.getCalNo());
//        log.warn("grade:" + inviteDTO.getGrade());

        return ResponseEntity.ok(data);
    }

    /**
     * methodName : sendJoinMail
     * comment : 이메일 찾기를 위한 PostMapping(보조 이메일을 입력하면 본 이메일을 HTML에 보낸다)
     * author : DGeon
     * date : 2023-06-24
     * description :
     *
     * @param emailPostDto the email post dto
     * @return response entity
     * @throws MessagingException the messaging exception
     */
    @PostMapping("/subemail")
    public ResponseEntity<?> sendForgotEmail(@RequestBody EmailPostDTO emailPostDto) throws MessagingException {

        String code = null;
        if(emailPostDto.getLanguage().toLowerCase().equals("ko-kr")) {
            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                    .to(emailPostDto.getEmail())
                    .subject("[Woori] 이메일 찾기 입니다")
                    .build();
            code = emailService.sendMail(emailMessage, "ko-subemail");
        }else if(emailPostDto.getLanguage().toLowerCase().equals("ja")) {
            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                    .to(emailPostDto.getEmail())
                    .subject("[Woori] 電子メールアカウント 検索です")
                    .build();
            code = emailService.sendMail(emailMessage, "ja-subemail");
        }else{
            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                    .to(emailPostDto.getEmail())
                    .subject("[Woori] email account is to find")
                    .build();
            code = emailService.sendMail(emailMessage, "en-subemail");
        }

        EmailResponseDTO emailResponseDto = new EmailResponseDTO();
        emailResponseDto.setCode(code);
//        log.warn(code);
        return ResponseEntity.ok(emailResponseDto);
    }
}