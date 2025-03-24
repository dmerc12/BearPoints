import { Response, NextFunction } from 'express-serve-static-core';
import { AuthenticatedRequest } from '../../types';
import admin from 'firebase-admin';

// Authentication middleware
export const authenticateUser = async (req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        res.status(401).json({ error: 'Unauthorized' });
        return;
    }
    try {
        const idToken = authHeader.split(' ')[ 1 ];
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        // Verify school domain
        if (!decodedToken.email) {
            res.status(403).json({ error: 'Forbidden - email not found in token' });
            return;
        }
        const allowedDomain = process.env.ALLOWED_DOMAIN;
        if (!allowedDomain || !decodedToken.email.endsWith(allowedDomain)) {
            res.status(403).json({ error: 'Forbidden - invalid domain' });
            return;
        }
        req.user = decodedToken;
        next();
    } catch (error) {
        res.status(401).json({ error: 'Invalid token' });
    }
};
