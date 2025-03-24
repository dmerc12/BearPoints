import { GoogleSpreadsheet } from 'google-spreadsheet';
import { GoogleAuth } from 'google-auth-library';
import { SheetsHelper } from '.';

jest.mock('google-spreadsheet', () => ({
    GoogleSpreadsheet: jest.fn().mockImplementation(() => ({}))
}));

jest.mock('google-auth-library', () => ({
    GoogleAuth: jest.fn().mockImplementation(() => ({}))
}));

const MockedGoogleAuth = GoogleAuth as jest.MockedClass<typeof GoogleAuth>;
const MockedGoogleSpreadsheet = GoogleSpreadsheet as jest.MockedClass<typeof GoogleSpreadsheet>;

describe('SheetsHelper', () => {
    beforeEach(() => {
        jest.clearAllMocks();
        delete process.env.SPREADSHEET_ID;
    });

    it('should throw an error if SPREADSHEET_ID is not set', () => {
        expect(() => SheetsHelper()).toThrow('SPREADSHEET_ID environment variable is not set');
    });

    it('should return a GoogleSpreadsheet instance if SPREADSHEET_ID is set', () => {
        process.env.SPREADSHEET_ID = 'test-spreadsheet-id';
        const mockAuth = {};
        const mockDoc = {};
        MockedGoogleAuth.mockImplementation(() => mockAuth as any);
        MockedGoogleSpreadsheet.mockImplementation(() => mockDoc as any);
        const result = SheetsHelper();
        expect(MockedGoogleAuth).toHaveBeenCalledWith({
            keyFile: './service-account.json',
            scopes: [ 'https://www.googleapis.com/auth/spreadsheets' ]
        });
        expect(MockedGoogleSpreadsheet).toHaveBeenCalledWith('test-spreadsheet-id', mockAuth);
        expect(result).toBe(mockDoc);
    });
});
