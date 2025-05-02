import { Request, RequestHandler, Response } from 'express-serve-static-core';
import { addBearBrag } from '../../helpers/sheets';
import { BodyType } from '../../types';

// Submit form to create a new BehaviorLog row
export const submitForm: RequestHandler<{}, any, BodyType> = async (request: Request<{}, any, BodyType>, response: Response) => {
    try {
        const { studentID, teacherID, grade, behaviors, notes = '' } = request.body;
        if (!studentID || !teacherID || !grade || !behaviors) {
            response.status(400).json({ error: 'Invalid request format' });
            return;
        }
        const result = await addBearBrag(studentID, teacherID, grade, behaviors, notes);
        response.status(201).json({ success: result });
    } catch (error: any) {
        response.status(500).json({ error: error.message });
    }
}
