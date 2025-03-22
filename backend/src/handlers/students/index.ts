import { Request, RequestHandler, Response } from 'express-serve-static-core';
import { SheetsHelper } from '../../helpers/sheets';
import { Student } from '../../types';

// Get all students
export const getStudents: RequestHandler = async (request: Request, response: Response) => {
    try {
        const doc = SheetsHelper();
        await doc.loadInfo();
        console.log('Available sheets:', doc.sheetsByTitle);
        const sheet = doc.sheetsByTitle[ 'Students' ];
        if (!sheet) {
            console.error('Students sheet not found');
            response.status(404).json({ error: 'Students sheet not found' });
        }
        const rows = await sheet.getRows();
        console.log('Raw rows from sheets:', rows);
        const students: Student[] = rows.map(row => ({
            studentID: Number(row.get('studentID')),
            name: String(row.get('name')),
            grade: String(row.get('grade')),
            teacher: String(row.get('teacher')),
        }));
        console.log('Processed students:', students);
        response.json(students);
    } catch (error: any) {
        console.error('Full error:', error);
        response.status(500).json({
            error: error.message,
            stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
        });
    }
};
