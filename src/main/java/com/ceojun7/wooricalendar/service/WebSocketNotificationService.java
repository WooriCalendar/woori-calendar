package com.ceojun7.wooricalendar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendMessageToClient(String notificationEntity) {
        // 클라이언트에게 메시지 전송
        messagingTemplate.convertAndSend("/socket/chatt", notificationEntity);
    }
}