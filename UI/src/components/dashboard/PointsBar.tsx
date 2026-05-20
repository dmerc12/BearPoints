import { ProgressBar, Spinner } from 'react-bootstrap';
import { useNearestReward } from '../../hooks';

interface PointsBarProps {
    points: number;
    maxPoints?: number;
    label?: string;
    size?: 's' | 'l';
    showLabel?: boolean;
    showNextReward?: boolean;
}

export default function PointsBar({
    points,
    maxPoints = 1000,
    label = 'Your Points',
    size = 'l',
    showLabel = true,
    showNextReward = true,
}: PointsBarProps) {
    const { pointsToNextReward, percentageToNextReward, nextRewardName, nextRewardCost, loading } =
        useNearestReward({ currentPoints: points });

    const getVariant = () => {
        if (percentageToNextReward >= 80) return 'success';
        if (percentageToNextReward >= 50) return 'info';
        if (percentageToNextReward >= 25) return 'warning';
        return 'danger';
    };

    const effectiveMax = nextRewardCost !== points ? nextRewardCost : maxPoints;
    const effectivePercentage = nextRewardCost !== points ? percentageToNextReward
        : (points / maxPoints) * 100;

    if (loading) {
        return (
            <div className="mb-4 p-3 border rounded bg-light text-center">
                <Spinner animation="border" size="sm" />
                <span className="ms-2">Loading rewards...</span>
            </div>
        )
    }

    return (
        <div className="mb-4 p-3 border rounded bg-light">
            {showLabel && (
                <div className="d-flex justify-content-between mb-2">
                    <strong>{label}</strong>
                    <span>{points} points</span>
                </div>
            )}
            <ProgressBar
                now={effectivePercentage}
                label={size === 'l' && effectivePercentage > 10 ? `${Math.round(effectivePercentage)}%` : undefined}
                variant={getVariant()}
                style={{ height: size === 'l' ? '25px' : '15px' }}
                animated={points < effectiveMax}
            />
            {showNextReward && nextRewardName !== 'Max Level' && (
                <div className="mt-2 text-center">
                    <small className="text-muted">
                        🎯 {pointsToNextReward} more points to unlock:{' '}
                        <strong>{nextRewardName}</strong> ({nextRewardCost} pts)
                    </small>
                </div>
            )}
            {showNextReward && nextRewardName === 'Max Level' && (
                <div className="mt-2 text-center">
                    <small className="text-success">
                        🏆 Max level reached! All rewards unlocked!
                    </small>
                </div>
            )}
            {!showLabel && (
                <div className="text-center mt-1 small text-muted">
                    {points} points
                </div>
            )}
        </div>
    );
}
