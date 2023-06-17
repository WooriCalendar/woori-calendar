package com.ceojun7.wooricalendar.controller;

import com.ceojun7.wooricalendar.dto.CalendarDTO;
import com.ceojun7.wooricalendar.dto.MemberDTO;
import com.ceojun7.wooricalendar.dto.ResponseDTO;
import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.MemberEntity;
import com.ceojun7.wooricalendar.model.ShareEntity;
import com.ceojun7.wooricalendar.security.TokenProvider;
import com.ceojun7.wooricalendar.service.CalendarService;
import com.ceojun7.wooricalendar.service.MemberService;
import com.ceojun7.wooricalendar.service.ShareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;

/**
 * @author : DGeon
 * @packageName : com.ceojun7.wooricalendar.controller
 * @fileName : MemberController
 * @date : 2023-05-31
 * @description :
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023-05-31 DGeon 최초 생성
 *              2023-06-04 강태수 getMemberByEmail,updateMember 생성
 **/
@RestController
@RequestMapping("member")
@Slf4j
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private ShareService shareService;

    /**
     * methodName : registerMember
     * comment : 회원가입
     * 2023-06-08 : 회원가입을 하면서 기본적인 Nickname의 캘린더 생성, language 추가
     * author : DGeon
     * date : 2023-06-01
     * description :
     *
     * @param memberDTO   the member dto
     * @param calendarDTO the calendar dto
     * @return response entity
     */
    @PostMapping("signup")
    public ResponseEntity<?> registerMember(@RequestBody MemberDTO memberDTO, CalendarDTO calendarDTO) {
        try {
            if (memberDTO == null || memberDTO.getPassword() == null) {
                throw new RuntimeException("Invalid Password value.");
            }
            // 요청을 이용해 저장할 유저 만들기
            MemberEntity member = MemberEntity.builder()
                    .email(memberDTO.getEmail())
                    .password(passwordEncoder.encode(memberDTO.getPassword()))
                    .nickname(memberDTO.getNickname())
                    .subemail(memberDTO.getSubemail())
                    .birthday(memberDTO.getBirthday())
                    .regDate(new Date())
                    .updateDate(new Date())
                    .language(memberDTO.getLanguage().substring(0, 2))
                    .build();
            MemberEntity registeredMember = memberService.create(member);
            CalendarEntity calendar = CalendarEntity.builder()
                    .name(memberDTO.getEmail().substring(0, memberDTO.getEmail().indexOf("@")))
                    .regdate(new Date())
                    .updatedate(new Date())
                    // .timezone()
                    .build();
            calendarService.create(calendar);

            ShareEntity shareEntity = ShareEntity.builder().calendarEntity(calendar)
                    .memberEntity(MemberEntity.builder().email(memberDTO.getEmail()).build()).checked(true).build();
            shareService.create(shareEntity);

            MemberDTO responseMemberDTO = memberDTO.builder()
                    .email(registeredMember.getEmail())
                    .build();
            return ResponseEntity.ok().body(responseMemberDTO);
        } catch (Exception e) {
            ResponseDTO responseDTO = ResponseDTO.builder().error(e.getLocalizedMessage()).build();
            return ResponseEntity
                    .badRequest()
                    .body(responseDTO);
        }
    }

    /**
     * methodName : authenticate
     * comment : 로그인 및 토큰발급
     * author : DGeon
     * date : 2023-06-05
     * description :
     *
     * @param memberDTO the member dto
     * @return response entity
     */
    @PostMapping("signin")
    public ResponseEntity<?> authenticate(@RequestBody MemberDTO memberDTO) {
        log.info("{}", memberDTO);
        MemberEntity member = memberService.getByCredentials(memberDTO.getEmail(), memberDTO.getPassword(),
                passwordEncoder);
        log.info("{}", member);
        if (member != null) {
            // 토큰생성
            final String token = tokenProvider.create(member);
            log.info("발급 토큰 : {}", token);
            final MemberDTO responseUserDTO = memberDTO.builder()
                    .email(member.getEmail())
                    .token(token)
                    .build();
            return ResponseEntity.ok().body(responseUserDTO);
        } else {
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .error("Login failed")
                    .build();
            return ResponseEntity
                    .badRequest()
                    .body(responseDTO);
        }
    }

    /**
     * methodName : getMemberByEmail
     * comment : 이메일로 회원 이 가지고있는 내용 조회
     * author : 강태수
     * date : 2023-06-04
     * description :
     *
     * @param email
     * @return ResponseEntity
     * 
     */

    @GetMapping("/{email}")
    public ResponseEntity<MemberDTO> getMemberByEmail(@PathVariable String email) {
        MemberDTO memberDTO = memberService.getMemberByEmail(email);
        if (memberDTO != null) {
            return new ResponseEntity<>(memberDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * methodName : updateMember
     * comment : 패스워드,닉네임, 서브이메일, 생년월일, 언어 수정
     * author : 강태수
     * date : 2023-06-04
     * description :
     *
     * @param memberDTO
     * @return ResponseEntity
     * 
     */

    @PutMapping("/{email}")
    public ResponseEntity<String> updateMember(@PathVariable String email,
            @RequestHeader("Authorization") String token,
            @RequestBody MemberDTO memberDTO) {

        boolean updated = memberService.updateMember(memberDTO);
        if (updated) {
            return new ResponseEntity<>("회원 정보가 성공적으로 업데이트되었습니다.", HttpStatus.OK);
        }
        return new ResponseEntity<>("회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }

    /**
     * methodName : getEmailList
     * comment : email중복검사를 위한 회원email목록 불러오는 메서드
     * author : DGeon
     * date : 2023-06-13
     * description :
     *
     * @return response entity
     */
    @GetMapping("signup")
    public ResponseEntity<?> getEmailList(){
        log.warn("email 중복검사 :: get호출됨");
        List<String> entities = memberService.findeamil();
        ResponseDTO<String> resp = ResponseDTO.<String>builder().data(entities).build();
        log.warn("넘겨주는 값 확인 :::"+String.valueOf(ResponseEntity.ok().body(resp)));
        return ResponseEntity.ok().body(resp);

    }

    @PutMapping("updatePassword")
    public ResponseEntity<String> updatePassword(@RequestBody MemberDTO memberDTO) {

        boolean updated = memberService.updatePassword(memberDTO.getEmail(), passwordEncoder.encode(memberDTO.getPassword()));
        if (updated) {
            return new ResponseEntity<>("회원 정보가 성공적으로 업데이트되었습니다.", HttpStatus.OK);
        }
        return new ResponseEntity<>("회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }

}
