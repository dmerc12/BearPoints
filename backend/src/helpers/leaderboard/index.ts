import { BehaviorLog, Student, Timeframe, LeaderboardEntry, StudentsMap } from '../../types';

export const filterLogsByTimeframe = ( logs: BehaviorLog[], timeframe: Timeframe ): BehaviorLog[] => {
    const now = new Date();
    const startDates: Record<Timeframe, Date> = {
        week: new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000),
        month: new Date( now.getFullYear(), now.getMonth(), 1 ),
        semester: new Date( now.getMonth() < 6 ? now.getFullYear() : now.getFullYear(), now.getMonth() < 6 ? 0 : 6, 1 ),
        year: new Date(now.getFullYear(), 0, 1),
        custom: new Date(now.getTime() + 1)
    };
    return logs.filter( log => new Date( log.timestamp ) >= startDates[ timeframe ] );
};

export const calculateLeaderboard = ( logs: BehaviorLog[], students: Student[], teacherFilter?: string ): LeaderboardEntry[] => {
    // Create student map
    const studentMap: Record<number, StudentsMap> = students.reduce( ( acc, row ) => {
        acc[ row.studentID ] = {
            name: row.name,
            teacher: row.teacher
        };
        return acc;
    }, {} as Record<number, StudentsMap>);
    // Sum points by student ID
    const pointsMap: Record<number, number> = logs.reduce( ( acc, log ) => {
        const studentId = log.studentID;
        if ( !studentMap[ studentId ] ) return acc;
        acc[ studentId ] = ( acc[ studentId ] || 0 ) + log.points;
        return acc;
    }, {} as Record<number, number>);
    // convert to array and filter by teacher
    return Object.entries(pointsMap)
        .map(([ studentId, points ]) => ({
            studentID: parseInt(studentId, 10),
            name: studentMap[ parseInt(studentId, 10) ].name,
            teacher: studentMap[ parseInt(studentId, 10)].teacher,
            points
        }))
        .filter(entry => !teacherFilter || entry.teacher === teacherFilter)
        .sort( ( a, b ) => b.points - a.points );
};
