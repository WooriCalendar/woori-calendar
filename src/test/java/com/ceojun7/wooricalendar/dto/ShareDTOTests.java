package com.ceojun7.wooricalendar.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author : 박현민
 * @packageName : com.ceojun7.wooricalendar.dto
 * @fileName : ShareDTOTests
 * @date : 2023-06-14
 * @description : 공유 DTO 테스트
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023-06-14 박현민 최초 생성
 */

public class ShareDTOTests {

  private ShareDTO shareDTO;

  @Test
  public void testShareDTO() {
    shareDTO = ShareDTO
        .builder()
        .calNo(57L)
        .email("parkhm323@gmail.com")
        .checked(true)
        .grade(1L)
        .build();

    // 테스트
    assertEquals(57L, shareDTO.getCalNo());
    assertEquals("parkhm323@gmail.com", shareDTO.getEmail());
    assertEquals(true, shareDTO.isChecked());
    assertEquals(1L, shareDTO.getGrade());

  }
}
