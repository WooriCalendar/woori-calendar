package com.ceojun7.wooricalendar.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.Date;
import java.sql.Timestamp;

/**
 * @packageName    : com.ceojun7.wooricalendar.model
 * @fileName       : ScheduleEntity.java
 * @author         : seolha86
 * @date           : 2023.05.31
 * @description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023.05.31         seolha86           최초 생성
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_schedule")
@DynamicInsert
public class ScheduleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scNo;
    private String name;
    private String comment;
    private String place;

    private Timestamp startTime;
    private Timestamp endTime;
    private Date startDate;
    private Date endDate;

    private Date regDate;
    private Date updateDate;

    private Timestamp reEndDate;
    private String rePeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calNo")
    private CalendarEntity calendarEntity;

    @Override
    public String toString() {
        return "ScheduleEntity{" +
                "scNo=" + scNo +
                ", name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", place='" + place + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", regDate=" + regDate +
                ", updateDate=" + updateDate +
                ", reEndDate=" + reEndDate +
                ", rePeriod='" + rePeriod + '\'' +
                '}';
    }
}
