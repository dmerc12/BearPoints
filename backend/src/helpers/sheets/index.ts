import { google } from 'googleapis';

const auth = new google.auth.GoogleAuth({
    keyFile: './service-account.json',
    scopes: [ 'https://www.googleapis.com/auth/spreadsheets' ]
});
const sheets = google.sheets({ version: 'v4', auth });
const spreadsheetId = process.env.SPREADSHEET_ID;

export const SheetsHelper = {
    async write(sheetName: string, range: string, values: any[] | any[][]) {
        const formattedValues = Array.isArray(values[ 0 ]) ? values : [ values ];
        try {
            const response = await sheets.spreadsheets.values.update({
                spreadsheetId,
                range: `${sheetName}!${range}`,
                valueInputOption: 'USER_ENTERED',
                requestBody: { values: formattedValues }
            });
            return response.data;
        } catch (error: any) {
            throw new Error(`Failed to write data to sheet: ${sheetName}\nError: ${error.message}`);
        }
    },

    async read(sheetName: string, range: string) {
        try {
            const response = await sheets.spreadsheets.values.get({
                spreadsheetId,
                range: `${sheetName}!${range}`
            });
            const values = response.data.values || [];
            return values.length === 1 ? values[ 0 ] : values;
        } catch (error: any) {
            throw new Error(`Failed to read data from sheet: ${sheetName}\nError: ${error.message}`);
        }
    },

    async append (sheetName: string, values: any[] | any[][]) {
        if (!values) throw new Error(`Failed to append data to sheet: ${sheetName}\nError: No values provided`);
        const formattedValues = Array.isArray(values[ 0 ]) ? values : [ values ];
        try {
            const response = await sheets.spreadsheets.values.append({
                spreadsheetId,
                range: `${sheetName}!A:Z`,
                valueInputOption: 'USER_ENTERED',
                requestBody: { values: formattedValues }
            });
            return response.data;
        } catch (error: any) {
            throw new Error(`Failed to append data to sheet: ${sheetName}\nError: ${error.message}`)
        }
    }
};
