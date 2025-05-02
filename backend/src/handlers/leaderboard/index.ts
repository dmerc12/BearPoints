import { Request, RequestHandler, Response } from 'express-serve-static-core';
import { mapSheets } from '../../helpers/sheets';

// Get leaderboard
export const getLeaderboard: RequestHandler = async (request: Request, response: Response) => {
    try {
        // Get all behavior logs and students
        const { bragLogs, students } = await mapSheets(response);
        response.json({ bragLogs, students });
    } catch (error: any) {
        response.status(500).json({ error: error.message });
    }
}
