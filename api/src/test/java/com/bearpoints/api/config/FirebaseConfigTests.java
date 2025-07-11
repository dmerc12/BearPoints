package com.bearpoints.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FirebaseConfig}.
 * <p>Verifies configuration and initialization of Firebase services:
 * <ul>
 *     <li>Proper initialization with valid credentials</li>
 *     <li>Error handling for invalid or missing credentials</li>
 *     <li>Correct creation of {@link FirebaseAuth} bean</li>
 *     <li>Internal initialization logic using Firebase SDK</li>
 * </ul>
 *
 * <p>Tests validate:
 * <ul>
 *     <li>Initialization succeeds with properly formatted service account JSON</li>
 *     <li>{@link IllegalArgumentException} is thrown for empty credentials</li>
 *     <li>{@link IOException} is thrown for invalid JSON credentials</li>
 *     <li>{@link FirebaseAuth} instance is correctly provided</li>
 *     <li>Credentials are properly parsed and FirebaseApp is initialized</li>
 * </ul>
 *
 * @see FirebaseConfig
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class FirebaseConfigTests {
    private FirebaseConfig firebaseConfig;

    @BeforeEach
    void setUp() {
        firebaseConfig = new FirebaseConfig();
        String validJson = "{" +
                "\"type\": \"service_account\"," +
                "\"project_id\": \"test-project\"," +
                "\"private_key_id\": \"test-key-id\"," +
                "\"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDArV8S+7iX6o3Q\\n2eLhY8uzJfF5z8Uw7q7X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X\\n7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X\\n7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X\\n7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X7X2X\\n7X2X7X2X7X2X7X2XAgMBAAECggEBAK1fEvu4l+qN0Nni4WPLsyXxec/FMO6u1+19\\nl+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19\\nl+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19\\nl+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19\\nl+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19\\nl+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19\\nl+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19\\nl+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19\\nl+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19\\nl+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19\\nl+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19l+19\\n-----END PRIVATE KEY-----\\n\"," +
                "\"client_email\": \"test@test-project.iam.gserviceaccount.com\"," +
                "\"client_id\": \"1234567890\"," +
                "\"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\"," +
                "\"token_uri\": \"https://oauth2.googleapis.com/token\"," +
                "\"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\"," +
                "\"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/test%40test-project.iam.gserviceaccount.com\"" +
                "}";
        ReflectionTestUtils.setField(firebaseConfig, "firebaseConfigJson", validJson);
    }

    @AfterEach
    void cleanup() {
        FirebaseApp.getApps().forEach(FirebaseApp::delete);
    }

    @Test
    @DisplayName("Firebase initialize with valid credentials")
    void initializeWithValidCredentials() {
        try (MockedStatic<GoogleCredentials> credentialsMock = mockStatic(GoogleCredentials.class)) {
            credentialsMock.when(() -> GoogleCredentials.fromStream(any()))
                    .thenReturn(mock(GoogleCredentials.class));
            assertDoesNotThrow(() -> firebaseConfig.initialize());
            assertFalse(FirebaseApp.getApps().isEmpty());
        }
    }

    @Test
    @DisplayName("Exception thrown when initialized with empty credentials")
    void initializeWithEmptyCredentials() {
        ReflectionTestUtils.setField(firebaseConfig, "firebaseConfigJson", "");
        assertThrows(IllegalArgumentException.class, () -> firebaseConfig.initialize());
    }

    @Test
    @DisplayName("Exception thrown when initialized with invalid json")
    void initializeWithInvalidJson() {
        ReflectionTestUtils.setField(firebaseConfig, "firebaseConfigJson", "invalid-json");
        assertThrows(IOException.class, () -> firebaseConfig.initialize());
    }

    @Test
    @DisplayName("Instance returned with FirebaseAuth")
    void firebaseAuth() {
        try (MockedStatic<FirebaseAuth> authMock = mockStatic(FirebaseAuth.class)) {
            FirebaseAuth mockAuth = mock(FirebaseAuth.class);
            authMock.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
            assertEquals(mockAuth, firebaseConfig.firebaseAuth());
        }
    }

    @Test
    @DisplayName("Initialized using correct credentials")
    void initializeWithCorrectCredentials() throws Exception {
        try (MockedStatic<GoogleCredentials> credentialsMock = mockStatic(GoogleCredentials.class);
            MockedStatic<FirebaseOptions> optionsMock = mockStatic(FirebaseOptions.class);
            MockedStatic<FirebaseApp> appMock = mockStatic(FirebaseApp.class)) {
                GoogleCredentials mockCreds = mock(GoogleCredentials.class);
                credentialsMock.when(() -> GoogleCredentials.fromStream(any())).thenReturn(mockCreds);
                FirebaseOptions.Builder mockBuilder = mock(FirebaseOptions.Builder.class);
                when(mockBuilder.setCredentials(any())).thenReturn(mockBuilder);
                FirebaseOptions mockOptions = mock(FirebaseOptions.class);
                when(mockBuilder.build()).thenReturn(mockOptions);
                optionsMock.when(FirebaseOptions::builder).thenReturn(mockBuilder);
                firebaseConfig.initialize();
                credentialsMock.verify(() -> GoogleCredentials.fromStream(any(ByteArrayInputStream.class)));
                appMock.verify(() -> FirebaseApp.initializeApp(mockOptions));
            }
    }
}
