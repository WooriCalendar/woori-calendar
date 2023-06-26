package com.ceojun7.wooricalendar.dto;

import lombok.Data;
import lombok.Getter;

/**
 * @author : DGeon
 * @packageName : com.ceojun7.wooricalendar.dto
 * @fileName : EmailPostDTO
 * @date : 2023-06-12
 * @description :
 *              ===========================================================
 *              DATE AUTHOR NOTE
 *              -----------------------------------------------------------
 *              2023-06-12 DGeon 최초 생성
 **/
@Getter
@Data
public class EmailPostDTO {
    private String email;
    private String language;
}