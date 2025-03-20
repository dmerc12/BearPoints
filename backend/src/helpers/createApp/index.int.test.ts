import request from 'supertest';
import { createApp } from '.';
import express from 'express';

describe('createApp (Integration)', () => {
    let app: express.Application;

    beforeAll(() => {
        app = createApp();
        app.get('/test', ((req: express.Request, res: express.Response) => {
            res.status(200).send('OK');
        }) as express.RequestHandler);
    });

    it('should handle valid requests', async () => {
        const response = await request(app).get('/test');
        expect(response.status).toBe(200);
        expect(response.text).toBe('OK');
    });

    it('should return 404 for unknown routes', async () => {
        const app = createApp();
        const response = await request(app).get('/non-existent-route');
        expect(response.status).toBe(404);
    });
});
