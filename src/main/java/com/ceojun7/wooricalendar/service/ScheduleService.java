package com.ceojun7.wooricalendar.service;

import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.ScheduleEntity;
import com.ceojun7.wooricalendar.model.ShareEntity;
import com.ceojun7.wooricalendar.persistence.CalendarRepository;
import com.ceojun7.wooricalendar.persistence.ScheduleRepository;
import com.ceojun7.wooricalendar.persistence.ShareRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @packageName : com.ceojun7.wooricalendar.service
 * @fileName : CalendarService.java
 * @author : seolha86, 강태수
 * @date : 2023.05.31
 * @description :
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023.05.31 seolha86 최초 생성
 *              2023.06.01 seolha86 create 기능추가
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ScheduleEntity> create(final ScheduleEntity entity) {
        scheduleRepository.save(entity);
        CalendarEntity calendarEntity = calendarRepository.findByCalNo(entity.getCalendarEntity().getCalNo()).get(0);

        return scheduleRepository.findByCalendarEntity_CalNo(calendarEntity.getCalNo());
    }

    public List<ScheduleEntity> retrieve(Long scNo) {
        return scheduleRepository.findByScNo(scNo);
    }

    /**
     * methodName : day
     * comment : 시작날짜로 조회
     * author : 강태수
     * date : 2023-06-02
     * description :
     *
     * @param startDate
     * @return scheduleRepository
     * 
     */

    public List<ScheduleEntity> day(Timestamp startDate) {
        return scheduleRepository.findByStartDate(startDate);
    }

    /**
     * methodName : update
     * comment : 캘린더 캘린더번호 내용 이름 시간대 수정
     * author : 강태수
     * date : 2023-06-02
     * description :
     *
     * @param entity
     * @return the list
     * 
     */
    public List<ScheduleEntity> update(final ScheduleEntity entity) {
        final List<ScheduleEntity> originalList = scheduleRepository
                .findByCalendarEntity_CalNo(entity.getCalendarEntity().getCalNo());
        if (!originalList.isEmpty()) {
            ScheduleEntity original = originalList.get(0);
            original.setComment(entity.getComment());
            original.setName(entity.getName());
            original.setStartDate(entity.getStartDate());
            original.setEndDate(entity.getEndDate());
            original.setPlace(entity.getPlace());
            original.setRePeriod(entity.getRePeriod());

            scheduleRepository.save(original);
        }
        List<ScheduleEntity> updatedList = scheduleRepository
                .findByCalendarEntity_CalNo(entity.getCalendarEntity().getCalNo());
        return updatedList;
    }

    /**
     * methodName : delete
     * comment : 캘린더 삭제
     * author : 강태수
     * date : 2023-06-02
     * description :
     *
     * @param entity
     * @return scheduleRepository
     * 
     */
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

    public List<ScheduleEntity> retrieveByCalNo(Long calNo) {
        return scheduleRepository.findByCalendarEntity_CalNo(calNo);
    }

    public List<Map<String, Object>> search(String email, String name) {
        return jdbcTemplate.queryForList("select ts.*, email, color" +
                " from tbl_schedule ts" +
                " join tbl_calendar tc using(calNo)" +
                " join tbl_share tsh using(calNo)" +
                " join tbl_member tm using(email)" +
                " where ts.name like'%" + name + "%'" +
                " and email = ?", email);
    }
}
