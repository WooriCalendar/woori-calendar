package com.ceojun7.wooricalendar.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ceojun7.wooricalendar.dto.ResponseDTO;
import com.ceojun7.wooricalendar.dto.ShareDTO;
import com.ceojun7.wooricalendar.model.ShareEntity;
import com.ceojun7.wooricalendar.security.JwtAuthenticationFilter;
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
 */

@Slf4j
@RestController
@RequestMapping("share")
public class ShareController {

  @Autowired
  private ShareService service;

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
   * comment : 캘린더 사용 권한 삭제
   * author : 박현민
   * date : 2023-06-08
   * description :
   *
   * @param dto
   * @return the response entity
   */
  @DeleteMapping
  public ResponseEntity<?> deleteShare(@RequestBody ShareDTO dto) {
    log.warn(String.valueOf(dto));
    try {
      ShareEntity entity = ShareDTO.toEntity(dto);
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

}