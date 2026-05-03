import { StudentRewardTable } from '../studentRewards';
import { useStudentRewardsTable } from '../../hooks';

interface StudentRewardsTableProps {
    studentName: string;
    itemsPerPage?: number;
    size?: 's' | 'm' | 'l';
}

export default function StudentRewardsTable({ studentName, itemsPerPage = 10, size = 'm' }: StudentRewardsTableProps) {
    const { modifiedFiltersConfig, modifiedHeaderConfig } = useStudentRewardsTable({ studentName, itemsPerPage });

    return (
        <StudentRewardTable
            itemsPerPage={itemsPerPage}
            customFiltersConfig={modifiedFiltersConfig}
            customHeaderConfig={modifiedHeaderConfig}
            size={size}
        />
    );
}
