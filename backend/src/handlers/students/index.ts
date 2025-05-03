import { Request, RequestHandler, Response } from 'express-serve-static-core';
import { mapSheets, fetchStudentByID } from '../../helpers/sheets';

// Get all students
export const getStudents: RequestHandler = async (request: Request, response: Response) => {
    try {
        const { students, teachers } = await mapSheets(response);
        response.json({ students, teachers });
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
        const token = request.query.token as string;
        if (!token) response.status(400).json({ error: 'Token required' });
        const student = await fetchStudentByID(token, response);
        response.json(student);
    } catch (error: any) {
        response.status(500).json({
            error: error.message,
            stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
        });
    }
};
