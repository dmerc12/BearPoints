import { Request, Response, RequestHandler } from 'express-serve-static-core';
import { checkHealth } from '../../helpers/sheets';

export const healthCheck: RequestHandler = async (request: Request, response: Response) => {
    try {
        await checkHealth(response);
    } catch (error: any) {
        console.log('Health check failed:', error);
        response.status(500).json({
            healthy: false,
            error: error.message,
            details: {
                serviceAccount: process.env.SHEETS_SERVICE_ACCOUNT ? 'Configured' : 'Missing',
                spreadsheetId: process.env.SPREADSHEET_ID ? 'Configured' : 'Missing'
            }
        });
    }
};
