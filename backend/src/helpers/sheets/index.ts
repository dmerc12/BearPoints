import { GoogleSpreadsheet } from 'google-spreadsheet';
import { JWT } from 'google-auth-library'

export const SheetsHelper = () => {
    const serviceAccount = process.env.SHEETS_SERVICE_ACCOUNT;
    const spreadsheetId = process.env.SPREADSHEET_ID;
    if (!serviceAccount) {
        throw new Error('SHEETS_SERVICE_ACCOUNT environment variable is not set');
    }
    if (!spreadsheetId) {
        throw new Error('SPREADSHEET_ID environment variable is not set');
    }
    const credentials = JSON.parse(serviceAccount)
    const auth = new JWT({
        email: credentials.client_email,
        key: credentials.private_key.replace(/\\n/g, '\n'),
        scopes: [ 'https://www.googleapis.com/auth/spreadsheets' ]
    });
    return new GoogleSpreadsheet(spreadsheetId, auth);
};
