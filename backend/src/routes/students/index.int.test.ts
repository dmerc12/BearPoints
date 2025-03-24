import { SheetsHelper } from '../../helpers/sheets';
import { createApp } from '../../helpers/createApp';
import { Application } from 'express';
import request from 'supertest';

const SHEET_NAME = 'Students';

const HEADER_VALUES = [ 'studentID', 'name', 'grade', 'teacher' ]

describe('Students Routes (Integration)', () => {
    let app: Application;
    let sheet: any;

    beforeAll(async () => {
        const doc = SheetsHelper();
        await doc.loadInfo();
        sheet = doc.sheetsByTitle[ SHEET_NAME ] ||
            await doc.addSheet({ title: SHEET_NAME, headerValues: HEADER_VALUES });
        await sheet.clearRows()
        await sheet.setHeaderRow(HEADER_VALUES);
        await sheet.addRow({ studentID: 789, name: 'Test Student', grade: '3rd', teacher: 'Test Teacher' });
        app = createApp();
    });

    // afterAll(async () => {
    //     const doc = SheetsHelper();
    //     await doc.loadInfo();
    //     const sheet = doc.sheetsByTitle[ SHEET_NAME ];
    //     if (sheet) await sheet.clearRows();
    // });

    it('should retrieve students from sheet', async () => {
        const startTime = Date.now();
        let response;
        let success = false;
        while (Date.now() - startTime < 10000) {
            response = await request(app).get('/api/students').expect(200);
            if (response.body.length === 1) {
                success = true;
                break;
            }
            await new Promise(resolve => setTimeout(resolve, 500));
        }
        if (!success) {
            throw new Error('getStudents integration test failed after retries');
        }
        expect(response?.body).toEqual([
            {
                studentID: 789,
                name: 'Test Student',
                grade: '3rd',
                teacher: 'Test Teacher'
            }
        ]);
    }, 15000);
});
