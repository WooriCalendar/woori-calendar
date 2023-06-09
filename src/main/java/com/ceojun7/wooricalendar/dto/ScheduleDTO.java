package com.ceojun7.wooricalendar.dto;

import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.ScheduleEntity;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author : seolha86
 * @packageName : com.ceojun7.wooricalendar.dto
 * @fileName : ScheduleDTO
 * @date : 2023-05-31
 * @description :
 * ===========================================================
 * DATE           AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-05-31       seolha86             최초 생성
 * 2023-06-12       seolha86             start, end 수정
 * 2023-06-13       seolha86             rrule 추가
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class ScheduleDTO {
    private Long scNo;
    private String title;
    private String comment;
    private String place;
    //    private Timestamp startTime;
//    private Timestamp endTime;
    private String start;
    private String end;
    private Date regDate;
    private Date updateDate;
    private Long calNo;

    private String color;

    private RRuleDTO rrule;

    private boolean status;

    private String dayOfWeek;

    private boolean allDay;

//    public ScheduleDTO(Long scNo, String title, String comment, String place, String start, String end, Date regDate, Date updateDate, Long calNo) {
//        this.scNo = scNo;
//        this.title = title;
//        this.comment = comment;
//        this.place = place;
//        this.start = start;
//        this.end = end;
//        this.regDate = regDate;
//        this.updateDate = updateDate;
//        this.calNo = calNo;
//    }

    public ScheduleDTO(final ScheduleEntity entity) {
        this.scNo = entity.getScNo();
        this.title = entity.getName();
        this.comment = entity.getComment();
        this.place = entity.getPlace();

        if (entity.getStartTime() == null && entity.getEndTime() == null) {
            SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");

            this.start = dtFormat.format(entity.getStartDate());
            this.end = dtFormat.format(entity.getEndDate());
            this.allDay = true;

//            Calendar cal = Calendar.getInstance();
//            cal.set(entity.getEndDate().getYear(), entity.getEndDate().getMonth() - 1, entity.getEndDate().getDate());
//
//            if (entity.getEndDate().getDate() == cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
//                entity.getEndDate().setDate(1);
//                cal.set(entity.getEndDate().getYear(), entity.getEndDate().getMonth(), entity.getEndDate().getDate());
//                this.end = dtFormat.format(cal.getTime());
//            } else {
//                this.end = dtFormat.format(entity.getEndDate()).substring(0, dtFormat.format(entity.getEndDate()).length() - 2) +
//                        (Integer.parseInt(dtFormat.format(entity.getEndDate()).substring(dtFormat.format(entity.getEndDate()).length() - 2)) + 1);
//            }

            if (entity.getCalendarEntity().getCalNo() != 90 && entity.getCalendarEntity().getCalNo() != 98) {
                LocalDate date = LocalDate.of(entity.getStartDate().getYear(), entity.getStartDate().getMonth(), entity.getStartDate().getDate());
                DayOfWeek day = date.getDayOfWeek();

                this.dayOfWeek = day.getDisplayName(TextStyle.SHORT, Locale.US);
            }
        } else {
            LocalDate date = LocalDate.of(entity.getStartTime().getYear(), entity.getStartTime().getMonth(), entity.getStartTime().getDate());
            DayOfWeek day = date.getDayOfWeek();

            this.start = String.valueOf(entity.getStartTime());
            this.end = String.valueOf(entity.getEndTime());

            if (entity.getCalendarEntity().getCalNo() != 90 && entity.getCalendarEntity().getCalNo() != 98) {
                this.dayOfWeek = day.getDisplayName(TextStyle.SHORT, Locale.US);
            }
        }

        this.calNo = entity.getCalendarEntity().getCalNo();
        this.color = entity.getCalendarEntity().getColor();

        if (entity.getRePeriod() != null) {
            this.rrule = new RRuleDTO(entity);
        }

//        this.isExclusive = true;
    }

    public static ScheduleEntity toEntity(final ScheduleDTO dto) {
        if (dto.getRrule().getFreq() == null && dto.status) {
            return ScheduleEntity.builder().scNo(dto.getScNo()).name(dto.getTitle()).comment(dto.getComment()).place(dto.getPlace())
                    .startDate(Timestamp.valueOf(dto.getStart() + " 00:00:00")).endDate(Timestamp.valueOf(dto.getEnd() + " 00:00:00"))
                    .calendarEntity(CalendarEntity.builder().calNo(dto.getCalNo()).build()).build();
        } else if (dto.getRrule().getFreq() == null && !dto.status) {
            return ScheduleEntity.builder().scNo(dto.getScNo()).name(dto.getTitle()).comment(dto.getComment()).place(dto.getPlace())
                    .startTime(Timestamp.valueOf(dto.getStart())).endTime(Timestamp.valueOf(dto.getEnd()))
                    .calendarEntity(CalendarEntity.builder().calNo(dto.calNo).build()).build();
        } else if (dto.getRrule().getFreq() != null && dto.status) {
            return ScheduleEntity.builder().scNo(dto.getScNo()).name(dto.getTitle()).comment(dto.getComment()).place(dto.getPlace())
                    .startDate(Timestamp.valueOf(dto.getStart() + " 00:00:00")).endDate(Timestamp.valueOf(dto.getEnd() + " 00:00:00"))
                    .calendarEntity(CalendarEntity.builder().calNo(dto.getCalNo()).build()).reEndDate(Timestamp.valueOf(dto.getRrule().getUntil() + " 00:00:00")).rePeriod(dto.getRrule().getFreq()).build();
        } else {
            return ScheduleEntity.builder().scNo(dto.getScNo()).name(dto.getTitle()).comment(dto.getComment()).place(dto.getPlace())
                    .startTime(Timestamp.valueOf(dto.getStart())).endTime(Timestamp.valueOf(dto.getEnd()))
                    .calendarEntity(CalendarEntity.builder().calNo(dto.calNo).build()).reEndDate(Timestamp.valueOf(dto.rrule.getUntil() + " 00:00:00")).rePeriod(dto.getRrule().getFreq()).build();
        }
    }
}

//    public static ScheduleEntity toEntity(final ScheduleDTO dto) {
//        if (dto.getRrule().getFreq() == null && dto.status) {
//            return ScheduleEntity.builder().scNo(dto.getScNo()).name(dto.getTitle()).comment(dto.getComment()).place(dto.getPlace())
//                    .startDate(Timestamp.valueOf(dto.getStart() + " 00:00:00")).endDate(Timestamp.valueOf(dto.getEnd() + " 00:00:00"))
//                    .calendarEntity(CalendarEntity.builder().calNo(dto.getCalNo()).build()).build();
//        } else if (dto.getRrule().getFreq() == null && !dto.status) {
//            return ScheduleEntity.builder().scNo(dto.getScNo()).name(dto.getTitle()).comment(dto.getComment()).place(dto.getPlace())
//                    .startTime(Timestamp.valueOf(dto.getStart())).endTime(Timestamp.valueOf(dto.getEnd()))
//                    .calendarEntity(CalendarEntity.builder().calNo(dto.calNo).build()).build();
//        } else if (dto.getRrule().getFreq() != null && dto.status) {
//            return ScheduleEntity.builder().scNo(dto.getScNo()).name(dto.getTitle()).comment(dto.getComment()).place(dto.getPlace())
//                    .startDate(Timestamp.valueOf(dto.getStart() + " 00:00:00")).endDate(Timestamp.valueOf(dto.getEnd() + " 00:00:00"))
//                    .calendarEntity(CalendarEntity.builder().calNo(dto.getCalNo()).build()).reEndDate(Timestamp.valueOf(dto.getRrule().getUntil() + " 00:00:00")).rePeriod(dto.getRrule().getFreq()).build();
//        } else {
//            return ScheduleEntity.builder().scNo(dto.getScNo()).name(dto.getTitle()).comment(dto.getComment()).place(dto.getPlace())
//                    .startTime(Timestamp.valueOf(dto.getStart())).endTime(Timestamp.valueOf(dto.getEnd()))
//                    .calendarEntity(CalendarEntity.builder().calNo(dto.calNo).build()).reEndDate(Timestamp.valueOf(dto.rrule.getUntil() + " 00:00:00")).rePeriod(dto.getRrule().getFreq()).build();
//        }
//    }

