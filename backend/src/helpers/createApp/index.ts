import { publicStudentsRouter, protectedStudentsRouter} from '../../routes/students';
import leaderboardRouter from '../../routes/leaderboard';
import formRouter from '../../routes/form';
import express from 'express';
import cors from 'cors';

const authorize = require('../authorize');

export function createApp () {
    // Create express application
    const app = express();

    // Enable JSON body parser
    app.use(express.json());

    // Enable application to use cors
    app.use(cors({
        origin: [
            // process.env.API_DOMAIN,
            // process.env.APP_DOMAIN,
            process.env.FIREBASE_AUTH_DOMAIN,
            // 'http://localhost:3000',
            'http://localhost:5173',
        ].filter(Boolean) as string[],
        credentials: true,
        methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
        allowedHeaders: ['content-type', 'Authorization', 'X-Requested-With'],
        exposedHeaders: ['Authorization']
    }));

    // Routes that do not require authorization go below
    app.use('/api/form', formRouter);
    app.use('/api/students', publicStudentsRouter);

    // Middleware for authorization
    app.use(authorize);

    // Routes that require authorization go below
    app.use('/api/students', protectedStudentsRouter);
    app.use('/api/leaderboard', leaderboardRouter);

    return app;
}
