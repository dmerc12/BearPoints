export enum Timeframe {
    WEEK = 'WEEK',
    MONTH = 'MONTH',
    SEMESTER = 'SEMESTER',
    YEAR = 'YEAR'
}

export enum GradeLevel {
    PRE_K = 'PRE_K',
    K = 'K',
    FIRST = 'FIRST',
    SECOND = 'SECOND',
    THIRD = 'THIRD',
    FOURTH = 'FOURTH'
}

export enum Role {
    STUDENT = 'STUDENT',
    TEACHER = 'TEACHER',
    ADMIN = 'ADMIN'
}

export interface PaginatedUsers {
    users: UserDTO[];
    totalPages: number;
    totalUsers: number;
}

export interface UserDTO {
    id: number | null;
    email: string;
    firstName: string;
    lastName: string;
    role: Role;
    teacherId?: number;
    studentId?: number;
}

export interface PaginatedStudents {
    students: Student[];
    totalPages: number;
    totalStudents: number;
}

export interface Student {
    id: number;
    points: number;
    token: string;
    user: UserDTO;
    teacher: Teacher;
    bragLogs?: BragLog[];
}

export interface PaginatedTeachers {
    teachers: Teacher[];
    totalPages: number;
    totalTeachers: number;
}

export interface Teacher {
    id: number;
    grade: GradeLevel;
    user: UserDTO;
    students?: Student[];
    bragLogs?: BragLog[];
}

export interface PaginatedBehaviorTypes {
    behaviorTypes: BehaviorType[];
    totalPages: number;
    totalBehaviorTypes: number;
}

export interface BehaviorType {
    id: number;
    name: string;
    pointValue: number;
    active: boolean;
}

export interface PaginatedBragLogs {
    bragLogs: BragLog[];
    totalPages: number;
    totalBragLogs: number;
}

export interface BragLog {
    id: number;
    student: Student;
    teacher: Teacher;
    behaviors: BehaviorType[];
    pointsGenerated: number;
    notes?: string;
    timestamp: string;
}

export interface PaginatedRewardItems {
    rewardItems: RewardItem[];
    totalPages: number;
    totalRewardItems: number;
}

export interface RewardItem {
    id: number;
    name: string;
    pointCost: number;
    stock: number;
}

export interface PaginatedStudentRewards {
    studentRewards: StudentReward[];
    totalPages: number;
    totalStudentRewards: number;
}

export interface StudentReward {
    id: number;
    redeemedAt: string;
    student: Student;
    rewardItem: RewardItem;
}

export interface BragLogRequest {
    studentId: number;
    teacherId: number;
    behaviorIds: number[];
    notes?: string;
}

export interface LeaderboardEntry {
    points: number;
    rank?: number;
    student: Person;
    teacher: Person;
    grade: string;
}

interface Person {
    id: number;
    firstName: string;
    lastName: string;
}
