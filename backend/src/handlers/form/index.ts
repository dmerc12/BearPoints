import { Request, RequestHandler, Response } from 'express-serve-static-core';
import { SheetsHelper } from '../../helpers/sheets';

type BodyType = {
    studentID: number;
    behaviors: {
        respectful: boolean;
        responsible: boolean;
        safe: boolean;
    };
    notes?: string;
};

// Submit form to create a new BehaviorLog row
export const submitForm: RequestHandler<{}, any, BodyType> = async (request: Request<{}, any, BodyType>, response: Response) => {
    try {
        const { studentID, behaviors, notes } = request.body;
        if (!behaviors) {
            response.status(400).json({ error: 'Invalid request format' });
            return;
        }
        const doc = SheetsHelper();
        await doc.loadInfo();
        const sheet = doc.sheetsByTitle[ 'BehaviorLog' ];
        if (!sheet) {
            throw new Error('BehaviorLog sheet not found');
            // TODO: Create behaviorLog sheet if it doesn't exist
        }
        const points = Object.values(behaviors).filter(Boolean).length;
        const timestamp = new Date().toISOString();
        await sheet.addRow({
            timestamp: timestamp,
            studentID: studentID,
            ...behaviors,
            points: points,
            notes: notes || ''
        });
        response.status(201).json({ success: true });
    } catch (error: any) {
        response.status(500).json({ error: error.message });
    }
}
