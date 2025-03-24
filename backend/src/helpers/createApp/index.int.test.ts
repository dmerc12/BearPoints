import request from 'supertest';
import { createApp } from '.';
import express from 'express';

describe('createApp (Integration)', () => {
    let app: express.Application;

    beforeAll(async () => {
        app = createApp();
    });

    it('should handle valid requests', async () => {
        app.get('/test', ((req: express.Request, res: express.Response) => {
            res.status(200).send('OK');
        }) as express.RequestHandler);
        const response = await request(app).get('/test');
        expect(response.status).toBe(200);
        expect(response.text).toBe('OK');
    });

    it('should return 404 for unknown routes', async () => {
        const response = await request(app).get('/non-existent-route');
        expect(response.status).toBe(404);
    });

    it('should have CORS enabled', async () => {
        const response = await request(app).options('/');
        expect(response.headers[ 'access-control-allow-origin' ]).toBe('*');
    });

    it('should have form routes mounted', async () => {
        const response = await request(app).post('/api/form/submit');
        expect(response.status).toBe(400);
    });

    it('should have students routes mounted', async () => {
        const response = await request(app).get('/api/students');
        expect(response.status).toBe(200);
    });

    it('should have leaderboard routes mounted', async () => {
        const response = await request(app).get('/api/leaderboard');
        expect(response.status).toBe(200);
    });
});
