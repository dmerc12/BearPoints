package com.bearpoints.api.config;

import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
@Profile("!prod")
public class TestDataInitializer implements CommandLineRunner {
    private final UserDAO userDAO;
    private final String testTeacherEmail;

    public TestDataInitializer(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.testTeacherEmail = System.getenv("TEST_TEACHER_EMAIL");
    }

    @Override
    public void run(String... args) {
        if (testTeacherEmail == null || testTeacherEmail.isBlank()) {
            log.warn("TEST_TEACHER_EMAIL is not set. Skipping test teacher creation.");
            return;
        }
        if (userDAO.findByEmail(testTeacherEmail).isPresent()) {
            log.info("Test teacher already exists for email: {}", testTeacherEmail);
            return;
        }
        try {
            Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                    "system", null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(adminAuth);
            createTestTeacher();
            log.info("Created test teacher with email: {}", testTeacherEmail);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void createTestTeacher() {
        User user = new User();
        user.setEmail(testTeacherEmail);
        user.setFirstName("Test");
        user.setLastName("Teacher");
        user.setRole(Role.TEACHER);
        Teacher teacher = new Teacher();
        teacher.setGrade(GradeLevel.SECOND);
        teacher.setUser(user);
        user.setTeacher(teacher);
        userDAO.save(user);
    }
}
