CREATE TABLE user (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE CHECK (email LIKE '%@okcps.org'),
    name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE teacher (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES user(id) ON DELETE CASCADE,
    grade VARCHAR(10) NOT NULL
    last_synced TIMESTAMP
);

CREATE TABLE student (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES user(id) ON DELETE CASCADE,
    teacher_id INT NOT NULL REFERENCES teacher(id) ON DELETE CASCADE,
    token UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    points INT NOT NULL DEFAULT 0,
    last_synced TIMESTAMP
);

CREATE TABLE brag_log (
    id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    teacher_id INT NOT NULL REFERENCES teacher(id) ON DELETE CASCADE,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name VARCHAR(100) NOT NULL,
    points_generated INT NOT NULL,
    notes TEXT,
    synced_to_sheets BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE behavior_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    point_value INT NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE behavior_log (
    brag_log_id INT REFERENCES brag_log(id) ON DELETE CASCADE,
    behavior_type_id INT REFERENCES behavior_log(id) ON DELETE CASCADE,
    PRIMARY KEY (brag_log_id, behavior_type_id)
);

CREATE INDEX idx_brag_log_student ON brag_log(student_id);

CREATE INDEX idx_behavior_log_composite ON behavior_log(brag_log_id, behavior_type_id);
