import { Request, Response, NextFunction } from 'express-serve-static-core';
import * as dotenv from 'dotenv';

dotenv.config();

// Initialize firebase app
const admin = require('firebase-admin');
const firebaseConfig = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT || '');
admin.initializeApp({
    credential: admin.credential.cert(firebaseConfig)
});

const authorize = async (request: Request, response: Response, next: NextFunction) => {
    // Get firebase token from request authorization header
    const authHeader = request.headers.authorization || '';
    if (!authHeader?.startsWith('Bearer ')) {
        return response.status(401).json({
            message: 'Unauthorized - No token provided',
            code: 'MISSING_TOKEN'
        });
    }
    const token = authHeader?.split('Bearer ')[ 1 ];
    if (!token) return response.status(401).send('Unauthorized');
    // Validate token with firebase
    try {
        const decodedToken = await admin.auth().verifyIdToken(token);
        const allowedDomain = process.env.ALLOWED_DOMAIN as string || '@okcps.org';
        if (!decodedToken.email?.endsWith(allowedDomain)) {
            return response.status(403).json({
                message: 'Forbidden - Invalid email domain',
                code: 'INVALID_DOMAIN'
            })
        }
        const email = decodedToken.email as string || '';
        const isValidEmail = email?.endsWith(allowedDomain);
        if (!email || !isValidEmail) {
            return response.status(403).send('Forbidden');
        }
        request.user = decodedToken;
        next();
    } catch (error) {
        console.error('Authorization error:', error);
        return response.status(401).json({message: 'Invalid token', code: 'INVALID_TOKEN'});
    }
};

module.exports = authorize;
