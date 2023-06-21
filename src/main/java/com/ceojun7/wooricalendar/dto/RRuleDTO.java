package com.ceojun7.wooricalendar.dto;

import com.ceojun7.wooricalendar.model.ScheduleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author : seolha86
 * @packageName : com.ceojun7.wooricalendar.dto
 * @fileName : RRuleDTO
 * @date : 2023-06-13
 * @description : 반복 규칙
 * ===========================================================
 * DATE           AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-06-13        seolha86             최초 생성
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RRuleDTO {
    private String dtstart;
    private String until = null;
    private String freq;

    public RRuleDTO(final ScheduleEntity entity) {
        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (entity.getStartDate() != null) {
            this.dtstart = dtFormat.format(entity.getStartDate());
        } else {
            this.dtstart = String.valueOf(entity.getStartTime());
        }
        this.until = dtFormat.format(entity.getReEndDate());
        this.freq = entity.getRePeriod();
    }
}
