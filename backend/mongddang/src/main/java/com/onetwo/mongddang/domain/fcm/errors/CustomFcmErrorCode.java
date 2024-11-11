package com.onetwo.mongddang.domain.fcm.errors;

import com.onetwo.mongddang.errors.errorcode.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomFcmErrorCode implements ErrorCode {

    FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "N000", "fcm 토큰을 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
