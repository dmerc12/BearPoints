package com.bearpoints.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {
    @Value("${firebase.credentials.json}")
    private String firebaseConfigJson;

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @PostConstruct
    public void initialize() throws IOException {
        if (!StringUtils.hasText(firebaseConfigJson)) {
            throw new IllegalArgumentException("Firebase credentials JSON is empty");
        }
        InputStream serviceAccount = new ByteArrayInputStream(
                firebaseConfigJson.getBytes(StandardCharsets.UTF_8)
        );
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        FirebaseApp.initializeApp(options);
    }
}
