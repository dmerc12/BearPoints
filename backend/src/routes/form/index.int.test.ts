import { SheetsHelper } from '../../helpers/sheets';
import { createApp } from '../../helpers/createApp';
import { Application } from 'express';
import request from 'supertest';

const TEST_SHEET_NAME = 'BehaviorLog';
const TEST_SPREADSHEET_ID = process.env.TEST_SPREADSHEET_ID;

describe('Form Routes (Integration)', () => {
    let app: Application;

    beforeEach(() => {
        process.env.SPREADSHEET_ID = TEST_SPREADSHEET_ID;
        app = createApp();
        // await SheetsHelper.clear(TEST_SHEET_NAME);
    });

    // afterAll(async () => {
    //     await SheetsHelper.clear(TEST_SHEET_NAME);
    // });

    it('should successfully submit valid form data to sheets', async () => {
        const testData = {
            studentId: 12345,
            behaviors: {
                respectful: true,
                responsible: true,
                safe: false,
            },
            notes: 'Integration test entry'
        };
        const response = await request(app).post('/api/form/submit').send(testData).expect(201);
        expect(response.body).toEqual({ success: true });
        const [ header, ...rows ] = await SheetsHelper.read(TEST_SHEET_NAME, 'A1:G20');
        const newRow = rows.find(row => row[ 1 ] === testData.studentId.toString());
        expect(newRow).toMatchObject([
            expect.any(String),
            testData.studentId.toString(),
            'TRUE',
            'TRUE',
            'FALSE',
            '2',
            testData.notes
        ]);
    });

    it('should handle invalid submissions', async () => {
        const response = await request(app).post('/api/form/submit').send({ invalid: 'data' }).expect(400);
        expect(response.body).toEqual({
            error: 'Invalid request format'
        });
    });

    it('should handle empty notes by storing empty string', async () => {
        const testData = {
            studentId: 67890,
            behaviors: {
                respectful: false,
                responsible: true,
                safe: true
            }
        };
        const response = await request(app).post('/api/form/submit').send(testData).expect(201);
        const [ _, ...rows ] = await SheetsHelper.read(TEST_SHEET_NAME, 'A1:G20');
        const newRow = rows.find(row => row[ 1 ] === testData.studentId.toString());
        expect(newRow).toMatchObject([
            expect.any(String),
            testData.studentId.toString(),
            'FALSE',
            'TRUE',
            'TRUE',
            '2'
        ]); 
    });

    it('should handle empty notes string', async () => {
        const testData = {
            studentId: 13579,
            behaviors: {
                respectful: true,
                responsible: false,
                safe: true
            },
            notes: ''
        };
        const response = await request(app).post('/api/form/submit').send(testData).expect(201);
        const [ _, ...rows ] = await SheetsHelper.read(TEST_SHEET_NAME, 'A1:G20');
        const newRow = rows.find(row => row[ 1 ] === testData.studentId.toString());
        expect(newRow).toMatchObject([
            expect.any(String),
            testData.studentId.toString(),
            'TRUE',
            'FALSE',
            'TRUE',
            '2'
        ]);
    });
});
