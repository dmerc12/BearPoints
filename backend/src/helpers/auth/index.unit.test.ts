import { authenticateUser } from '.';
import admin from 'firebase-admin';

const mockVerifyIdToken = jest.fn();

jest.mock('firebase-admin', () => ({
    auth: () => ({
        verifyIdToken: mockVerifyIdToken
    })
}));

describe('authenticateUser (Unit)', () => {
    const mockVerify = admin.auth().verifyIdToken as jest.Mock;
    let req: any;
    let res: any;
    let next: jest.Mock;

    beforeEach(() => {
        req = { headers: {}, user: null };
        res = {
            status: jest.fn(() => res),
            json: jest.fn(() => res),
        };
        next = jest.fn();
        mockVerifyIdToken.mockReset();
    });

    it('should reject requests without authorization header', async () => {
        await authenticateUser(req, res, next);
        expect(res.status).toHaveBeenCalled();
    });

    it('should reject invalid token format', async () => {
        req.headers = { authorization: 'InvalidToken' };
        await authenticateUser(req, res, next);
        expect(res.status).toHaveBeenCalledWith(401);
        expect(next).not.toHaveBeenCalled();
    });

    it('should reject invalid tokens', async () => {
        req.headers = { authorization: 'Bearer invalid' };
        mockVerify.mockRejectedValue(new Error('Invalid token'));
        await authenticateUser(req, res, next);
        expect(res.status).toHaveBeenCalledWith(401);
        expect(next).not.toHaveBeenCalled();
    });

    it('should reject tokens without email', async () => {
        req.headers.authorization = 'Bearer valid-token';
        mockVerifyIdToken.mockResolvedValue({ uid: 'test-uid' });
        await authenticateUser(req, res, next);
        expect(res.status).toHaveBeenCalledWith(403);
        expect(res.json).toHaveBeenCalledWith({ error: 'Forbidden - email not found in token' });
        expect(next).not.toHaveBeenCalled();
    });

    it('should reject unauthorized domains', async () => {
        req.headers = { authorization: 'Bearer valid-token' };
        mockVerifyIdToken.mockResolvedValue({ email: 'user@invalid.com', uid: 'test-uid' });
        await authenticateUser(req, res, next);
        expect(res.status).toHaveBeenCalledWith(403);
        expect(res.json).toHaveBeenCalledWith({ error: 'Forbidden - invalid domain' });
        expect(next).not.toHaveBeenCalled();
    });

    it('should allow valid requests', async () => {
        const mockUser = {
            email: 'user@okcps.org',
            uid: 'test-uid',
            iss: 'firebase-issuer',
            aud: 'firebase-audience',
            iat: Date.now(),
            exp: Date.now() + 3600
        };
        req.headers = { authorization: 'Bearer valid-token' };
        mockVerifyIdToken.mockResolvedValue(mockUser);
        await authenticateUser(req, res, next);
        expect(req.user).toEqual(mockUser);
        expect(next).toHaveBeenCalled();
        expect(res.status).not.toHaveBeenCalled();
    });
});
