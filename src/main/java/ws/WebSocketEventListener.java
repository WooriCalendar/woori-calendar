//package ws;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.event.EventListener;
//import org.springframework.messaging.simp.SimpMessageSendingOperations;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.messaging.SessionConnectedEvent;
//import org.springframework.web.socket.messaging.SessionDisconnectEvent;
//
//@Component
//public class WebSocketEventListener {
//
//  private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
//
//  @Autowired
//  private SimpMessageSendingOperations messagingTemplate;
//
//
//  private static Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//  private static String email = authentication.getName(); // 사용자의 이메일 정보
//
//  @EventListener
//  public void handleWebSocketConnectListener(SessionConnectedEvent event) {
//    logger.info("Received a new web socket connection");
//    ChatMessage chatMessage = new ChatMessage();
//    chatMessage.setRevEmail(email);
//  }
//
//  @EventListener
//  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//
//    // String username = (String) headerAccessor.getSessionAttributes().get("username");
//
//    if (email != null) {
//      logger.info("User Disconnected : {}", email);
//
//      ChatMessage chatMessage = new ChatMessage();
//
//      messagingTemplate.convertAndSend("/topic/public", chatMessage);
//    }else{
//      logger.info("User Disconnected : {}", (Object) null);
//    }
//  }
//}
