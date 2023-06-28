package com.ceojun7.wooricalendar.dto;

import com.ceojun7.wooricalendar.model.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author : DGeon,강태수
 * @packageName : com.ceojun7.wooricalendar.dto
 * @fileName : MemberDTO
 * @date : 2023-06-01
 * @description :
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023-06-01 DGeon 최초 생성
 *              2023-06-04 강태수 birthday 생성
 **/
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MemberDTO {

    private String token;
    private String email;
    private String password;
    private String nickname;
    private String subemail;
    private String language;
    private Date birthday;
    private String auth_Provider;

    public MemberDTO(final MemberEntity memberEntity) {
        if (memberEntity.getPassword() != null) {
            this.email = memberEntity.getEmail();
            this.password = memberEntity.getPassword();
        } else if (memberEntity.getSubemail() != null && memberEntity.getEmail() != null) {
            this.subemail = memberEntity.getSubemail();
            this.email = memberEntity.getEmail();
        } else if (memberEntity.getSubemail() != null) {
            this.subemail = memberEntity.getSubemail();
        }
    }

    public static MemberEntity toEntity(final MemberDTO memberDTO) {
        if (memberDTO.getPassword() != null) {
            return MemberEntity.builder().email(memberDTO.getEmail()).password(memberDTO.getPassword()).build();
        } else {
            return MemberEntity.builder().email(memberDTO.getEmail()).build();
        }
    }
}