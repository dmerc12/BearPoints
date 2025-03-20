import express from 'express';
import cors from 'cors';

export function createApp () {
    // Create express application
    const app = express();

    // Enable application to use cors
    app.use(cors());

    // Routes go below

    return app;
}
