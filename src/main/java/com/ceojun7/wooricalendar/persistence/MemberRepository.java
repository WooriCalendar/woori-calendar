package com.ceojun7.wooricalendar.persistence;

import com.ceojun7.wooricalendar.model.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author : DGeon
 * @packageName :
 * @fileName : MemberRepository
 * @date : 2023-06-01
 * @packageName : com.ceojun7.wooricalendar.persistence
 * @description : =========================================================== DATE           AUTHOR             NOTE ----------------------------------------------------------- 2023-06-01        DGeon             최초 생성
 */
@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    /**
     * methodName : findByEmail
     * comment : 회원 조회
     * author : DGeon
     * date : 2023-06-05
     * description :
     *
     * @param email the email
     * @return member entity
     */
    MemberEntity findByEmail(String email);

    /**
     * methodName : existsByEmail
     * comment : 회원 존재 여부 확인
     * author : DGeon
     * date : 2023-06-05
     * description :
     *
     * @param email the email
     * @return boolean
     */
    boolean existsByEmail(String email);

    /**
     * methodName : findByEmailAndPassword
     * comment : email과 password를 기반으로 DB와 일치하는 회원 조회
     * author : DGeon
     * date : 2023-06-05
     * description :
     *
     * @param email    the email
     * @param password the password
     * @return member entity
     */
    MemberEntity findByEmailAndPassword(String email, String password);

    /**
     * methodName : findBySubEmail
     * comment : submemail로 본 email 찾기
     * author : DGeon
     * date : 2023-06-23
     * description :
     *
     * @param subemail the subemail
     * @return member entity
     */
    MemberEntity findBySubemail(String subemail);

}