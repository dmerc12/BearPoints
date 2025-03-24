import { GoogleSpreadsheet } from 'google-spreadsheet';
import { GoogleAuth } from 'google-auth-library'

export const SheetsHelper = () => {
    const auth = new GoogleAuth({
        keyFile: './service-account.json',
        scopes: [ 'https://www.googleapis.com/auth/spreadsheets' ]
    });
    const spreadsheetId = process.env.SPREADSHEET_ID;
    if (!spreadsheetId) {
        throw new Error('SPREADSHEET_ID environment variable is not set');
    }
    const doc = new GoogleSpreadsheet(spreadsheetId, auth);
    return doc;
};
