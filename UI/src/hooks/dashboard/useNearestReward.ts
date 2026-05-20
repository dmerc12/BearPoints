import { useAppSelector, useAppDispatch, fetchRewardItems } from '../../store';
import { RewardItemDTO } from '../../services';
import { useEffect, useMemo } from 'react';

export interface UseNearestRewardProps {
    currentPoints: number;
}

export interface UseNearestRewardReturn {
    nearestReward: RewardItemDTO | null;
    pointsToNextReward: number;
    percentageToNextReward: number;
    loading: boolean;
    nextRewardName: string;
    nextRewardCost: number;
}

export function useNearestReward({ currentPoints }: UseNearestRewardProps): UseNearestRewardReturn {
    const dispatch = useAppDispatch();
    const { data: rewardItems, loading } = useAppSelector(state => state.rewardItems);

    useEffect(() => {
        if (rewardItems.length === 0 && !loading) {
            dispatch(fetchRewardItems({ page: 0, size: 100, force: true }));
        }
    }, [dispatch, loading, rewardItems.length]);

    const nearestReward = useMemo(() => {
        const affordableItems = rewardItems.filter(item =>
            item.pointCost > currentPoints
        );
        if (affordableItems.length === 0) return null;
        return affordableItems.reduce((prev, curr) =>
            curr.pointCost < prev.pointCost ? curr : prev
        );
    }, [rewardItems, currentPoints]);

    const pointsToNextReward = useMemo(() => {
        if (!nearestReward) return 0;
        return nearestReward.pointCost - currentPoints;
    }, [nearestReward, currentPoints]);

    const percentageToNextReward = useMemo(() => {
        if (!nearestReward) return 100;
        const totalNeeded = nearestReward.pointCost;
        return Math.min((currentPoints / totalNeeded) * 100, 100);
    }, [nearestReward, currentPoints]);

    return {
        nearestReward,
        pointsToNextReward,
        percentageToNextReward,
        loading,
        nextRewardName: nearestReward?.name || 'Max Level',
        nextRewardCost: nearestReward?.pointCost || currentPoints,
    };
}
