package com.ceojun7.wooricalendar.service;

import com.ceojun7.wooricalendar.dto.InviteDTO;
import com.ceojun7.wooricalendar.model.EmailMessageEntity;
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
public class EmailService2 {
  private final JavaMailSender javaMailSender;
  private final SpringTemplateEngine templateEngine;

  public String sendInviteMail(EmailMessageEntity emailMessage, String type) throws MessagingException {
    String authNum = createCode();
    log.warn("1" + emailMessage.getMessage());
    log.warn("2" + emailMessage.getSubject());
    log.warn("3" + emailMessage.getTo());
    InviteDTO inviteDTO = new InviteDTO();
    String[] inviteStrings = emailMessage.getMessage().split("^"); // 추후 고려 사항 (캘린더 제목에 제한사항을 두는 것을 고려하여, 구분자 선정)
    inviteDTO.setEmail(inviteStrings[0]);
    inviteDTO.setName(inviteStrings[1]);
    // inviteDTO.setCalNo(inviteStrings[2]);
    // inviteDTO.setGrade(inviteStrings[3]);

    // inviteDTO.setEmail(emailMessage.getMessage().split(""));
    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    try {
      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
          true, "UTF-8");
      mimeMessageHelper.setTo(emailMessage.getTo()); // 메일 수신자
      mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
      // mimeMessageHelper.setText(setContext(inviteDTO, type), true); // 메일 본문
      // 내용,HTML 여부
      // setContext(, type)
      mimeMessageHelper.setText(type, authNum);
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

  // public String setContext(String , String type)
}