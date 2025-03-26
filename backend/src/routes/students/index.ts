import { getStudents, getStudentByToken } from '../../handlers/students';
import { Router } from 'express';

const authorize = require('../../helpers/authorize');

const router = Router();

router.get('/token', getStudentByToken);

const protectedRouter = Router();

protectedRouter.use(authorize);

protectedRouter.get('/', getStudents);

export { router as publicStudentsRouter, protectedRouter as protectedStudentsRouter };
