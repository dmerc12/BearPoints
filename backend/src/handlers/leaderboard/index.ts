import { Request, RequestHandler, Response } from 'express-serve-static-core';
import { SheetsHelper } from '../../helpers/sheets';

// Get leaderboard
export const getLeaderboard: RequestHandler = async (request: Request, response: Response) => {
    try {
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
        response.json({ behaviorLogs, students });
    } catch (error: any) {
        response.status(500).json({ error: error.message });
    }
}
