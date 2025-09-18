import { LeaderboardTimeframeSelector, BaseTable } from '../index';
import { useLeaderboardTable } from '../../hooks';
import { LeaderboardEntry } from '../../services';
import { Container } from 'react-bootstrap';

interface LeaderboardTableProps {
    itemsPerPage?: number;
}

export default function LeaderboardTable ({ itemsPerPage = 10 }: LeaderboardTableProps) {
    const {
        loading, error, data, allData, columns, filters, updateFilter, currentPage, totalPages, setCurrentPage,
        retry, currentTimeframe, handleTimeframeChange, filtersConfig
    } = useLeaderboardTable({ itemsPerPage: itemsPerPage });

    const headerConfig = {
        title: 'Leaderboard',
        itemName: 'entries',
        showCreateButton: false,
        additionalElements: (
            <LeaderboardTimeframeSelector
                currentTimeframe={currentTimeframe}
                onTimeframeChange={handleTimeframeChange}
            />
        ),
    };

    return (
        <Container fluid className='mt-3 pt-2 px-lg-5 mb-4'>
            <BaseTable<LeaderboardEntry>
                data={data}
                loading={loading}
                error={error}
                columns={columns}
                currentPage={currentPage}
                totalPages={totalPages}
                totalCount={allData.length}
                onPageChange={setCurrentPage}
                onRetry={retry}
                filtersConfig={filtersConfig}
                headerConfig={headerConfig}
                filters={filters}
                updateFilter={updateFilter}
            />
        </Container>
    );
}
