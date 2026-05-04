import { useClassroomLeaderboardTable } from '../../hooks';
import { LeaderboardTable } from '../leaderboard';

interface ClassroomLeaderboardTableProps {
    teacherId: number;
    itemsPerPage?: number;
    size?: 's' | 'm' | 'l';
}

export default function ClassroomLeaderboardTable({
    teacherId,
    itemsPerPage = 10,
    size = 'm',
}: ClassroomLeaderboardTableProps) {
    const { modifiedFiltersConfig, modifiedHeaderConfig } =
        useClassroomLeaderboardTable({ teacherId, itemsPerPage });

    return (
        <LeaderboardTable
            itemsPerPage={itemsPerPage}
            customFiltersConfig={modifiedFiltersConfig}
            customHeaderConfig={modifiedHeaderConfig}
            size={size}
        />
    );
}
