package com.ceojun7.wooricalendar.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ceojun7.wooricalendar.dto.ResponseDTO;
import com.ceojun7.wooricalendar.dto.ShareDTO;
import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.ShareEntity;
import com.ceojun7.wooricalendar.persistence.ShareRepository;
import com.ceojun7.wooricalendar.service.ShareService;

import lombok.extern.slf4j.Slf4j;

/**
 * @packageName : com.ceojun7.wooricalendar.controller
 * @fileName : ShareController.java
 * @author : 박현민
 * @date : 2023.06.02
 * @description : 공유(share)
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023.06.02 박현민 최초 생성
 *              2023.06.07 박현민 create, update 추가
 *              2023.06.08 박현민 retrieve, delete 추가
 *              2023.06.22 박현민 mailShare 추가
 */

@Slf4j
@RestController
@RequestMapping("share")
public class ShareController {

  @Autowired
  private ShareService service;

  @Autowired
  private ShareRepository repository;

  /**
   * methodName : createShare
   * comment : 캘린더 사용 권한 생성
   * author : 박현민
   * date : 2023-06-07
   * description :
   *
   * @param dto
   * @return the response entity
   */

  @PostMapping
  public ResponseEntity<?> createShare(@RequestBody ShareDTO dto) {
    log.warn(String.valueOf(dto));
    try {
      ShareEntity entity = ShareDTO.toEntity(dto);
      List<ShareEntity> entities = service.create(entity);
      List<ShareDTO> dtos = entities.stream().map(ShareDTO::new).collect(Collectors.toList());
      ResponseDTO<ShareDTO> response = ResponseDTO.<ShareDTO>builder().data(dtos).build();
      return ResponseEntity.ok().body(response);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.badRequest().body(ResponseDTO.<ShareDTO>builder().error(e.getMessage()).build());
    }
  }

  /**
   * methodName : retrieveShare
   * comment : 캘린더 사용 권한 조회
   * author : 박현민
   * date : 2023-06-08
   * description :
   *
   * @param dto
   * @return the response entity
   */
  @GetMapping
  public ResponseEntity<?> retrieveShare(@RequestBody ShareDTO dto) {
    log.warn(String.valueOf(dto.getShareNo()));
    List<ShareEntity> entities = service.retrieve(dto.getShareNo());
    List<ShareDTO> dtos = entities.stream().map(ShareDTO::new).collect(Collectors.toList());
    ResponseDTO<ShareDTO> response = ResponseDTO.<ShareDTO>builder().data(dtos).build();
    return ResponseEntity.ok().body(response);
  }

  @GetMapping("/{shareNo}")
  public ResponseEntity<?> retrieveShare(@PathVariable Long shareNo) {
    List<ShareEntity> entities = service.retrieve(shareNo);
    List<ShareDTO> dtos = entities.stream().map(ShareDTO::new).collect(Collectors.toList());
    ResponseDTO<ShareDTO> response = ResponseDTO.<ShareDTO>builder().data(dtos).build();
    return ResponseEntity.ok().body(response);
  }

  // 캘린더 번호로 조회
  @GetMapping("/retrieve/{calNo}")
    public ResponseEntity<?> retrieveShareByCalNo(@PathVariable Long calNo) {
    List<ShareEntity> entities = service.retrieveByCalNo(calNo);
    List<ShareDTO> dtos = entities.stream().map(ShareDTO::new).collect(Collectors.toList());
    ResponseDTO<ShareDTO> response = ResponseDTO.<ShareDTO>builder().data(dtos).build();
    return ResponseEntity.ok().body(response);
}


  /**
   * methodName : updateShare
   * comment : 캘린더 사용 권한 수정
   * author : 박현민
   * date : 2023-06-07
   * description :
   *
   * @param dto
   * @return the response entity
   */
  @PutMapping
  public ResponseEntity<?> updateShare(@RequestBody ShareDTO dto) {
    log.warn(String.valueOf(dto));
    ShareEntity entity = ShareDTO.toEntity(dto);
    List<ShareEntity> entities = service.update(entity);
    List<ShareDTO> dtos = entities.stream().map(ShareDTO::new).collect(Collectors.toList());
    ResponseDTO<ShareDTO> response = ResponseDTO.<ShareDTO>builder().data(dtos).build();
    return ResponseEntity.ok().body(response);
  }

  /**
   * methodName : deleteShare
   * comment : 캘린더 사용 권한 삭제-구독 취소
   * author : 박현민
   * date : 2023-06-08
   * description :
   *
   * @param dto
   * @return the response entity
   */
  @DeleteMapping
  public ResponseEntity<?> deleteShare(@RequestBody Long shareNo) {
    log.warn("===========넘어옴====" + shareNo);
    try {

      ShareEntity entity = ShareEntity.builder()
          .shareNo(shareNo)
          .build();// 쉐어 넘버만 있는 엔티티 나머지 null

      List<ShareEntity> entities = service.delete(entity);
      List<ShareDTO> dtos = entities.stream().map(ShareDTO::new).collect(Collectors.toList());
      ResponseDTO<ShareDTO> response = ResponseDTO.<ShareDTO>builder().data(dtos).build();

      return ResponseEntity.ok().body(response);
    } catch (Exception e) {
      String error = e.getMessage();
      ResponseDTO<ShareDTO> response = ResponseDTO.<ShareDTO>builder().error(error).build();
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * methodName : mailShare
   * comment : 캘린더 초대 수락
   * author : 박현민
   * date : 2023-06-22
   * description :
   *
   * @param calNo, receiver, grade, response
   * @return null
   */
  @GetMapping("/{calNo}/{receiver}/{grade}")
  public String mailShare(@PathVariable String calNo, @PathVariable String receiver,
      @PathVariable String grade, HttpServletResponse response) throws IOException {
    Long longCalNo = Long.parseLong(calNo);
    ShareDTO dto = new ShareDTO();
    dto.setCalNo(Long.parseLong(calNo));
    dto.setEmail(receiver);
    dto.setChecked(true);
    dto.setGrade(Long.parseLong(grade));

    List<ShareEntity> entities = service.retrieveByEmail(receiver);
    for (int i = 0; i < entities.size(); i++) {
      if (entities.get(i).getCalendarEntity().getCalNo() == longCalNo) {
        log.info("이미 구독하고있습니다.");
        response.sendRedirect("http://localhost:3000/");
        return null;
      }
    }
    // 알림보내기
    // 보낼 내용 : receiver가 캘린더 초대를 수락했습니다.
    // 보낸 사람 : system
    // 받을 사람 : inviteDTO의 email(초대한 사람)

    ShareEntity entity = ShareDTO.toEntity(dto);
    service.create(entity);
    response.sendRedirect("http://localhost:3000/");
    return null;
  }
}