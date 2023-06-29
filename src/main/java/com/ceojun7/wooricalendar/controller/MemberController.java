package com.ceojun7.wooricalendar.controller;

import com.ceojun7.wooricalendar.dto.*;
import com.ceojun7.wooricalendar.model.*;
import com.ceojun7.wooricalendar.security.TokenProvider;
import com.ceojun7.wooricalendar.service.*;

import lombok.RequiredArgsConstructor;
import com.ceojun7.wooricalendar.service.CalendarService;
import com.ceojun7.wooricalendar.service.MemberService;
import com.ceojun7.wooricalendar.service.NotificationService;
import com.ceojun7.wooricalendar.service.ScheduleService;
import com.ceojun7.wooricalendar.service.ShareService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final CalendarService calendarService;
    private final ShareService shareService;
    private final ScheduleService scheduleService;
    private final EmailService emailService;
    private final NotificationService notificationService;

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
        log.warn("사인업호출됨");
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
            String zone;
            if(member.getLanguage().equals("ko")){
                 zone = "Asia/Seoul";
            }else if(member.getLanguage().equals("ja")){
                zone = "Asia/Tokyo";
            }else{
                zone = "America/New_York";
            }
            CalendarEntity calendar = CalendarEntity.builder()
                    .name(memberDTO.getEmail().substring(0, memberDTO.getEmail().indexOf("@")))
                    .regdate(new Date())
                    .updatedate(new Date())
                    .timezone(zone)
                    .color("#7bc6ff")
                    .build();
            calendarService.create(calendar);
            ShareEntity shareEntity = ShareEntity.builder().calendarEntity(calendar)
                    .memberEntity(MemberEntity.builder().email(memberDTO.getEmail()).build()).checked(true).build();
            shareService.create(shareEntity);

            if(memberDTO.getLanguage().substring(0,2).equals("ko")) {
//                CalendarEntity holidayCalendar = CalendarEntity.builder()
//                        .name("대한민국 공휴일")
//                        .regdate(new Date())
//                        .updatedate(new Date())
//                        // .timezone()
//                        .build();
//                calendarService.create(holidayCalendar);
                ShareEntity koreaCalendar = null;
                koreaCalendar = ShareEntity.builder().calendarEntity(CalendarEntity.builder().calNo(90L).build())
                        .memberEntity(MemberEntity.builder().email(memberDTO.getEmail()).build()).checked(true).build();
                shareService.create(koreaCalendar);
                koreaCalendar = ShareEntity.builder().calendarEntity(CalendarEntity.builder().calNo(98L).build())
                        .memberEntity(MemberEntity.builder().email(memberDTO.getEmail()).build()).checked(true).build();
                shareService.create(koreaCalendar);
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
     * @param memberDTO
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
    public ResponseEntity<?> getEmail(@RequestBody MemberDTO memberDTO) throws MessagingException {

        // log.warn(memberDTO.getEmail());
        // String findemail = memberService.findByEmail(memberDTO.getEmail());
        // log.warn(findemail);
        try {
            MemberEntity member = null;
            MemberDTO responseMemberDTO = null;
                if(memberDTO.getEmail() != null) {
                    log.warn("email 중복검사 :: get호출됨 :: " + memberDTO.getEmail());
                    member = memberService.findByEmail(memberDTO.getEmail());
                    responseMemberDTO = memberDTO.builder()
                            .email(member.getEmail())
                            .build();
                }else if(memberDTO.getSubemail() != null){
                    log.warn("subemail 호출::"+memberDTO.getSubemail());
                    if(memberService.findBySubEmail(memberDTO.getSubemail()) !=null) {
                        member = memberService.findBySubEmail(memberDTO.getSubemail());

                        if (member.getLanguage().equals("ko")) {
                            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                                    .to(member.getSubemail())
                                    .subject("[Woori] 이메일 찾기 입니다")
                                    .build();
                            emailService.sendsubEmail(emailMessage, "ko-subemail", member.getEmail());
                        } else if (member.getLanguage().equals("ja")) {
                            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                                    .to(member.getSubemail())
                                    .subject("[Woori] 電子メールアカウントを探す")
                                    .build();
                            emailService.sendsubEmail(emailMessage, "ja-subemail", member.getEmail());
                        } else {
                            EmailMessageEntity emailMessage = EmailMessageEntity.builder()
                                    .to(member.getSubemail())
                                    .subject("[Woori] Looking for an email account")
                                    .build();
                            emailService.sendsubEmail(emailMessage, "en-subemail", member.getEmail());
                        }
                    }

//                    EmailResponseDTO emailResponseDto = new EmailResponseDTO();
//                    emailResponseDto.setCode(code);
//                    log.warn(code);
//

//                    return ResponseEntity.ok(emailResponseDto);
                        responseMemberDTO = memberDTO.builder()
                                .email(member.getEmail())
                                .subemail(member.getSubemail())
                                .build();

                    }else {
                        responseMemberDTO = memberDTO.builder()
                                .email(member.getEmail())
                                .subemail(member.getSubemail())
                                .build();
                    }
            return ResponseEntity.ok().body(responseMemberDTO);
        } catch (NullPointerException nullPointerException) {
            log.warn("nullPoint");
            MemberDTO reMemberDTO = memberDTO.builder().build();
            return ResponseEntity.ok().body(reMemberDTO);
        }
    }

    /**
     * Gets sub email.
     * comment : subemail을 찾기 위한 PostMapping  메서드
     * author : DGeon
     * date : 2023-06-23
     * description :
     *
     * @param memberDTO the member dto
     * @return the sub email
     */
//    @PostMapping("findsubemail")
//    public ResponseEntity<?> getSubEmail(@RequestBody MemberDTO memberDTO) {
//
//        try {
//            MemberEntity member = memberService.findBySubEmail(memberDTO.getSubemail());
//            MemberDTO responseMemberDTO = memberDTO.builder()
//                    .email(member.getEmail())
//                    .build();
//            return ResponseEntity.ok().body(responseMemberDTO);
//
//        } catch (NullPointerException nullPointerException) {
//            MemberDTO reMemberDTO = memberDTO.builder().build();
//            return ResponseEntity.ok().body(reMemberDTO);
//        }
//    }

    /**
     * methodName : updatePassword
     * comment : 비밀번호 변경(forgotpassword)
     * author : DGeon
     * date : 2023-06-17
     * description :
     *
     * @param memberDTO the member dto
     * @return response entity
     */
    @PutMapping("updatePassword")
    public ResponseEntity<String> updatePassword(@RequestBody MemberDTO memberDTO) {

        boolean updated = memberService.updatePassword(memberDTO.getEmail(),
                passwordEncoder.encode(memberDTO.getPassword()));
        if (updated) {
            return new ResponseEntity<>("회원 정보가 성공적으로 업데이트되었습니다.", HttpStatus.OK);
        }
        return new ResponseEntity<>("회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping
    @Transactional
    public void deleteMember(@AuthenticationPrincipal String email){
//        List<ScheduleEntity> scheduleEntityList = scheduleService.retrieveByEmail(email); // 캘린더 번호를 통하여 일정 조회
//
//        // 일정 삭제
//        for (ScheduleEntity scheduleEntity : scheduleEntityList) {
//            scheduleService.delete(scheduleEntity);
//        }
//
//        // 공유된 일정 삭제 및 캘린더 삭제
//        List<ShareEntity> shareEntityList = shareService.retrieveByEmail(email);
//        if (shareEntityList.size() == 1) {
//            // 공유된 일정이 한 명에게만 공유되었을 경우 캘린더 삭제
//            ShareEntity shareEntity = shareEntityList.get(0);
//            shareService.delete(shareEntity); // 공유 삭제
//            CalendarEntity calendarEntity = shareEntity.getCalendarEntity();
//            calendarService.delete(calendarEntity); // 캘린더 삭제
//        } else {
//            // 공유된 일정이 여러 명에게 공유되었을 경우에는 공유만 삭제
//            for (ShareEntity shareEntity : shareEntityList) {
//                shareService.delete(shareEntity); // 공유 삭제
//            }
//        }
//
//        // 알림 삭제
//        List<NotificationEntity> notificationEntities = notificationService.retrieve(email);
//        for (NotificationEntity notificationEntity : notificationEntities) {
//            notificationService.delete(notificationEntity);
//        }
//
//        // 멤버 삭제
//        MemberEntity memberEntity = memberService.findByEmail(email);
//        if (memberEntity != null) {
//            memberService.deleteMember(memberEntity);
//            return ResponseEntity.ok().build();
//        } else {
//            return ResponseEntity.notFound().build();
//        }
            MemberEntity member = memberService.findByEmail(email);

            if(shareService.retrieveByEmail(email) != null){
                List<ShareEntity> shareEntities = shareService.retrieveByEmail(email);
                for (ShareEntity shareEntity : shareEntities) {
                    shareService.delete(shareEntity);
                }
            }

            if(!(notificationService.retrieve(email).isEmpty())){
                List<NotificationEntity> notificationEntities = notificationService.retrieve(email);
                for (NotificationEntity notificationEntity : notificationEntities){
                    notificationService.delete(notificationEntity);
                }
            }
            memberService.deleteMember(member);
//        List<ShareEntity> shareEntities = shareService.retrieveByEmail(email);
//        for(int i=0; i<=shareEntities.size(); i++){
//            shareService.delete(shareEntities.get(i));
//        }
//        return ResponseEntity.ok().build();
    }

    /**
     * methodName : cal
     * comment : 대한민국 대체공휴일 및 국경일 불러오는 GetMapping 메서드 (1년에 1번 정도 실행하면 된다)
     * author : DGeon
     * date : 2023-06-23
     * description :
     *
     * @param scheduleDTO the schedule dto
     * @param email       the email
     * @throws IOException    the io exception
     * @throws ParseException the parse exception
     */
    @GetMapping("cal")
    public void cal(ScheduleDTO scheduleDTO, String email) throws IOException, ParseException {
        String AnniversaryInfo = "http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getAnniversaryInfo";
        String HoliDeInfo = "http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getHoliDeInfo";
        StringBuilder urlBuilder = new StringBuilder(HoliDeInfo); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=4chyTuzqZyZy3L0j6TzepfD0SEeHJM5J7yGtEX%2F8HQeD4fZofd%2B%2FseXdgoveey8ibGuH9KHKmqTkZc3ztsbvVQ%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("solYear", "UTF-8") + "=" + URLEncoder.encode("2024", "UTF-8")); /*연*/
//        urlBuilder.append("&" + URLEncoder.encode("solMonth", "UTF-8") + "=" + URLEncoder.encode("06", "UTF-8")); /*월*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        String xml = sb.toString();
        JSONObject jsonObject = convertXmlToJson(xml);
        JSONArray itemArray = jsonObject.getJSONObject("response")
                .getJSONObject("body")
                .getJSONObject("items")
                .getJSONArray("item");

        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < itemArray.length(); i++) {
            JSONObject itemObject = itemArray.getJSONObject(i);
            String dateName = itemObject.getString("dateName");
            String dateFull = String.valueOf(itemObject.getInt("locdate"));
            String year = dateFull.substring(0,4);
            String month = dateFull.substring(4,6);
            String day = dateFull.substring(6,8);
            String date = year+"-"+month+"-"+day;

//            Date dd = Date.parse(date);
            ScheduleEntity entity = ScheduleEntity .builder()
                    .name(dateName)
                    .startDate(Timestamp.valueOf(date + " 00:00:00"))
                    .endDate(Timestamp.valueOf(date + " 00:00:00"))
                    .calendarEntity(CalendarEntity.builder().calNo(98L).build())
                    .build();
            scheduleService.create(entity);

        }
    }


    private JSONObject convertXmlToJson(String xml) {
        try {
            JSONObject jsonObject = XML.toJSONObject(xml);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static void xmlToJson(String str) {
//
//        try{
//            JSONObject jObject = XML.toJSONObject(str);
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.enable(SerializationFeature.INDENT_OUTPUT);
//            Object json = mapper.readValue(jObject.toString(), Object.class);
//            String output = mapper.writeValueAsString(json);
//            System.out.println(output);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
