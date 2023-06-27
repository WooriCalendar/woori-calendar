package com.ceojun7.wooricalendar.service;

import java.util.Date;
import java.util.List;

import com.ceojun7.wooricalendar.model.MemberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ceojun7.wooricalendar.model.CalendarEntity;
import com.ceojun7.wooricalendar.model.ScheduleEntity;
import com.ceojun7.wooricalendar.model.ShareEntity;
import com.ceojun7.wooricalendar.persistence.CalendarRepository;
import com.ceojun7.wooricalendar.persistence.ScheduleRepository;
import com.ceojun7.wooricalendar.persistence.ShareRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @packageName : com.ceojun7.wooricalendar.service
 * @fileName : ShareService.java
 * @author : 박현민
 * @date : 2023.06.05
 * @description :
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023.06.05 박현민 최초 생성
 *              2023.06.07 박현민 create, update 추가
 *              2023.06.08 박현민 retrieve, delete 추가
 */

@Slf4j
@Service
public class ShareService {

  @Autowired
  private ShareRepository shareRepository;

  @Autowired
  private CalendarRepository calendarRepository;

  @Autowired
  private CalendarService calendarService;

  @Autowired
  private ScheduleRepository scheduleRepository;
  @Autowired
  private ScheduleService scheduleService;

  /**
   * methodName : create
   * comment : 공유 권한 생성
   * author : 박현민
   * date : 2023-06-07
   * description :
   *
   * @param shareEntity
   * @return shareNo
   * 
   */
  public List<ShareEntity> create(final ShareEntity shareEntity) {
    shareRepository.save(shareEntity);

    return shareRepository.findByShareNo(shareEntity.getShareNo());
  }

  /**
   * methodName : retrieve
   * comment : 공유 권한 조회
   * author : 박현민
   * date : 2023-06-08
   * description :
   *
   * @param shareNo
   * @return shareNo
   * 
   */
  public List<ShareEntity> retrieve(final Long shareNo) {
    return shareRepository.findByShareNo(shareNo);
  }

  public List<ShareEntity> retrieveByEmail(final String email) {
    return shareRepository.findByMemberEntity_Email(email);
  }

  public List<ShareEntity> retrieveByCalNo(final Long calNo) {
    return shareRepository.findByCalendarEntity_CalNo(calNo);
  }

  /**
   * methodName : update
   * comment : 공유 권한 수정
   * author : 박현민
   * date : 2023-06-07
   * description :
   *
   * @param shareEntity
   * @return shareNo
   * 
   */
  public List<ShareEntity> update(final ShareEntity shareEntity) {
    final List<ShareEntity> originalList = shareRepository.findByShareNo(shareEntity.getShareNo());
    // 등급을 업데이트하고 저장
    if (!originalList.isEmpty()) {
      ShareEntity original = originalList.get(0);
      original.setGrade(shareEntity.getGrade());
      original.setUpdateDate(new Date());
      original.setChecked(shareEntity.isChecked());

      shareRepository.save(original);
    }

    // 업데이트한 등급으로 반환
    return shareRepository.findByShareNo(shareEntity.getShareNo());
  }

  /**
   * methodName : update
   * comment : 공유 권한 삭제
   * author : 박현민
   * date : 2023-06-08
   * description :
   *
   * @param shareEntity
   * @return shareNo
   * 
   */
  public List<ShareEntity> delete(ShareEntity shareEntity) {
    // Long calNo = shareEntity.getCalendarEntity().getCalNo(); // 구독했던 캘린더 번호 가져오기
    List<ShareEntity> shareEntities = shareRepository.findByShareNo(shareEntity.getShareNo());
    Long calNo = shareEntities.get(0).getCalendarEntity().getCalNo();

    List<ShareEntity> entities = shareRepository.findByCalendarEntity_CalNo(calNo); // 캘린더 번호를 통하여 구독자 목록 가져오기
    List<ScheduleEntity> scheduleEntities = scheduleRepository.findByCalendarEntity_CalNo(calNo); // 캘린더 번호를 통하여 일정가져오기

    if (entities.size() == 1) { // 구독자가 한명이면(나만)
      if (scheduleEntities.size() > 0) { // 일정이 하나 이상 존재하면
        // 모든 일정 탐색
        // 모든 일정 삭제
        scheduleRepository.deleteAll(scheduleEntities);
      }
      shareRepository.delete(shareEntity); // 구독 취소
      if(calNo != 98L && calNo != 90L) {
        calendarService.delete(calendarRepository.findByCalNo(calNo).get(0)); // 캘린더 삭제
      }
    }
    shareRepository.delete(shareEntity); // 구독자가 여러명일 경우 구독취소만 됨

    return shareRepository.findByShareNo(shareEntity.getShareNo());
  }

//  public void deleteList(MemberEntity memberEntity){
//    log.warn("구독취소");
//    shareRepository.deleteAllByMemberEntity_Email(memberEntity.getEmail());
//  }


}