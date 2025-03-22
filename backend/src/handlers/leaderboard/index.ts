import { filterLogsByTimeframe, calculateLeaderboard } from '../../helpers/leaderboard';
import { Request, RequestHandler, Response } from 'express-serve-static-core';
import { LeaderboardEntry, Timeframe } from '../../types';
import { SheetsHelper } from '../../helpers/sheets';

type LeaderboardQuery = {
    timeframe?: Timeframe | string;
    teacher?: string;
};

// Get leaderboard
export const getLeaderboard: RequestHandler<{}, LeaderboardEntry[] | { error: string }, unknown, LeaderboardQuery> = async (request: Request, response: Response) => {
    try {
        // Query filters
        const rawTimeframe = request.query.timeframe;
        const teacher = typeof request.query.teacher === 'string' ? request.query.teacher : undefined;
        // Validate timeframe
        const isValidTimeframe = (value: string): value is Timeframe => {
            return [ 'week', 'month', 'semester', 'year', 'custom' ].includes(value);
        };
        if (rawTimeframe && (typeof rawTimeframe !== 'string' || !isValidTimeframe(rawTimeframe))) {
            response.status(400).json({ error: 'Invalid timeframe parameter' });
            return;
        }
        const timeframe: Timeframe = rawTimeframe && isValidTimeframe(rawTimeframe) ? rawTimeframe : 'week';
        // Get all behavior logs and students
        const doc = SheetsHelper();
        await doc.loadInfo();
        const behaviorLogSheet = doc.sheetsByTitle[ 'BehaviorLog' ];
        const studentsSheet = doc.sheetsByTitle[ 'Students' ];
        if (!behaviorLogSheet || !studentsSheet) {
            throw new Error('Required sheets not found');
        }
        const [ behaviorLogRows, studentRows ] = await Promise.all([
            behaviorLogSheet.getRows(),
            studentsSheet.getRows()
        ]);
        const behaviorLogs = behaviorLogRows.map(row => ({
            timestamp: row.get('timestamp'),
            studentID: Number(row.get('studentID')),
            respectful: row.get('respectful') === 'TRUE',
            responsible: row.get('responsible') === 'TRUE',
            safe: row.get('safe') === 'TRUE',
            points: Number(row.get('points')),
            notes: row.get('notes') || undefined
        }));
        const students = studentRows.map(row => ({
            studentID: Number(row.get('studentID')),
            name: row.get('name'),
            grade: row.get('grade'),
            teacher: row.get('teacher')
        }));
        // Process data
        const filteredLogs = filterLogsByTimeframe(behaviorLogs, timeframe);
        // Sum points per student
        const leaderboard = calculateLeaderboard(filteredLogs, students, teacher);
        response.json(leaderboard);
    } catch (error: any) {
        response.status(500).json({ error: error.message });
    }
}
