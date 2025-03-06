package com.example.LocalFit.facility.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


/**
 * INFO-000	정상 처리되었습니다<br>
 * ERROR-300	필수 값이 누락되어 있습니다.<br>
 * 요청인자를 참고 하십시오.<br>
 * INFO-100	인증키가 유효하지 않습니다.<br>
 * 인증키가 없는 경우, 열린 데이터 광장 홈페이지에서 인증키를 신청하십시오.<br>
 * ERROR-301	파일타입 값이 누락 혹은 유효하지 않습니다.<br>
 * 요청인자 중 TYPE을 확인하십시오.<br>
 * ERROR-310	해당하는 서비스를 찾을 수 없습니다.<br>
 * 요청인자 중 SERVICE를 확인하십시오.<br>
 * ERROR-331	요청시작위치 값을 확인하십시오.<br>
 * 요청인자 중 START_INDEX를 확인하십시오.<br>
 * ERROR-332	요청종료위치 값을 확인하십시오.<br>
 * 요청인자 중 END_INDEX를 확인하십시오.<br>
 * ERROR-333	요청위치 값의 타입이 유효하지 않습니다.<br>
 * 요청위치 값은 정수를 입력하세요.<br>
 * ERROR-334	요청종료위치 보다 요청시작위치가 더 큽니다.<br>
 * 요청시작조회건수는 정수를 입력하세요.<br>
 * ERROR-335	샘플데이터(샘플키:sample) 는 한번에 최대 5건을 넘을 수 없습니다.<br>
 * 요청시작위치와 요청종료위치 값은 1 ~ 5 사이만 가능합니다.<br>
 * ERROR-336	데이터요청은 한번에 최대 1000건을 넘을 수 없습니다.<br>
 * 요청종료위치에서 요청시작위치를 뺀 값이 1000을 넘지 않도록 수정하세요.<br>
 * ERROR-500	서버 오류입니다.<br>
 * 지속적으로 발생시 열린 데이터 광장으로 문의(Q&A) 바랍니다.<br>
 * ERROR-600	데이터베이스 연결 오류입니다.<br>
 * 지속적으로 발생시 열린 데이터 광장으로 문의(Q&A) 바랍니다.<br>
 * ERROR-601	SQL 문장 오류 입니다.<br>
 * 지속적으로 발생시 열린 데이터 광장으로 문의(Q&A) 바랍니다.<br>
 * INFO-200	해당하는 데이터가 없습니다.<br>
 */

@Data
public class Result {
    @JsonProperty("CODE")
    private String code;  //요청결과 코드

    @JsonProperty("MESSAGE")
    private String message;  //요청결과 메시지
}