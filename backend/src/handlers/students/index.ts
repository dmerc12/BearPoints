import { Request, RequestHandler, Response } from 'express-serve-static-core';
import { SheetsHelper } from '../../helpers/sheets';
import { Student } from '../../types';

// Get all students
export const getStudents: RequestHandler = async (request: Request, response: Response) => {
    try {
        const rows = await SheetsHelper.read('Students', 'A:D');
        const students: Student[] = rows.map((row: any[]) => ({
            studentID: Number(row[ 0 ]),
            name: String(row[ 1 ]),
            grade: String(row[ 2 ]),
            teacher: String(row[ 3 ]),
        }));
        response.json(students);
    } catch (error: any) {
        response.status(500).json({ error: error.message });
    }
}
