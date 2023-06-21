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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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

            if(memberDTO.getLanguage().substring(0,2).equals("ko")) {
                CalendarEntity holidayCalendar = CalendarEntity.builder()
                        .name("대한민국 공휴일")
                        .regdate(new Date())
                        .updatedate(new Date())
                        // .timezone()
                        .build();
                calendarService.create(holidayCalendar);

                ShareEntity holidayShareEntity = ShareEntity.builder().calendarEntity(holidayCalendar)
                        .memberEntity(MemberEntity.builder().email(memberDTO.getEmail()).build()).checked(true).build();
                shareService.create(holidayShareEntity);
            }



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
     * methodName : checkPassword
     * comment : 패스워드 일치확인
     * author : 강태수
     * date : 2023-06-20
     * description :
     *
     * @param MemberEntity
     * @return the ResponseEntity
     * 
     */
    @PostMapping("check")
    public ResponseEntity<?> checkPassword(@RequestBody MemberDTO memberDTO) {
        MemberEntity member = memberService.getByCredentials(memberDTO.getEmail(), memberDTO.getPassword(),
                passwordEncoder);
        log.info("{}", member);
        return ResponseEntity.ok().body(member);
        // if (member != null) {
        // final MemberDTO responseUserDTO = memberDTO.builder()
        // .email(member.getEmail())
        // .password(member.getPassword())
        // .build();
        // return ResponseEntity.ok(responseUserDTO); // "비밀번호가 일치합니다. 123"
        // } else {
        // ResponseDTO responseDTO = ResponseDTO.builder()
        // .error("Login failed")
        // .build();
        // return ResponseEntity.badRequest().body(responseDTO); // "비밀번호가 일치하지 않습니다."
        // }
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

    @GetMapping
    public ResponseEntity<MemberDTO> getMemberByEmail(@AuthenticationPrincipal String email) {
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

    @PutMapping
    public ResponseEntity<String> updateMember(@AuthenticationPrincipal String email,
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
    @PostMapping("findemail")
    public ResponseEntity<?> getEmail(@RequestBody MemberDTO memberDTO) {

        // log.warn(memberDTO.getEmail());
        // String findemail = memberService.findByEmail(memberDTO.getEmail());
        // log.warn(findemail);
        try {
            log.warn("email 중복검사 :: get호출됨 :: " + memberDTO.getEmail());
            MemberEntity member = memberService.findByEmail(memberDTO.getEmail());
            MemberDTO responseMemberDTO = memberDTO.builder()
                    .email(member.getEmail())
                    .build();
            return ResponseEntity.ok().body(responseMemberDTO);
        } catch (NullPointerException nullPointerException) {
            MemberDTO reMemberDTO = memberDTO.builder().build();
            return ResponseEntity.ok().body(reMemberDTO);
        }
    }

    @PutMapping("updatePassword")
    public ResponseEntity<String> updatePassword(@RequestBody MemberDTO memberDTO) {

        boolean updated = memberService.updatePassword(memberDTO.getEmail(),
                passwordEncoder.encode(memberDTO.getPassword()));
        if (updated) {
            return new ResponseEntity<>("회원 정보가 성공적으로 업데이트되었습니다.", HttpStatus.OK);
        }
        return new ResponseEntity<>("회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }
}
