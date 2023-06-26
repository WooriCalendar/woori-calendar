package com.ceojun7.wooricalendar.controller;

import com.ceojun7.wooricalendar.dto.ResponseDTO;
import com.ceojun7.wooricalendar.dto.ScheduleDTO;
import com.ceojun7.wooricalendar.dto.ShareDTO;
import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.ScheduleEntity;
import com.ceojun7.wooricalendar.model.ShareEntity;
import com.ceojun7.wooricalendar.service.CalendarService;
import com.ceojun7.wooricalendar.service.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @packageName : com.ceojun7.wooricalendar.contorller
 * @fileName : ScheduleController.java
 * @author : seolha86
 * @date : 2023.05.31
 * @description :
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023.05.31 seolha86 최초 생성
 *              2023.05.31 seolha86 create, retrieve 생성
 *              2023.06.01 강태수 update, delete, day 생성
 *              2023.06.08 seolha86 retrieve 수정 (calNo -> email)
 *              2023.06.20 seolha86 retrieveScheduleByScNo 추가
 */
@RestController
@RequestMapping("schedule")
@Slf4j
public class ScheduleController {
    @Autowired
    private ScheduleService service;

    @Autowired
    private CalendarService calendarService;

    /**
     * methodName : createSchedule
     * comment : 일정 생성
     * author : seolha86
     * date : 2023-05-31
     * description :
     *
     * @param dto the dto
     * @return the response entity
     */
    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody ScheduleDTO dto, @AuthenticationPrincipal String email) {
        log.warn(String.valueOf(dto));
        try {
            ScheduleEntity entity = ScheduleDTO.toEntity(dto);
            if (dto.getCalNo() == null) {
                entity.setCalendarEntity(CalendarEntity.builder().calNo(calendarService.retrieveByEmail(email).stream().filter(calendarEntity -> calendarEntity.getName().equals(email)).collect(Collectors.toList()).get(0).getCalNo()).build());
            }
            List<ScheduleEntity> entities = service.create(entity);
            List<ScheduleDTO> dtos = entities.stream().map(ScheduleDTO::new).collect(Collectors.toList());
            ResponseDTO<ScheduleDTO> response = ResponseDTO.<ScheduleDTO>builder().data(dtos).build();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseDTO.<ScheduleDTO>builder().error(e.getMessage()).build());
        }
    }

    /**
     * methodName : retrieveSchedule
     * comment : 사용자의 이메일로 일정 조회
     * author : seolha86
     * date : 2023-06-01
     * description :
     *
     * @param email the email
     * @return the response entity
     */
    @GetMapping
    public ResponseEntity<?> retrieveSchedule(@AuthenticationPrincipal String email) {
        List<ScheduleEntity> entities = service.retrieveByEmail(email);
        List<ScheduleDTO> dtos = entities.stream().map(ScheduleDTO::new).collect(Collectors.toList());
        ResponseDTO<ScheduleDTO> response = ResponseDTO.<ScheduleDTO>builder().data(dtos).build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{scNo}")
    public ResponseEntity<?> retrieveScheduleByScNo(@PathVariable Long scNo) {
        List<ScheduleEntity> entities = service.retrieve(scNo);
        List<ScheduleDTO> dtos = entities.stream().map(ScheduleDTO::new).collect(Collectors.toList());
        ResponseDTO<ScheduleDTO> response = ResponseDTO.<ScheduleDTO>builder().data(dtos).build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/day")
    public ResponseEntity<?> daySchedule(@RequestBody ScheduleDTO dto) {
        // log.warn(String.valueOf(dto.getCalNo()));
        List<ScheduleEntity> entities = service.day(Timestamp.valueOf(dto.getStart()));
        List<ScheduleDTO> dtos = entities.stream().map(ScheduleDTO::new).collect(Collectors.toList());
        ResponseDTO<ScheduleDTO> response = ResponseDTO.<ScheduleDTO>builder().data(dtos).build();
        return ResponseEntity.ok().body(response);
    }

    @PutMapping
    public ResponseEntity<?> updateSchedule(@RequestBody ScheduleDTO dto) {
        log.warn(String.valueOf(dto));
        try {
            ScheduleEntity entity = ScheduleDTO.toEntity(dto);
            List<ScheduleEntity> entities = service.update(entity);
            List<ScheduleDTO> dtos = entities.stream().map(ScheduleDTO::new).collect(Collectors.toList());
            ResponseDTO<ScheduleDTO> response = ResponseDTO.<ScheduleDTO>builder().data(dtos).build();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseDTO.<ScheduleDTO>builder().error(e.getMessage()).build());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteSchedule(@RequestBody ScheduleDTO dto) {
        log.warn(String.valueOf(dto));
        try {
            ScheduleEntity entity = ScheduleDTO.toEntity(dto);
            List<ScheduleEntity> entities = service.delete(entity);
            List<ScheduleDTO> dtos = entities.stream().map(ScheduleDTO::new).collect(Collectors.toList());
            ResponseDTO<ScheduleDTO> response = ResponseDTO.<ScheduleDTO>builder().data(dtos).build();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ResponseDTO.<ScheduleDTO>builder().error(e.getMessage()).build());
        }
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchSchedule(@AuthenticationPrincipal String email, @RequestBody ScheduleDTO dto) {
//        List<Map<String, Object>> entities = service.search(email, dto.getTitle());
//        List<ShareDTO> dtos = entities.stream().map(ShareDTO).collect(Collectors.toList());
//        ResponseDTO<ShareDTO> response = ResponseDTO.<ShareDTO>builder().data(dtos).build();
        return ResponseEntity.ok().body(service.search(email, dto.getTitle()));
    }

//    @GetMapping
//    public ResponseEntity<?> retrieveSchedule(@AuthenticationPrincipal String email) {
//        List<ScheduleEntity> entities = service.retrieveByEmail(email);
//        List<ScheduleDTO> dtos = entities.stream().map(ScheduleDTO::new).collect(Collectors.toList());
//        ResponseDTO<ScheduleDTO> response = ResponseDTO.<ScheduleDTO>builder().data(dtos).build();
//        return ResponseEntity.ok().body(response);
//    }
}