import studentsRouter from '../../routes/students';
import formRouter from '../../routes/form';
import express from 'express';
import cors from 'cors';

export function createApp () {
    // Create express application
    const app = express();

    // Enable JSON body parser
    app.use(express.json());

    // Enable application to use cors
    app.use(cors());

    // Routes go below
    app.use('/api/form', formRouter);
    app.use('/api/students', studentsRouter);

    return app;
}
