import { getStudents } from '../../handlers/students';
import { Router } from 'express';

const router = Router();

router.get('/', getStudents);

export default router;
