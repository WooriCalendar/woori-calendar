package com.ceojun7.wooricalendar.service;

import com.ceojun7.wooricalendar.dto.InviteDTO;
import com.ceojun7.wooricalendar.model.EmailMessageEntity;
import com.ceojun7.wooricalendar.persistence.MemberRepository;
import com.ceojun7.wooricalendar.security.TokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author : DGeon
 * @packageName : com.ceojun7.wooricalendar.service
 * @fileName : EmailService
 * @date : 2023-06-12
 * @description :
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023-06-12 DGeon 최초 생성
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    public String sendMail(EmailMessageEntity emailMessage, String type) throws MessagingException {
        String authNum = createCode();

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setTo(emailMessage.getTo()); // 메일 수신자
            mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
            mimeMessageHelper.setText(setContext(authNum, type), true); // 메일 본문 내용, HTML 여부
            ClassPathResource imgRs = new ClassPathResource("images/emailLogo.jpg");
            mimeMessageHelper.addInline("logo", imgRs);
            javaMailSender.send(mimeMessage);

            log.info("Success");

            return authNum;

        } catch (MessagingException e) {
            log.info("fail");
            throw new RuntimeException(e);
        }
    }

    // 인증번호 및 임시 비밀번호 생성 메서드
    public String createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(4);

            switch (index) {
                case 0:
                    key.append((char) ((int) random.nextInt(26) + 97));
                    break;
                case 1:
                    key.append((char) ((int) random.nextInt(26) + 65));
                    break;
                default:
                    key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }

    // thymeleaf를 통한 html 적용
    public String setContext(String code, String type) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process(type, context);
    }

    public String setContextMap(Map<String, Object> data, String type) {
        Context context = new Context();
        context.setVariable("data", data);
        return templateEngine.process(type, context);
    }

    public Map<String, Object> sendInviteMail(EmailMessageEntity emailMessage, String type, InviteDTO inviteDTO,
            String email)
            throws MessagingException {

        final String token = tokenProvider.create(memberRepository.findByEmail(inviteDTO.getEmail()));

        Map<String, Object> data = new HashMap<>();
        data.put("name", inviteDTO.getName());
        data.put("receiver", inviteDTO.getEmail());
        data.put("sender", email);
        log.info("과연 토큰을 가져왔을까요?" + token);
        data.put("token", token);
        String gradeSet = "";
        if (inviteDTO.getGrade() == 0) {
            gradeSet = "보기";
        } else if (inviteDTO.getGrade() == 1) {
            gradeSet = "편집";
        } else if (inviteDTO.getGrade() == 2) {
            gradeSet = "관리";
        }
        data.put("grade", gradeSet);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setTo(emailMessage.getTo()); // 메일 수신자
            mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
            mimeMessageHelper.setText(setContextMap(data, type), true); // 메일 본문 내용, HTML 여부
            ClassPathResource imgRs = new ClassPathResource("images/emailLogo.jpg");
            mimeMessageHelper.addInline("logo", imgRs);
            javaMailSender.send(mimeMessage);

            log.info("Success");

            return data;

        } catch (MessagingException e) {
            log.info("fail");
            throw new RuntimeException(e);
        }
    }

    public String sendsubEmail(EmailMessageEntity emailMessage, String type, String email) throws MessagingException {
        String authNum = email;

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setTo(emailMessage.getTo()); // 메일 수신자
            mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
            mimeMessageHelper.setText(setContext(authNum, type), true); // 메일 본문 내용, HTML 여부
            ClassPathResource imgRs = new ClassPathResource("images/emailLogo.jpg");
            mimeMessageHelper.addInline("logo", imgRs);
            javaMailSender.send(mimeMessage);

            log.info("Success");

            return authNum;

        } catch (MessagingException e) {
            log.info("fail");
            throw new RuntimeException(e);
        }
    }
}