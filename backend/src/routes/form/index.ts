import { submitForm } from '../../handlers/form';
import { Router } from 'express';

const router = Router();

router.post('/submit', submitForm);

export default router;
