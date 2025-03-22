import { Request, RequestHandler, Response } from 'express-serve-static-core';
import { SheetsHelper } from '../../helpers/sheets';
import { Student } from '../../types';

// Get all students
export const getStudents: RequestHandler = async (request: Request, response: Response) => {
    try {
        const doc = SheetsHelper();
        await doc.loadInfo();
        const sheet = doc.sheetsByTitle[ 'Students' ];
        if (!sheet) {
            throw new Error('Students sheet not found');
            // TODO: Create students sheet if it doesn't exist
        }
        const rows = await sheet.getRows();
        const students: Student[] = rows.map(row => ({
            studentID: Number(row.get('studentID')),
            name: String(row.get('name')),
            grade: String(row.get('grade')),
            teacher: String(row.get('teacher')),
        }));
        response.json(students);
    } catch (error: any) {
        response.status(500).json({ error: error.message });
    }
}
