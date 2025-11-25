package com.twojz.y_kit.external.fcm;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * FCM 푸시 알림 전송 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmClient {

    /**
     * 단일 토큰에 메시지 전송
     */
    public String sendToToken(String token, FcmMessage fcmMessage) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(fcmMessage.getTitle())
                            .setBody(fcmMessage.getBody())
                            .setImage(fcmMessage.getImage())
                            .build())
                    .putAllData(fcmMessage.getData() != null ? fcmMessage.getData() : Map.of())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 전송 성공 - messageId: {}", response);
            return response;

        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패 - token: {}, error: {}", maskToken(token), e.getMessage());
            throw new RuntimeException("FCM 전송 실패", e);
        }
    }

    /**
     * 여러 토큰에 메시지 전송 (최대 500개)
     */
    public BatchResponse sendToTokens(List<String> tokens, FcmMessage fcmMessage) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("전송할 토큰이 없습니다.");
        }

        if (tokens.size() > 500) {
            throw new IllegalArgumentException(
                    String.format("토큰 개수가 최대 제한(500개)을 초과했습니다: %d개", tokens.size()));
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(fcmMessage.getTitle())
                            .setBody(fcmMessage.getBody())
                            .setImage(fcmMessage.getImage())
                            .build())
                    .putAllData(fcmMessage.getData() != null ? fcmMessage.getData() : Map.of())
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            log.info("FCM 배치 전송 완료 - 성공: {}, 실패: {}",
                    response.getSuccessCount(),
                    response.getFailureCount());

            if (response.getFailureCount() > 0) {
                logFailedTokens(tokens, response);
            }

            return response;

        } catch (FirebaseMessagingException e) {
            log.error("FCM 배치 전송 실패", e);
            throw new RuntimeException("FCM 배치 전송 실패", e);
        }
    }

    /**
     * 토픽에 메시지 전송
     */
    public String sendToTopic(String topic, FcmMessage fcmMessage) {
        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(fcmMessage.getTitle())
                            .setBody(fcmMessage.getBody())
                            .setImage(fcmMessage.getImage())
                            .build())
                    .putAllData(fcmMessage.getData() != null ? fcmMessage.getData() : Map.of())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 토픽 전송 성공 - topic: {}, messageId: {}", topic, response);
            return response;

        } catch (FirebaseMessagingException e) {
            log.error("FCM 토픽 전송 실패 - topic: {}", topic, e);
            throw new RuntimeException("FCM 토픽 전송 실패", e);
        }
    }

    /**
     * 실패한 토큰 로깅
     */
    private void logFailedTokens(List<String> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                FirebaseMessagingException exception = responses.get(i).getException();
                String errorCode = exception != null ? exception.getErrorCode().name() : "UNKNOWN";
                log.warn("FCM 전송 실패 - token: {}, error: {}",
                        maskToken(tokens.get(i)), errorCode);
            }
        }
    }

    /**
     * 토큰 마스킹 (로깅용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
    }
}