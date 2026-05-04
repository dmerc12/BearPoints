import { LeaderboardTimeframeSelector, BaseTable, FilterConfig, HeaderConfig } from '../index';
import { useLeaderboardTable } from '../../hooks';
import { LeaderboardEntryDTO } from '../../services';
import { Container } from 'react-bootstrap';

interface LeaderboardTableProps {
    itemsPerPage?: number;
    customFiltersConfig?: FilterConfig[];
    customHeaderConfig?: HeaderConfig;
    size?: 's' | 'm' | 'l';
}

export default function LeaderboardTable ({ itemsPerPage = 10, customFiltersConfig, customHeaderConfig, size = 'm' }
                                          : LeaderboardTableProps) {
    const {
        data, loading, error, filters, updateFilter, columns,
        retry, currentTimeframe, handleTimeframeChange, filtersConfig, headerConfig,
        currentPage, totalPages, setCurrentPage, totalCount,
        sortConfig, handleSort,
    } = useLeaderboardTable({ itemsPerPage });

    const finalFiltersConfig = customFiltersConfig ?? filtersConfig;
    const finalHeaderConfig = customHeaderConfig ?? headerConfig;

    const enhancedHeaderConfig = {
       ...finalHeaderConfig,
        additionalElements: (
            <LeaderboardTimeframeSelector
                currentTimeframe={currentTimeframe}
                onTimeframeChange={handleTimeframeChange}
            />
        ),
    };

    return (
        <Container fluid className='mt-3 pt-2 px-lg-5 mb-4'>
            <BaseTable<LeaderboardEntryDTO>
                data={data}
                loading={loading}
                error={error}
                columns={columns}
                currentPage={currentPage}
                totalPages={totalPages}
                totalCount={totalCount}
                onPageChange={setCurrentPage}
                onRetry={retry}
                filtersConfig={finalFiltersConfig}
                headerConfig={enhancedHeaderConfig}
                filters={filters}
                updateFilter={updateFilter}
                sortConfig={sortConfig}
                onSort={handleSort}
                size={size}
            />
        </Container>
    );
}
