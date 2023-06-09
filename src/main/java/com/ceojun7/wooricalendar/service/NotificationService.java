package com.ceojun7.wooricalendar.service;

import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.NotificationEntity;
import com.ceojun7.wooricalendar.persistence.CalendarRepository;
import com.ceojun7.wooricalendar.persistence.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * The type Notification service.
 *
 * @author : Hamdoson
 * @packageName : com.ceojun7.wooricalendar.service
 * @fileName : NotificationService
 * @date : 2023-06-01
 * @description :
 * ===========================================================
 * DATE           AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-06-01     Hamdoson           최초 생성
 * 2023-06-05     Hamdoson           create, retrieve 생성
 * 2023-06-08     Hamdoson           delete 생성
 */
@Service
@Slf4j
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    /**
     * methodName : create
     * comment : Create Notification
     * author : Hamdoson
     * date : 2023-06-05
     * description : Notification Create생성
     *
     * @param notificationEntity the notification entity
     * @return the list
     */
    public List<NotificationEntity> create(final NotificationEntity notificationEntity) {
        notificationRepository.save(notificationEntity);

        CalendarEntity calendarEntity = calendarRepository.findByCalNo(notificationEntity.getCalendarEntity().getCalNo()).get(0);
        return notificationRepository.findByCalendarEntity_CalNo(calendarEntity.getCalNo());
    }

    /**
     * methodName : retrieve
     * comment : 이메일을 통한 알림 조회
     * author : Hamdoson
     * date : 2023-06-05
     * description : 수신자의 이메일을 통한 알림을 조회한다.
     *
     * @param revEmail the rev email
     * @return the list
     */
    public List<NotificationEntity> retrieve(String revEmail) {
        return notificationRepository.findByRevEmail(revEmail);
    }
    /**
     * methodName : update
     * comment : 알림 수신 업데이트
     * author : Hamdoson
     * date : 2023-06-16
     * description : rDate를 현재시간으로 업데이트 함으로서 수신일 생성
     *
     * @param entity the entity
     * @return the list
     */
    public List<NotificationEntity> update(final NotificationEntity entity) {

        final List<NotificationEntity> entities = notificationRepository.findByRevEmail(entity.getRevEmail());
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).getRdate() == null) {
                entities.get(i).setRdate(new Date());
                notificationRepository.save(entities.get(i));
            }
        }
        return notificationRepository.findByRevEmail(entity.getRevEmail());
    }

    /**
     * methodName : delete
     * comment : 알림 삭제
     * author : Hamdoson
     * date : 2023-06-08
     * description : 알림번호를 통해 알림을 삭제한다.
     *
     * @param entity the entity
     * @return the list
     */
    public List<NotificationEntity> delete(final NotificationEntity entity) {
        try {
            notificationRepository.delete(entity);
        } catch (Exception e) {
            log.error("error deleting entity ", entity.getRevEmail(), e);
            throw new RuntimeException("error deleting entity " + entity.getRevEmail());
        }
        return retrieve(entity.getRevEmail());
    }
}
