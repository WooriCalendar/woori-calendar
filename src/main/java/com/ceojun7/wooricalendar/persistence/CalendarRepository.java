package com.ceojun7.wooricalendar.persistence;

import com.ceojun7.wooricalendar.model.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @packageName    : com.ceojun7.wooricalendar.persistence
 * @fileName       : CalendarRepository.java
 * @author         : seolha86
 * @date           : 2023.05.31
 * @description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023.05.31         seolha86           최초 생성
 */

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, String> {
    /**
     * methodName : findByCalNo
     * comment : 캘린더 넘버로 캘린더 조회
     * author : seolha86
     * date : 2023-06-13
     * description :
     *
     * @param calNo the cal no
     * @return the list
     */
    List<CalendarEntity> findByCalNo(Long calNo);

    /**
     * methodName : findByName
     * comment : 캘린더 이름으로 캘린더 조회
     * author : seolha86
     * date : 2023-06-13
     * description :
     *
     * @param name the name
     * @return the list
     */
    List<CalendarEntity> findByName(String name);
}
