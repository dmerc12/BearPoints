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

/**
 * Configures Firebase services for the application.
 * <p>This configuration:
 * <ul>
 *     <li>Initializes Firebase using credentials from application properties</li>
 *     <li>Provides {@link FirebaseAuth} bean for authentication services</li>
 *     <li>Validates credentials during initialization</li>
 * </ul>
 *
 * <p>Firebase credentials are provided via {@code firebase.credentials.json} property
 * containing the service account JSON. The configuration ensures credentials:
 * <ul>
 *     <li>Are not empty or null</li>
 *     <li>Are properly formatted JSON</li>
 *     <li>Successfully initialize Firebase services</li>
 * </ul>
 *
 * @see FirebaseApp
 * @see FirebaseAuth
 * @version 1.0
 * @author Dylan Mercer
 */
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
        if (FirebaseApp.getApps().isEmpty()) {
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
}
