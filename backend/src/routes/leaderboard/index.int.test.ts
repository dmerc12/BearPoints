import { createApp } from '../../helpers/createApp';
import { SheetsHelper } from '../../helpers/sheets';
import { Application } from 'express';
import request from 'supertest';

describe('Leaderboard Routes (Integration)', () => {
    let app: Application;

    beforeEach(async () => {
        const doc = SheetsHelper();
        await doc.loadInfo();
        const behaviorLogSheet = doc.sheetsByTitle[ 'BehaviorLog' ] ||
            await doc.addSheet({
                title: 'BehaviorLog',
                headerValues: [ 'timestamp', 'studentID', 'respectful', 'responsible', 'safe', 'points', 'notes' ]
            });
        await behaviorLogSheet.clearRows();
        await behaviorLogSheet.addRow({
            timestamp: new Date().toISOString(),
            studentID: 465,
            respectful: true,
            responsible: true,
            safe: false,
            points: 2,
            notes: 'Test notes'
        });
        const studentsSheet = doc.sheetsByTitle[ 'Students' ] ||
            await doc.addSheet({
                title: 'Students',
                headerValues: [ 'studentID', 'name', 'grade', 'teacher' ]
            });
        await studentsSheet.clearRows();
        await studentsSheet.addRow({
            studentID: 465,
            name: 'Test Student',
            grade: '3rd',
            teacher: 'Test Teacher'
        });
        app = createApp();
    });

    afterEach(async () => {
        const doc = SheetsHelper();
        await doc.loadInfo();
        const behaviorLogSheet = doc.sheetsByTitle[ 'BehaviorLog' ];
        const studentsSheet = doc.sheetsByTitle[ 'Students' ];
        if (behaviorLogSheet) await behaviorLogSheet.clearRows();
        if (studentsSheet) await studentsSheet.clearRows();
    });

    it('should return filtered leaderboard data', async () => {
        const response = await request(app).get('/api/leaderboard')
            .query({ timeframe: 'month' }).expect(200);
        expect(response.body).toEqual([ {
            studentID: 465,
            name: 'Test Student',
            teacher: 'Test Teacher',
            points: 2
        } ]);
    });

    it('should respect teacher filter', async () => {
        const startTime = Date.now();
        let response;
        let success = false;
        while (Date.now() - startTime < 10000) {
            response = await request(app).get('/api/leaderboard')
                .query({ teacher: 'Test Teacher' }).expect(200);
            if (response.body.length === 1) {
                success = true;
                break;
            }
            await new Promise(resolve => setTimeout(resolve, 500));
        }
        if (!success) {
            throw new Error('Teacher filter test failed after retries')
        }
        expect(response?.body.length).toBe(1);
    }, 15000);

    it('should handle invalid timeframe', async () => {
        const response = await request(app).get('/api/leaderboard')
            .query({ timeframe: 'invalid' }).expect(400);
        expect(response.body).toEqual({
            error: 'Invalid timeframe parameter'
        });
    });
});
