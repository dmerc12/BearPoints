import { SheetsHelper } from '../../helpers/sheets';
import { createApp } from '../../helpers/createApp';
import { Application } from 'express';
import request from 'supertest';

const TEST_SHEET_NAME = 'BehaviorLog';
const HEADER_VALUES = [ 'timestamp', 'studentID','respectful','responsible','safe','points','notes' ];

describe('Form Routes (Integration)', () => {
    let app: Application;
    let testSheet: any;

    beforeAll(async () => {
        const doc = SheetsHelper();
        await doc.loadInfo();
        testSheet = doc.sheetsByTitle[ TEST_SHEET_NAME ];
        if (testSheet) {
            await testSheet.clearRows();
        } else {
            testSheet = await doc.addSheet({
                title: TEST_SHEET_NAME,
                headerValues: HEADER_VALUES
            });
        }
        app = createApp();
    });

    afterEach(async () => {
        await testSheet.clearRows();
    });

    it('should handle invalid submissions', async () => {
        const response = await request(app).post('/api/form/submit').send({ invalid: 'data' }).expect(400);
        expect(response.body).toEqual({
            error: 'Invalid request format'
        });
    });

    it('should handle empty notes by storing empty string', async () => {
        const testData = {
            studentID: '123',
            behaviors: {
                respectful: false,
                responsible: true,
                safe: true
            }
        };
        await request(app).post('/api/form/submit').send(testData).expect(201);
        const rows = await testSheet.getRows();
        expect(rows[ 0 ].notes).toBeUndefined();
    });

    it('should handle empty notes string', async () => {
        const testData = {
            studentID: '123',
            behaviors: {
                respectful: true,
                responsible: false,
                safe: true
            },
            notes: ''
        };
        await request(app).post('/api/form/submit').send(testData).expect(201);
        const rows = await testSheet.getRows();
        expect(rows[ 0 ].notes).toBeUndefined();
    });
});
