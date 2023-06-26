package com.ceojun7.wooricalendar.service;

import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.ShareEntity;
import com.ceojun7.wooricalendar.persistence.CalendarRepository;
import com.ceojun7.wooricalendar.persistence.ShareRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

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
 *              2023.05.31 seolha86 create 기능추가
 *              2023.06.01 강태수 update 기능추가
 *              2023.06.01 강태수 delete 기능추가
 */
@Service
@Slf4j
@Transactional
public class CalendarService {
    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private ShareRepository shareRepository;

    /**
     * methodName : create
     * comment : 새 calendar 생성
     * author : seolha86
     * date : 2023-06-01
     * description :
     *
     * @param calendarEntity the calendar entity
     * @return the list
     */
    public List<CalendarEntity> create(final CalendarEntity calendarEntity) {
        calendarRepository.save(calendarEntity);
        return calendarRepository.findByName(calendarEntity.getName());
    }

    public List<CalendarEntity> retrieve(Long calNo) {
        return calendarRepository.findByCalNo(calNo);
    };

    public List<CalendarEntity> retrieveByEmail(String email) {
        List<ShareEntity> shareList = shareRepository.findByMemberEntity_Email(email);
        List<CalendarEntity> calendarList = new ArrayList<>();

        for (ShareEntity shareEntity : shareList) {
            calendarList.add(shareEntity.getCalendarEntity());
        }

        return calendarList;
    }

    public List<ShareEntity> retrieveByShareEntity_Email(String email) {
        return shareRepository.findByMemberEntity_Email(email);
    }

    /**
     * methodName : delete
     * comment : 캘린더 삭제
     * author : 강태수
     * date : 2023-06-01
     * description :
     *
     * @param entity
     * @return the list
     * 
     */

    public List<CalendarEntity> delete(final CalendarEntity entity) {

        calendarRepository.delete(entity);

        return calendarRepository.findByCalNo(entity.getCalNo());
    }

    /**
     * methodName : update
     * comment : 캘린더 캘린더번호 내용 이름 시간대 수정
     * author : 강태수
     * date : 2023-06-01
     * description :
     *
     * @param entity
     * @return the list
     * 
     */

    public List<CalendarEntity> update(final CalendarEntity entity) {

        final List<CalendarEntity> originalList = calendarRepository.findByCalNo(entity.getCalNo());
        if (!originalList.isEmpty()) {
            CalendarEntity original = originalList.get(0);
            original.setCalNo(entity.getCalNo());
            original.setComment(entity.getComment());
            original.setName(entity.getName());
            original.setTimezone(entity.getTimezone());
            original.setColor(entity.getColor());

            calendarRepository.save(original);
        }
        return calendarRepository.findByCalNo(entity.getCalNo());
    }

    // final Optional<CalendarEntity> original =
    // calendarRepository.findByCalNo(entity.getCalNo());
    // original.ifPresent(calendar -> {
    // calendar.setCalNo(entity.getCalNo());
    // calendar.setComment(entity.getComment());
    // calendar.setName(entity.getName());
    // calendar.setTimezone(entity.getTimezone());

    // calendarRepository.save(calendar);
    // });
    // return calendarRepository.findByCalNo(entity.getCalNo());
    // }
}
