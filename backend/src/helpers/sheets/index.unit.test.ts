import { SheetsHelper } from '.';

var mockAuth: jest.Mock;
var mockUpdate: jest.Mock;
var mockGet: jest.Mock;
var mockAppend: jest.Mock;

jest.mock('googleapis', () => {
    mockAuth = jest.fn().mockImplementation(() => ({}));
    mockUpdate = jest.fn().mockResolvedValue({ data: {} });
    mockGet = jest.fn().mockResolvedValue({ data: { values: [ [ 'Test Data' ] ] } });
    mockAppend = jest.fn().mockResolvedValue({ data: {} });
    return {
        google: {
            auth: {
                GoogleAuth: mockAuth
            },
            sheets: jest.fn().mockImplementation(() => ({
                spreadsheets: {
                    values: {
                        update: mockUpdate,
                        get: mockGet,
                        append: mockAppend
                    }
                }
            }))
        }
    };
});

describe('authentication', () => {
    it('should initialize auth with correct config', () => {
        expect(mockAuth).toHaveBeenCalledWith({
            keyFile: './service-account.json',
            scopes: [ 'https://www.googleapis.com/auth/spreadsheets' ]
        });
    });
});

describe('SheetsHelper', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe('write', () => {
        it('should write data successfully', async () => {
            await SheetsHelper.write('TestSheet', 'A1', [ 'Test' ]);
            expect(mockUpdate).toHaveBeenCalledWith({
                spreadsheetId: process.env.SPREADSHEET_ID,
                range: 'TestSheet!A1',
                valueInputOption: 'USER_ENTERED',
                requestBody: { values: [ [ 'Test' ] ] }
            });
        });
        it('should throw error when write fails', async () => {
            mockUpdate.mockRejectedValueOnce(new Error('API Error'));
            await expect(SheetsHelper.write('TestSheet', 'A1', [ 'Test' ]))
                .rejects.toThrow('Failed to write data to sheet: TestSheet');
        });
        it('should handle 2D array input', async () => {
            await SheetsHelper.write('TestSheet', 'A1', [ [ 'Row1' ], [ 'Row2' ] ]);
            expect(mockUpdate).toHaveBeenCalledWith(expect.objectContaining({
                requestBody: { values: [ [ 'Row1' ], [ 'Row2' ] ] }
            }));
        });
    });

    describe('read', () => {
        it('should read data successfully', async () => {
            const result = await SheetsHelper.read('TestSheet', 'A1');
            expect(mockGet).toHaveBeenCalledWith({
                spreadsheetId: process.env.SPREADSHEET_ID,
                range: 'TestSheet!A1'
            });
            expect(result).toEqual([ 'Test Data' ]);
        });
        it('should handle multiple rows', async () => {
            mockGet.mockResolvedValueOnce({ data: { values: [ [ 'A1' ], [ 'A2' ] ] } });
            const result = await SheetsHelper.read('TestSheet', 'A1:A2');
            expect(result).toEqual([ [ 'A1' ], [ 'A2' ] ]);
        });
        it('should handle empty response', async () => {
            mockGet.mockResolvedValueOnce({ data: {} });
            const result = await SheetsHelper.read('TestSheet', 'A1');
            expect(result).toEqual([]);
        });
        it('should throw error when read fails', async () => {
            mockGet.mockRejectedValueOnce(new Error('API Error'));
            await expect(SheetsHelper.read('TestSheet', 'A1'))
                .rejects.toThrow('Failed to read data from sheet: TestSheet');
        });
    });

    describe('append', () => {
        it('should append data successfully', async () => {
            await SheetsHelper.append('TestSheet', [ 'Appended' ]);
            expect(mockAppend).toHaveBeenCalledWith({
                spreadsheetId: process.env.SPREADSHEET_ID,
                range: 'TestSheet!A:Z',
                valueInputOption: 'USER_ENTERED',
                requestBody: { values: [ [ 'Appended' ] ] }
            });
        });
        it('should throw error when append fails', async () => {
            mockAppend.mockRejectedValueOnce(new Error('API Error'));
            await expect(SheetsHelper.append('TestSheet', [ 'Appended' ]))
                .rejects.toThrow('Failed to append data to sheet: TestSheet');
        });
        it('should handle single-row append', async () => {
            await SheetsHelper.append('TestSheet', [ 'SingleValue' ]);
            expect(mockAppend).toHaveBeenCalledWith(expect.objectContaining({
                requestBody: { values: [ [ 'SingleValue' ] ] }
            }));
        });
        it('should handle 2D array input without wrapping', async () => {
            await SheetsHelper.append('TestSheet', [ [ 'Row1' ], [ 'Row2' ] ]);

            expect(mockAppend).toHaveBeenCalledWith(expect.objectContaining({
                requestBody: { values: [ [ 'Row1' ], [ 'Row2' ] ] }
            }));
        });
        it('should throw error when values are missing', async () => {
            await expect(SheetsHelper.append('TestSheet', null as any))
                .rejects.toThrow('Failed to append data to sheet: TestSheet\nError: No values provided');
        });
    });
});
