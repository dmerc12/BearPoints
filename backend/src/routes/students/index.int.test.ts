import { SheetsHelper } from '../../helpers/sheets';
import { createApp } from '../../helpers/createApp';
import { Application } from 'express';
import request from 'supertest';

const SHEET_NAME = 'Students';
const SPREADSHEET_ID = process.env.SPREADSHEET_ID;

describe('Students Routes (Integration)', () => {
    let app: Application;

    beforeAll(async () => {
        await SheetsHelper.write(SHEET_NAME, 'A2:D', []);
        app = createApp();
        await SheetsHelper.write(SHEET_NAME, 'A2', [
            [ '789', 'Test Student', '3rd', 'Test Teacher' ],
        ]);
    });


    afterAll(async () => {
        await SheetsHelper.write(SHEET_NAME, 'A2', []);
    });

    it('should retrieve students from sheet', async () => {
        const response = await request(app).get('/api/students').expect(200);
        expect(response.body).toEqual([
            {
                studentID: null,
                name: 'Student Name',
                grade: 'Grade',
                teacher: 'Teacher',
            },
            {
                studentID: 789,
                name: 'Test Student',
                grade: '3rd',
                teacher: 'Test Teacher'
            }
        ]);
    });
});
