package com.ceojun7.wooricalendar.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.MemberEntity;
import com.ceojun7.wooricalendar.model.ShareEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @packageName : com.ceojun7.wooricalendar.controller
 * @fileName : ShareDTO.java
 * @author : 박현민
 * @date : 2023.06.02
 * @description : 공유(share)
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023.06.02 박현민 최초 생성
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShareDTO {
  private Long shareNo;
  private Long calNo;
  private String email;
  private boolean checked;
  private Date regDate;
  private Date updateDate;
  private Long grade;

  private String calName;
  private String color;

  private List<ScheduleDTO> schedule = new ArrayList<>();

  // entity > dto
  public ShareDTO(final ShareEntity entity) {
    this.shareNo = entity.getShareNo();
    this.calNo = entity.getCalendarEntity().getCalNo();
    this.email = entity.getMemberEntity().getEmail();
    this.checked = entity.isChecked();
    this.regDate = entity.getRegDate();
    this.updateDate = entity.getUpdateDate();
    this.grade = entity.getGrade();
    this.calName = entity.getCalendarEntity().getName();
    this.color = entity.getCalendarEntity().getColor();

    for (int i = 0; i < entity.getCalendarEntity().getSchedules().size(); i++) {
        this.schedule.add(new ScheduleDTO(entity.getCalendarEntity().getSchedules().get(i)));
    }
  }

  // dto > entity
  public static ShareEntity toEntity(final ShareDTO dto) {
    return ShareEntity.builder()
        .shareNo(dto.getShareNo())
        .calendarEntity(CalendarEntity.builder().calNo(dto.calNo).build())
        .memberEntity(MemberEntity.builder().email(dto.email).build())
        .checked(dto.isChecked())
        .regDate(dto.getRegDate())
        .updateDate(dto.getUpdateDate())
        .grade(dto.getGrade())
        .build();
  }
}

