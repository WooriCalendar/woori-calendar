package com.ceojun7.wooricalendar.dto;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InviteDTO {
  private String email;
  private Long calNo;
  private String name;
  private Long grade;
}
