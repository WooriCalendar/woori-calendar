package com.ceojun7.wooricalendar.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ceojun7.wooricalendar.model.ShareEntity;
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
  public List<ShareEntity> delete(final ShareEntity shareEntity) {
    shareRepository.delete(shareEntity);

    return shareRepository.findByShareNo(shareEntity.getShareNo());
  }

}