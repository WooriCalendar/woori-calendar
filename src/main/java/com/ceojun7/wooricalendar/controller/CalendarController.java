package com.ceojun7.wooricalendar.controller;

import com.ceojun7.wooricalendar.dto.CalendarDTO;
import com.ceojun7.wooricalendar.dto.ResponseDTO;
import com.ceojun7.wooricalendar.dto.ShareDTO;
import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.MemberEntity;
import com.ceojun7.wooricalendar.model.ScheduleEntity;
import com.ceojun7.wooricalendar.model.ShareEntity;
import com.ceojun7.wooricalendar.service.CalendarService;
import com.ceojun7.wooricalendar.service.ScheduleService;
import com.ceojun7.wooricalendar.service.ShareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @packageName : com.ceojun7.wooricalendar.contorller
 * @fileName : CalendarController.java
 * @author : seolha86, 강태수
 * @date : 2023.05.31
 * @description :
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023.05.31 seolha86 최초 생성
 *              2023.06.01 강태수 update, delete 생성
 *              2023.06.13 박현민 retrieveCalendar(calNo로 조회) 생성
 * 
 */
@RestController
@RequestMapping("calendar")
@Slf4j
public class CalendarController {
    @Autowired
    private CalendarService service;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private ShareService shareService;

    /**
     * methodName : createSchedule
     * comment : 새 calendar 생성
     * author : seolha86
     * date : 2023-06-01
     * description :
     *
     * @param dto the dto
     * @return the response entity
     * 
     * @DeleteMapping 생성
     */
    @PostMapping
    public ResponseEntity<?> createCalendar(@RequestBody CalendarDTO dto, @AuthenticationPrincipal String email) {
        log.warn(String.valueOf(dto));
        try {
            CalendarEntity entity = CalendarDTO.toEntity(dto);

            if (dto.getColor().trim().length() == 0) {
                Random r = new Random();
                entity.setColor(String.format("#%06x", r.nextInt(0xffffff + 1)));
            }

            List<CalendarEntity> entities = service.create(entity);
            List<CalendarDTO> dtos = entities.stream().map(CalendarDTO::new).collect(Collectors.toList());
            ResponseDTO<CalendarDTO> response = ResponseDTO.<CalendarDTO>builder().data(dtos).build();

            // 캘린더 생성 시 생성된 캘린더 구독
            ShareEntity shareEntity = ShareEntity.builder().calendarEntity(entity)
                    .memberEntity(MemberEntity.builder().email(email).build()).checked(true).build();
            shareService.create(shareEntity);
            log.warn("shareEntity");
            log.warn(String.valueOf(shareEntity));

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseDTO.<CalendarDTO>builder().error(e.getMessage()).build());
        }
    }

    /**
     * methodName : retrieveCalendar
     * comment : 회원의 이메일로 구독중인 캘린더 조회
     * author : seolha86
     * date : 2023-06-13
     * description :
     *
     * @param email the email
     * @return the response entity
     */
    @GetMapping
    public ResponseEntity<?> retrieveCalendar(@AuthenticationPrincipal String email) {
        List<CalendarEntity> entities = service.retrieveByEmail(email);
        List<CalendarDTO> dtos = entities.stream().map(CalendarDTO::new).collect(Collectors.toList());
        ResponseDTO<CalendarDTO> response = ResponseDTO.<CalendarDTO>builder().data(dtos).build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/share")
    public ResponseEntity<?> retrieveCalendarShared(@AuthenticationPrincipal String email) {
        List<ShareEntity> entities = service.retrieveByShareEntity_Email(email);
        List<ShareDTO> dtos = entities.stream().map(ShareDTO::new).collect(Collectors.toList());
        ResponseDTO<ShareDTO> response = ResponseDTO.<ShareDTO>builder().data(dtos).build();
        return ResponseEntity.ok().body(response);
    }

    /**
     * methodName : updateCalendar
     * comment : 캘린더 캘린더번호 내용 이름 시간대 수정
     * author : 강태수
     * date : 2023-06-01
     * description :
     *
     * @param dto the dto
     * @return the response entity
     * 
     */

    @PutMapping
    public ResponseEntity<?> updateCalendar(@RequestBody CalendarDTO dto) {
        CalendarEntity entity = CalendarDTO.toEntity(dto);

        List<CalendarEntity> entities = service.update(entity);
        List<CalendarDTO> dtos = entities.stream().map(CalendarDTO::new).collect(Collectors.toList());
        ResponseDTO<CalendarDTO> response = ResponseDTO.<CalendarDTO>builder().data(dtos).build();
        return ResponseEntity.ok().body(response);
    }

    /**
     * methodName : deleteCalendar
     * comment : 캘린더 삭제
     * author : 강태수
     * date : 2023-06-01
     * description : 일정 조회 후 존재시 삭제, 쉐어 조회 후 존재 시 삭제
     *
     * @param dto
     * @return the response entity
     * 
     */
    @DeleteMapping
    public ResponseEntity<?> deleteCalendar(@RequestBody Long calNo) {
        try {

            CalendarEntity calendarEntity = service.retrieve(calNo).get(0); //캘린더 번호를 통하여 캘린더 엔티티 조회
            // 스케쥴 조회 및 삭제
            List<ScheduleEntity> scheduleEntityList = scheduleService.retrieveByCalNo(calNo); //캘린더 번호를 통하여 일정 조회
            if (scheduleEntityList.size() > 0){ // 일정이 존재할 때
                for(int i = 0; i < scheduleEntityList.size(); i++){
                    scheduleService.delete(scheduleEntityList.get(i)); // 모든 일정 삭제
                }
            }
            // 쉐어 조회 및 삭제
            List<ShareEntity> shareEntityList = shareService.retrieveByCalNo(calNo); // 캘린더 번호를 통한 공유 조회
            if (shareEntityList.size() > 0){ // 구독자가 존재할 경우
                for(int i = 0; i < shareEntityList.size(); i++) {
                    shareService.delete(shareEntityList.get(i)); // 모든 공유 삭제
                }
            }

            // 일정과 공유를 삭제했으니, 이제 캘린더 삭제
            List<CalendarEntity> entities = service.delete(calendarEntity);
            List<CalendarDTO> dtos = entities.stream().map(CalendarDTO::new).collect(Collectors.toList());
            ResponseDTO<CalendarDTO> response = ResponseDTO.<CalendarDTO>builder().data(dtos).build();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseDTO.<CalendarDTO>builder().error(e.getMessage()).build());
        }

    }

    /**
     * methodName : retrieveCalendar
     * comment : 캘린더 번호로 조회
     * author : 박현민
     * date : 2023-06-13
     * description : 사용자가 가지고 있는 캘린더만 조회되게 수정해야 함
     *
     * @param calNo the calNo
     * @return the response entity
     */
    @GetMapping("/{calNo}")
    public ResponseEntity<?> retrieveCalendar(@PathVariable Long calNo) {
        List<CalendarEntity> entities = service.retrieve(calNo);
        List<CalendarDTO> dtos = entities.stream().map(CalendarDTO::new).collect(Collectors.toList());
        ResponseDTO<CalendarDTO> response = ResponseDTO.<CalendarDTO>builder().data(dtos).build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("cal")
    public void cal() throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getAnniversaryInfo"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=4chyTuzqZyZy3L0j6TzepfD0SEeHJM5J7yGtEX%2F8HQeD4fZofd%2B%2FseXdgoveey8ibGuH9KHKmqTkZc3ztsbvVQ%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("solYear","UTF-8") + "=" + URLEncoder.encode("2023", "UTF-8")); /*연*/
        urlBuilder.append("&" + URLEncoder.encode("solMonth","UTF-8") + "=" + URLEncoder.encode("06", "UTF-8")); /*월*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
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
        xmlToJson(xml);
//        JSONObject jObject = XML.toJSONObject(xml);
//        String jsonStr = jObject.toString();
//        System.out.println(jsonStr);
//        xmlToJson(xml);

//        System.out.println(sb.toString());
    }

    public static void xmlToJson(String str) {

        try{
            JSONObject jObject = XML.toJSONObject(str);
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            Object json = mapper.readValue(jObject.toString(), Object.class);
            String output = mapper.writeValueAsString(json);
            System.out.println(output);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
