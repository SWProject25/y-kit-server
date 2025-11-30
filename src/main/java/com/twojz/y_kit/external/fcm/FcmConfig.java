package com.twojz.y_kit.external.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FcmConfig {
    @Value("${FIREBASE_CONFIG_PATH}")
    private String firebaseConfigPath;

    @PostConstruct
    public void init() throws IOException {
        try (FileInputStream serviceAccount = new FileInputStream(firebaseConfigPath)) {
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccount);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(googleCredentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase application has been initialized from: {}", firebaseConfigPath);
            }
        } catch (Exception e) {
            log.error("❌ Failed to initialize Firebase: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        log.info("🔔 FirebaseMessaging Bean 생성");
        return FirebaseMessaging.getInstance();
    }
}