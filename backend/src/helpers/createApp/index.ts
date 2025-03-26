import leaderboardRouter from '../../routes/leaderboard';
import { publicStudentsRouter, protectedStudentsRouter} from '../../routes/students';
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
            process.env.VITE_APP_URL as string,
            process.env.FIREBASE_AUTH_DOMAIN as string,
            'http://localhost:3000',
            'http://localhost:5173'
        ].filter(Boolean) as string[]
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
