package com.ceojun7.wooricalendar.service;

import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.ScheduleEntity;
import com.ceojun7.wooricalendar.model.ShareEntity;
import com.ceojun7.wooricalendar.persistence.CalendarRepository;
import com.ceojun7.wooricalendar.persistence.ScheduleRepository;
import com.ceojun7.wooricalendar.persistence.ShareRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @packageName : com.ceojun7.wooricalendar.service
 * @fileName : CalendarService.java
 * @author : 김설하, 강태수
 * @date : 2023.05.31
 * @description :
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023.05.31 김설하 최초 생성
 *              2023.06.01 김설하 create 기능추가
 *              2023.06.02 강태수 update, delete, day 기능추가
 */
@Service
@Slf4j
@Transactional
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private ShareRepository shareRepository;

    public List<ScheduleEntity> create(final ScheduleEntity entity) {
        scheduleRepository.save(entity);
        CalendarEntity calendarEntity = calendarRepository.findByCalNo(entity.getCalendarEntity().getCalNo()).get(0);

        return scheduleRepository.findByCalendarEntity_CalNo(calendarEntity.getCalNo());
    }

    public List<ScheduleEntity> retrieve(Long calNo) {
        return scheduleRepository.findByCalendarEntity_CalNo(calNo);
    }

    public List<ScheduleEntity> day(Timestamp startDate) {
        return scheduleRepository.findByStartDate(startDate);
    }

    public List<ScheduleEntity> update(final ScheduleEntity entity) {
        final List<ScheduleEntity> originalList = scheduleRepository
                .findByCalendarEntity_CalNo(entity.getCalendarEntity().getCalNo());
        if (!originalList.isEmpty()) {
            ScheduleEntity original = originalList.get(0);
            original.setComment(entity.getComment());
            original.setName(entity.getName());

            scheduleRepository.save(original);
        }
        List<ScheduleEntity> updatedList = scheduleRepository
                .findByCalendarEntity_CalNo(entity.getCalendarEntity().getCalNo());
        return updatedList;
    }

    public List<ScheduleEntity> delete(final ScheduleEntity entity) {
        scheduleRepository.delete(entity);
        CalendarEntity calendarEntity = calendarRepository.findByCalNo(entity.getCalendarEntity().getCalNo()).get(0);

        return scheduleRepository.findByCalendarEntity_CalNo(calendarEntity.getCalNo());
    }

    public List<ScheduleEntity> retrieveByEmail(String email) {
        List<ShareEntity> list = shareRepository.findByMemberEntity_EmailAndChecked(email, true);
        List<ScheduleEntity> scheduleList = new ArrayList<>();

        for (ShareEntity shareEntity : list) {
            scheduleList.addAll(shareEntity.getCalendarEntity().getSchedules());
        }

        return scheduleList;
    }
}
