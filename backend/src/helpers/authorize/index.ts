import { Request, Response, NextFunction } from 'express-serve-static-core';
import { ServiceAccount } from 'firebase-admin';

// Initialize firebase app
const admin = require('firebase-admin');
const firebaseConfig = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT || '');
admin.initializeApp({
    credential: admin.credential.cert(firebaseConfig)
});

const authorize = async (request: Request, response: Response, next: NextFunction) => {
    // Get firebase token from request authorization header
    const authorization = request.headers.authorization || '';
    const token = authorization?.split('Bearer ')[ 1 ];
    if (!token) return response.status(401).send('Unauthorized');
    // Validate token with firebase
    try {
        const decodedToken = await admin.auth().verifyIdToken(token);
        const allowedDomain = process.env.ALLOWED_DOMAIN as string || '@okcps.org';
        const email = decodedToken.email as string || '';
        const isValidEmail = email?.endsWith(allowedDomain);
        if (!email || !isValidEmail) {
            return response.status(403).send('Forbidden');
        }
        request.user = decodedToken;
        next();
    } catch (error) {
        console.error('Authorization error:', error);
        return response.status(401).send('Invalid token');
    }
};

module.exports = authorize;
