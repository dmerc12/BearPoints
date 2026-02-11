package com.bearpoints.api.config;

import com.bearpoints.api.dao.*;
import com.bearpoints.api.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Initializes test data for non-production environments.
 * <p>Creates a predefined test teacher account during application startup when:
 * <ul>
 *     <li>Not running in production profile</li>
 *     <li>TEST_EMAIL environment variable is set</li>
 *     <li>No existing user with the specified email exists</li>
 * </ul>
 *
 * <p>The test ecosystem includes:
 * <ul>
 *     <li>Administrator accounts with varying permissions</li>
 *     <li>Teacher accounts with random grade level assignments</li>
 *     <li>Student accounts distributed across teachers</li>
 *     <li>Behavior types with mixed active/inactive status</li>
 *     <li>Brag logs with randomized behavior combinations</li>
 *     <li>Reward items with varying point costs and stock levels</li>
 *     <li>Student reward assignments</li>
 * </ul>
 *
 * <p>Execution flow:
 * <ol>
 *     <li>Checks if TEST_EMAIL environment variable is set</li>
 *     <li>Verifies no existing user with specified email</li>
 *     <li>Establishes temporary admin security context</li>
 *     <li>Creates primary test administrator account</li>
 *     <li>Generates additional test administrators</li>
 *     <li>Creates teachers with randomized grade levels</li>
 *     <li>Distributes students across created teachers</li>
 *     <li>Initializes behavior type catalog</li>
 *     <li>Generates brag logs with behavior combinations</li>
 *     <li>Creates reward item inventory</li>
 *     <li>Assigns rewards to students</li>
 *     <li>Cleans up security context</li>
 * </ol>
 *
 * <p>Configuration constants control data volume:
 * <ul>
 *     <li>NUM_TEST_ADMINS_TO_CREATE: Number of additional admin accounts (default: 12)</li>
 *     <li>NUM_TEST_TEACHERS_TO_CREATE: Number of teacher accounts (default: 25)</li>
 *     <li>MIN/MAX_NUM_TEST_STUDENTS_PER_TEACHER: Student distribution range (default: 20-30)</li>
 *     <li>NUM_TEST_BRAG_LOGS_TO_CREATE: Brag log entries (default: 200)</li>
 *     <li>NUM_TEST_REWARD_ITEMS_TO_CREATE: Reward catalog size (default: 20)</li>
 *     <li>NUM_TEST_STUDENT_REWARDS_TO_CREATE: Reward assignments (default: 50)</li>
 * </ul>
 *
 * @see CommandLineRunner
 * @version 2.1
 * @author Dylan Mercer
 */
@Component
@Slf4j
@Profile("!prod")
public class TestDataInitializer implements CommandLineRunner {
    private final UserDAO userDAO;
    private final BehaviorTypeDAO behaviorTypeDAO;
    private final BragLogDAO bragLogDAO;
    private final RewardItemDAO rewardItemDAO;
    private final StudentRewardDAO studentRewardDAO;
    private final String testEmail;
    private final Integer NUM_TEST_ADMINS_TO_CREATE;
    private final Integer NUM_TEST_TEACHERS_TO_CREATE;
    private final Integer MIN_NUM_TEST_STUDENTS_PER_TEACHER;
    private final Integer MAX_NUM_TEST_STUDENTS_PER_TEACHER;
    private final Integer NUM_TEST_BRAG_LOGS_TO_CREATE;
    private final Integer NUM_TEST_REWARD_ITEMS_TO_CREATE;
    private final Integer NUM_TEST_STUDENT_REWARDS_TO_CREATE;
    private final List<BehaviorType> createdBehaviorTypes = new ArrayList<>();

    public TestDataInitializer(UserDAO userDAO, BehaviorTypeDAO behaviorTypeDAO, BragLogDAO bragLogDAO,
                               RewardItemDAO rewardItemDAO, StudentRewardDAO studentRewardDAO) {
        this.userDAO = userDAO;
        this.behaviorTypeDAO = behaviorTypeDAO;
        this.bragLogDAO = bragLogDAO;
        this.rewardItemDAO = rewardItemDAO;
        this.studentRewardDAO = studentRewardDAO;
        this.testEmail = System.getenv("TEST_EMAIL");
        NUM_TEST_ADMINS_TO_CREATE = 12;
        NUM_TEST_TEACHERS_TO_CREATE = 25;
        MIN_NUM_TEST_STUDENTS_PER_TEACHER = 20;
        MAX_NUM_TEST_STUDENTS_PER_TEACHER = 30;
        NUM_TEST_BRAG_LOGS_TO_CREATE = 200;
        NUM_TEST_REWARD_ITEMS_TO_CREATE = 20;
        NUM_TEST_STUDENT_REWARDS_TO_CREATE = 50;
    }

    @Override
    public void run(String... args) {
        if (testEmail == null || testEmail.isBlank()) {
            log.warn("TEST_EMAIL is not set. Skipping test user creation.");
            return;
        }
        if (userDAO.findByEmail(testEmail).isPresent()) {
            log.info("Test user already exists for email: {}", testEmail);
            return;
        }
        try {
            // Set admin role to allow permission to create test data
            Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                    "system", null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(adminAuth);
            // Create test user
            String[] emailParts = testEmail.split("@");
            String firstInitial = emailParts[0].substring(0, 1);
            String testUserLastName = emailParts[0].substring(2);
            createTestAdmin(testEmail, firstInitial, testUserLastName);
            log.info("Created test user");
            // Create test data
            // Create test administrators
            createTestAdmins();
            // Create test teachers

            List<Teacher> createdTeachers = createTestTeachers();
            // Create test students
            List<Student> createdStudents = createTestStudentsForTeachers(createdTeachers);
            // Create test behavior types
            createTestBehaviorTypes();
            // Create test brag logs
            createTestBragLogs(createdStudents);
            // Create test reward items
            List<RewardItem> createdRewardItems = createTestRewardItems();
            // Create test student rewards
            createTestStudentRewards(createdStudents, createdRewardItems);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void createTestAdmin(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.ADMIN);
        User admin = userDAO.save(user);
        log.info("Created test administrator: {}", admin);
    }

    private void createTestAdmins() {
        log.info("Starting creating test admins with number to create:  {}", NUM_TEST_ADMINS_TO_CREATE);
        for (int i = 0; i < NUM_TEST_ADMINS_TO_CREATE; i++) {
            createTestAdmin("admin" + i + "@okcps.org", "admin", "admin" + i);
        }
        log.info("Finished creating test admins");
    }

    private Teacher createTestTeacher(String email, String firstName, String lastName, GradeLevel gradeLevel) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.TEACHER);
        Teacher teacher = new Teacher();
        teacher.setGrade(gradeLevel);
        teacher.setUser(user);
        user.setTeacher(teacher);
        User teacherUser = userDAO.save(user);
        log.info("Created test teacher: {}", teacherUser.getTeacher());
        return teacherUser.getTeacher();
    }

    private List<Teacher> createTestTeachers() {
        log.info("Starting creating test teachers with number to create:  {}", NUM_TEST_TEACHERS_TO_CREATE);
        Random rand = new Random();
        GradeLevel[] gradeLevels = GradeLevel.values();
        List<Teacher> teachers = new ArrayList<>();
        for (int i = 0; i < NUM_TEST_TEACHERS_TO_CREATE; i++) {
            GradeLevel randomGrade = gradeLevels[rand.nextInt(gradeLevels.length)];
            Teacher teacher = createTestTeacher("teacher" + i + "@okcps.org", "TeacherFirstName" + i, "TeacherLastName" + i, randomGrade);
            teachers.add(teacher);
        }
        log.info("Finished creating test teachers");
        return teachers;
    }

    private Student createTestStudent(String email, String firstName, String lastName, Teacher teacher) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.STUDENT);
        Student student = new Student();
        student.setTeacher(teacher);
        student.setUser(user);
        student.setPoints(100);
        student.generateToken();
        user.setStudent(student);
        User studentUser = userDAO.save(user);
        log.info("Created test student: {}", studentUser.getStudent());
        return studentUser.getStudent();
    }

    private List<Student> createTestStudentsForTeachers(List<Teacher> teachers) {
        int min = MIN_NUM_TEST_STUDENTS_PER_TEACHER;
        int max = MAX_NUM_TEST_STUDENTS_PER_TEACHER;
        log.info("Starting creating test students for {} teachers with {}-{} students per teacher", teachers.size(), min, max);
        Random rand = new Random();
        List<Student> allStudents = new ArrayList<>();
        int studentCounter = 0;
        for (Teacher teacher : teachers) {
            int numStudentsForTeacher = min + rand.nextInt(
                    max - min + 1);
            for (int i = 0; i < numStudentsForTeacher; i++) {
                Student student = createTestStudent(
                        "student" + studentCounter + "@okcps.org",
                        "StudentFirstName" + studentCounter,
                        "StudentLastName" + studentCounter,
                        teacher
                );
                allStudents.add(student);
                studentCounter++;
            }
            log.info("Created {} students for teacher: {}", numStudentsForTeacher, teacher.getUser().getEmail());
        }
        log.info("Finished creating {} test students", allStudents.size());
        return allStudents;
    }

    private BehaviorType createTestBehaviorType(String name, Integer pointValue, Boolean active) {
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setName(name);
        behaviorType.setPointValue(pointValue);
        behaviorType.setActive(active);
        log.info("Created test behavior type: {}", behaviorType);
        BehaviorType behavior = behaviorTypeDAO.save(behaviorType);
        log.info("Created test behavior: {}", behavior);
        return behavior;
    }

    private void createTestBehaviorTypes() {
        log.info("Starting creating test behavior types");
        createdBehaviorTypes.add(createTestBehaviorType("Brilliant", 1, true));
        createdBehaviorTypes.add(createTestBehaviorType("Excelled", 1, true));
        createdBehaviorTypes.add(createTestBehaviorType("Answered", 3, true));
        createdBehaviorTypes.add(createTestBehaviorType("Read", 2, true));
        createdBehaviorTypes.add(createTestBehaviorType("Participated", 2, true));
        createdBehaviorTypes.add(createTestBehaviorType("Behaved", 3, false));
        createdBehaviorTypes.add(createTestBehaviorType("Quiet", 2, false));
        createdBehaviorTypes.add(createTestBehaviorType("Cleaned Up", 4, false));
        createdBehaviorTypes.add(createTestBehaviorType("Helped Others", 5, false));
        log.info("Finishing creating {} behavior types", createdBehaviorTypes.size());
    }

    private void createTestBragLog(Student student, Teacher teacher, Set<BehaviorType> behaviors,
                                   Integer pointsGenerated, String notes) {
        BragLog bragLog = new BragLog();
        bragLog.setStudent(student);
        bragLog.setTeacher(teacher);
        bragLog.setBehaviors(behaviors);
        bragLog.setPointsGenerated(pointsGenerated);
        bragLog.setNotes(notes);
        bragLog.setSubmitterName("John Doe");
        BragLog brag = bragLogDAO.save(bragLog);
        log.info("Created test brag log: {}", brag);
    }

    private void createTestBragLogs(List<Student> students) {
        log.info("Starting creating {} test brag logs", NUM_TEST_BRAG_LOGS_TO_CREATE);
        if (students.isEmpty() || createdBehaviorTypes.isEmpty()) {
            log.warn("Cannot create brag logs - no students or behavior types available");
            return;
        }
        Random rand = new Random();
        List<BehaviorType> activeBehaviorTypes = createdBehaviorTypes.stream().filter(BehaviorType::getActive).toList();
        for (int i = 0; i < NUM_TEST_BRAG_LOGS_TO_CREATE; i++) {
            Student randomStudent = students.get(rand.nextInt(students.size()));
            Teacher teacher = randomStudent.getTeacher();
            Set<BehaviorType> behaviors = new HashSet<>();
            int numBehaviors = 1 + rand.nextInt(5);
            for (int j = 0; j < numBehaviors && !activeBehaviorTypes.isEmpty(); j++) {
                BehaviorType randomBehavior = activeBehaviorTypes.get(rand.nextInt(activeBehaviorTypes.size()));
                behaviors.add(randomBehavior);
            }
            Integer pointsGenerated = behaviors.stream().mapToInt(BehaviorType::getPointValue).sum();
            String notes = "Test brag log #" + i + " - " + behaviors.stream().map(BehaviorType::getName)
                    .collect(Collectors.joining(", "));
            createTestBragLog(randomStudent, teacher, behaviors, pointsGenerated, notes);
        }
        log.info("Finished creating {} brag logs",  NUM_TEST_BRAG_LOGS_TO_CREATE);
    }

    private RewardItem createTestRewardItem(String name, Integer pointCost, Integer stock) {
        RewardItem rewardItem = new RewardItem();
        rewardItem.setName(name);
        rewardItem.setPointCost(pointCost);
        rewardItem.setStock(stock);
        RewardItem item = rewardItemDAO.save(rewardItem);
        log.info("Created test reward item: {}", item);
        return item;
    }

    private List<RewardItem> createTestRewardItems() {
        String[] rewardNames = {
                "Homework Pass", "Extra Recess", "Sticker Sheet", "Pencil Set", "Bookmark",
                "Small Toy", "Certificate", "Positive Note Home", "Line Leader", "Tech Time",
                "Lunch with Teacher", "Class Game", "Art Supplies", "Science Kit", "Reading Buddy"
        };
        int itemsToCreate =Math.min(NUM_TEST_REWARD_ITEMS_TO_CREATE, rewardNames.length);
        log.info("Starting creating {} test reward items", itemsToCreate);
        List<RewardItem> rewardItems = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < itemsToCreate; i++) {
            int pointCost = 5 + rand.nextInt(46);
            int stock = 1 + rand.nextInt(20);
            rewardItems.add(createTestRewardItem(rewardNames[i], pointCost, stock));
        }
        log.info("Finished creating {} reward items", itemsToCreate);
        return rewardItems;
    }

    private void createTestStudentReward(Student student, RewardItem rewardItem) {
        StudentReward studentReward = new StudentReward();
        studentReward.setStudent(student);
        studentReward.setRewardItem(rewardItem);
        StudentReward reward = studentRewardDAO.save(studentReward);
        log.info("Created test student reward: {}", reward);
    }

    private void createTestStudentRewards(List<Student> students, List<RewardItem> rewardItems) {
        log.info("Starting creating {} test student rewards", NUM_TEST_STUDENT_REWARDS_TO_CREATE);
        if (rewardItems.isEmpty() || students.isEmpty()) {
            log.warn("Cannot create student rewards - no reward items or students available");
            return;
        }
        Random rand = new Random();
        for (int i = 0; i < NUM_TEST_STUDENT_REWARDS_TO_CREATE; i++) {
            Student randomStudent = students.get(rand.nextInt(students.size()));
            RewardItem randomReward = rewardItems.get(rand.nextInt(rewardItems.size()));
            createTestStudentReward(randomStudent, randomReward);
        }
        log.info("Finished creating {} student rewards", NUM_TEST_STUDENT_REWARDS_TO_CREATE);
    }
}
