import { BehaviorLog, Student, Timeframe, LeaderboardEntry } from '../services/types';

export const getProcessedLeaderboard = (
    rawData: { behaviorLogs: BehaviorLog[]; students: Student[]; },
    filters: { timeframe: Timeframe; teacher?: string; }
): LeaderboardEntry[] => {
    const filteredLogs = filterLogsByTimeframe(rawData.behaviorLogs, filters.timeframe);
    return calculateLeaderboard(filteredLogs, rawData.students, filters.teacher);
};

export const filterLogsByTimeframe = ( logs: BehaviorLog[], timeframe: Timeframe ): BehaviorLog[] => {
    const now = new Date();
    const startDates: Record<Timeframe, Date> = {
        week: new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000),
        month: new Date( now.getFullYear(), now.getMonth() - 1, now.getDate() ),
        semester: new Date( now.getMonth() < 6 ? now.getFullYear() : now.getFullYear(), now.getMonth() < 6 ? 0 : 6, 1 ),
        year: new Date(now.getFullYear(), 0, 1),
    };
    return logs.filter( log => new Date( log.timestamp ) >= startDates[ timeframe ] );
};

const calculateLeaderboard = (
    logs: BehaviorLog[],
    students: Student[],
    teacher?: string
): LeaderboardEntry[] => {
    const studentMap = new Map(students.map(student => [
        student.studentID,
        {
            name: student.name,
            teacher: student.teacher,
            grade: student.grade
        }
    ]));

    return Array.from(
        logs.reduce((acc, log) => {
            const student = studentMap.get(log.studentID);
            if (student && (!teacher || student.teacher === teacher)) {
                acc.set(log.studentID, (acc.get(log.studentID) || 0) + log.points);
            }
            return acc;
        }, new Map<number, number>())
            .entries()
    ).map(([ studentID, points ]: [ number, number ]) => ({
        studentID,
        points,
        name: studentMap.get(studentID)!.name,
        teacher: studentMap.get(studentID)!.teacher,
        grade: studentMap.get(studentID)!.grade
    }))
        .sort((a: LeaderboardEntry, b: LeaderboardEntry) => b.points - a.points);
};

export const getUniqueTeachers = (entries: LeaderboardEntry[]): string[] => {
    return Array.from(new Set(entries.map(entry => entry.teacher))).filter(Boolean);
};