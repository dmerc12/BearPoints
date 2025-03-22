import { GoogleSpreadsheet } from 'google-spreadsheet';
import { SheetsHelper } from '.';

describe('SheetsHelper (Integration)', () => {
    it('should throw an error if SPREADSHEET_ID is not set', () => {
        const originalSpreadsheetId = process.env.SPREADSHEET_ID;
        delete process.env.SPREADSHEET_ID;
        expect(() => SheetsHelper()).toThrow('SPREADSHEET_ID environment variable is not set');
        process.env.SPREADSHEET_ID = originalSpreadsheetId;
    });

    it('should return a GoogleSpreadsheet instance if SPREADSHEET_ID is set', async () => {
        if (!process.env.SPREADSHEET_ID) {
            throw new Error('SPREADSHEET_ID environment variable is not set for integration tests');
        }
        const doc = SheetsHelper();
        expect(doc).toBeInstanceOf(GoogleSpreadsheet);
        await doc.loadInfo();
        expect(doc.title).toBeDefined();
    });
});
