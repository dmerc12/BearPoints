import { healthCheck } from '../../handlers/health';
import express from 'express';

const router = express.Router();

router.get('/', healthCheck);

export default router;
