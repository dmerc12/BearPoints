import { getStudents, getStudentByToken } from '../../handlers/students';
import { Router } from 'express';

const router = Router();

router.get('/', getStudents);
router.get('/token', getStudentByToken);

export default router;
