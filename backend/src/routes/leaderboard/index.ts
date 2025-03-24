import { getLeaderboard } from '../../handlers/leaderboard';
import { Router } from 'express';

const router = Router();

router.get('/',getLeaderboard
);

export default router;
