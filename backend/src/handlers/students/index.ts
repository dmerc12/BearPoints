import { Request, RequestHandler, Response } from 'express-serve-static-core';
import { SheetsHelper } from '../../helpers/sheets';
import { Student } from '../../types';

// Get all students
export const getStudents: RequestHandler = async (request: Request, response: Response) => {
    try {
        const doc = SheetsHelper();
        await doc.loadInfo();
        const studentsSheet = doc.sheetsByTitle[ 'Students' ]!;
        if (!studentsSheet) {
            console.error('Students sheet not found');
            response.status(404).json({ error: 'Students sheet not found' });
        }
        const behaviorSheet = doc.sheetsByTitle[ 'BehaviorLog' ];
        const pointsMap = new Map<number, number>();
        if (behaviorSheet) {
            const behaviorRows = await behaviorSheet.getRows();
            behaviorRows.forEach(row => {
                const studentID = Number(row.get('studentID'));
                const points = Number(row.get('points')) || 0;
                pointsMap.set(studentID, (pointsMap.get(studentID) || 0) + points);
            });
        }
        const studentRows = await studentsSheet.getRows();
        const students: Student[] = studentRows.map(row => ({
            studentID: Number(row.get('studentID')),
            name: String(row.get('name')),
            grade: String(row.get('grade')),
            teacher: String(row.get('teacher')),
            token: String(row.get('token')),
            points: pointsMap.get(Number(row.get('studentID'))) || 0
        }));
        response.json(students);
    } catch (error: any) {
        console.error('Full error:', error);
        response.status(500).json({
            error: error.message,
            stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
        });
    }
};

// Get student by token
export const getStudentByToken: RequestHandler = async (request: Request, response: Response) => {
    try {
        const token = request.query.token;
        if (!token) response.status(400).json({ error: 'Token required' });
        const doc = SheetsHelper();
        await doc.loadInfo();
        const sheet = doc.sheetsByTitle[ 'Students' ]!;
        const rows = await sheet.getRows();
        const student = rows.find(row => row.get('token') === token)!;
        response.json({
            studentID: Number(student.get('studentID')),
            name: String(student.get('name')),
            token: String(student.get('token'))
        });
    } catch (error: any) {
        response.status(500).json({
            error: error.message,
            stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
        });
    }
};
