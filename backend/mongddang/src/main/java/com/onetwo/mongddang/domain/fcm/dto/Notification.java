package com.onetwo.mongddang.domain.fcm.dto;

import com.onetwo.mongddang.domain.user.model.User;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private User receiver;   // 수신자의 ID 또는 알림 대상
    private User child; // 알림 대상자의 닉네임(이상혈당 지속시)
    private String message;    // 알림 내용
    private String title;      // 알림 제목

}