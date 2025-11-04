import { LeaderboardTimeframeSelector, BaseTable } from '../index';
import { useLeaderboardTable } from '../../hooks';
import { LeaderboardEntry } from '../../services';
import { Container } from 'react-bootstrap';

interface LeaderboardTableProps {
    itemsPerPage?: number;
}

export default function LeaderboardTable (props: LeaderboardTableProps) {
    const { itemsPerPage = 10 } = props;
    const {
        data, loading, error, filters, updateFilter, columns,
        retry, currentTimeframe, handleTimeframeChange, filtersConfig, headerConfig,
        currentPage, totalPages, setCurrentPage, totalCount
    } = useLeaderboardTable({ itemsPerPage: itemsPerPage });

    const enhancedHeaderConfig = {
       ...headerConfig,
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
                totalCount={totalCount}
                onPageChange={setCurrentPage}
                onRetry={retry}
                filtersConfig={filtersConfig}
                headerConfig={enhancedHeaderConfig}
                filters={filters}
                updateFilter={updateFilter}

            />
        </Container>
    );
}
