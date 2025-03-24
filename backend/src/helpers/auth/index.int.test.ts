import express, { Request, Response } from 'express';
import { createFirebase } from '../createFirebase';
import { getAuth } from 'firebase-admin/auth';
import { createApp } from '../createApp';
import { authenticateUser } from '.';
import request from 'supertest';

const firebase = createFirebase()
const auth = getAuth(firebase);

const createTestApp = () => {
    const app = createApp();
    app.use('/protected', authenticateUser, (req: Request, res: Response) => {
        res.status(200).json({ user: req.user });
    });
    return app;
};

describe('authenticateUser (Integration)', () => {
    let app: express.Express;
    let validToken: string;
    let invalidDomainToken: string;
    let noEmailToken: string;

    beforeAll(async () => {
        app = createTestApp();
        const validUser = await auth.createUser({
            email: 'testuser@gmail.com',
            password: 'testPassword123'
        });

        const invalidUser = await auth.createUser({
            email: 'testuser@example.com',
            password: 'testPassword123'
        });

        const noEmailUser = await auth.createUser({
            uid: 'test-uid-no-email'
        });

        validToken = await exchangeCustomTokenForIdToken(await auth.createCustomToken(validUser.uid));
        invalidDomainToken = await exchangeCustomTokenForIdToken(await auth.createCustomToken(invalidUser.uid));
        noEmailToken = await exchangeCustomTokenForIdToken(await auth.createCustomToken(noEmailUser.uid));
    });

    afterAll(async () => {
        await auth.deleteUser((await auth.getUserByEmail('testuser@gmail.com')).uid);
        await auth.deleteUser((await auth.getUserByEmail('testuser@example.com')).uid);
        await auth.deleteUser('test-uid-no-email');
    });

    it('should reject tokens without email', async () => {
        const response = await request(app).get('/protected').set('Authorization', `Bearer ${noEmailToken}`);
        expect(response.status).toBe(403);
        expect(response.body.error).toBe('Forbidden - email not found in token');
    })

    it('should reject unauthenticated requests', async () => {
        const response = await request(app).get('/protected');
        expect(response.status).toBe(401);
        expect(response.body.error).toBe('Unauthorized');
    });

    it('should reject invalid tokens', async () => {
        const response = await request(app).get('/protected').set('Authorization', 'Bearer invalid-token');
        expect(response.status).toBe(401);
        expect(response.body.error).toBe('Invalid token');
    });

    it('should allow valid tokens with authorized domain', async () => {
        const response = await request(app).get('/protected').set('Authorization', `Bearer ${validToken}`);
        expect(response.status).toBe(200);
        expect(response.body.user.email).toBe('testuser@gmail.com');
    });

    it('should reject unauthorized domains', async () => {
        const response = await request(app).get('/protected').set('Authorization', `Bearer ${invalidDomainToken}`);
        expect(response.status).toBe(403);
        expect(response.body.error).toBe('Forbidden - invalid domain');
    });
});

// Helper function
async function exchangeCustomTokenForIdToken (customToken: string): Promise<string> {
    const response = await fetch(`https://identitytoolkit.googleapis.com/v1/accounts:signInWithCustomToken?key=${process.env.FIREBASE_WEB_API_KEY}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            token: customToken,
            returnSecureToken: true
        })
    });
    const data = await response.json();
    return data.idToken;
}
