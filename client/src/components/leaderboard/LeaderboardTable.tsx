import { LeaderboardFilters, LeaderboardTimeframeSelector, BaseTable } from '../index';
import { useLeaderboardTable } from '../../hooks';
import { LeaderboardEntry } from '../../services';
import { Container } from 'react-bootstrap';

interface LeaderboardTableProps {
    itemsPerPage?: number;
}

export default function LeaderboardTable ({ itemsPerPage = 10 }: LeaderboardTableProps) {
    const {
        loading, error, currentTimeframe, filters, filteredEntries, teacherOptions, gradeOptions,
        columns, handleFilterChange, handleTimeframeChange, retry
    } = useLeaderboardTable();

    const renderHeader = () => (
        <div className='text-center mb-4'>
            <h1>Leaderboard</h1>
            <LeaderboardTimeframeSelector
                currentTimeframe={currentTimeframe}
                onTimeframeChange={handleTimeframeChange}
            />
        </div>
    );

    const renderFilters = () => (
        <LeaderboardFilters
            teacherFilter={filters.teacherFilter}
            gradeFilter={filters.gradeFilter}
            teacherOptions={teacherOptions}
            gradeOptions={gradeOptions}
            onFilterChange={handleFilterChange}
        />
    );

    return (
        <Container fluid className='mt-3 pt-2 px-lg-5 mb-4'>
            <BaseTable<LeaderboardEntry>
                data={filteredEntries}
                loading={loading}
                error={error}
                columns={columns}
                itemsPerPage={itemsPerPage}
                renderHeader={renderHeader}
                renderFilters={renderFilters}
                onRetry={retry}
            />
        </Container>
    );
}
