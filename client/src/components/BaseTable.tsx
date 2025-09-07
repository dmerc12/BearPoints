import { Table, Pagination, Spinner, Alert, Container, Button } from 'react-bootstrap';
import React, { useState, useMemo, useCallback } from 'react';

interface BaseTableProps<T> {
    data: T[];
    loading: boolean;
    error: string | null;
    columns: TableColumn<T>[];
    itemsPerPage?: number;
    renderFilters?: () => React.ReactNode;
    renderHeader?: () => React.ReactNode;
    onRetry?: () => void;
    size?: 's' | 'm' | 'l';
    showCreateButton?: boolean;
    createButtonText?: string;
    onCreateClick?: () => void;
}

export interface TableColumn<T> {
    key: string;
    header: string;
    render: (item: T) => React.ReactNode;
    className?: string;
}

export default function BaseTable<T>({ data, loading, error, columns, renderFilters, renderHeader, onRetry, size = 'm',
                                         itemsPerPage = 10, showCreateButton = false, createButtonText = 'Create',
                                         onCreateClick }: BaseTableProps<T>) {
    const [currentPage, setCurrentPage] = useState(1);

    const totalPages = Math.ceil(data.length / itemsPerPage);

    const paginatedData = useMemo(() => {
        if (data.length === 0) return [];
        const startIndex = (currentPage - 1) * itemsPerPage;
        const endIndex = Math.min(startIndex + itemsPerPage, data.length);
        return data.slice(startIndex, endIndex);
    }, [data, currentPage, itemsPerPage]);

    const renderPaginationItems = () => {
        const items = [];
        const maxVisiblePages = 7;
        let startPage = 1;
        let endPage = totalPages;
        if (totalPages > maxVisiblePages) {
            const half = Math.floor(maxVisiblePages / 2);
            startPage = Math.max(1, currentPage - half);
            endPage = Math.min(totalPages, currentPage + half);
            if (currentPage - half < 1) {
                endPage = maxVisiblePages;
            }
            if (currentPage + half > totalPages) {
                startPage = totalPages - maxVisiblePages + 1;
            }
        }
        if (startPage > 1) {
            items.push(
                <Pagination.Item key={1} active={1 === currentPage}
                                 onClick={() => setCurrentPage(1)}
                >
                    1
                </Pagination.Item>
            );
            if (startPage > 2) {
                items.push(<Pagination.Ellipsis key='start-ellipsis' disabled />);
            }
        }
        for (let number = startPage; number <= endPage; number++) {
            items.push(
                <Pagination.Item
                    key={number}
                    active={number === currentPage}
                    onClick={() => setCurrentPage(number)}
                >
                    {number}
                </Pagination.Item>
            );
        }
        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                items.push(<Pagination.Ellipsis key='end-ellipsis' disabled />);
            }
            items.push(
                <Pagination.Item
                    key={totalPages}
                    active={totalPages === currentPage}
                    onClick={() => setCurrentPage(totalPages)}
                >
                    {totalPages}
                </Pagination.Item>
            );
        }
        return items;
    };

    const handlePrev = useCallback(() => {
        setCurrentPage(prev => Math.max(1, prev - 1));
    }, []);

    const handleNext = useCallback(() => {
        setCurrentPage(prev => Math.min(totalPages, prev + 1));
    }, [totalPages]);

    const defaultHeader = useMemo(() => {
        return (
            <div className='m-2 d-flex justify-content-between align-items-center'>
                <span>Showing {data.length} items</span>
                {showCreateButton && onCreateClick && (
                    <Button variant='primary'
                            onClick={onCreateClick}
                            className='me-2'
                            size={size === 's' ? 'sm' : undefined}
                    >
                        {createButtonText}
                    </Button>
                )}
            </div>
        );
    }, [data.length, showCreateButton, onCreateClick, createButtonText, size]);

    if (loading) {
        return (
            <Container className='d-flex justify-content-center align-items-center'
                       style={{ minHeight: '200px' }}
            >
                <Spinner animation='border' variant='primary'>
                    <span className='visually-hidden'>Loading...</span>
                </Spinner>
                <span className='ms-2'>Loading...</span>
            </Container>
        );
    }

    if (error) {
        return (
            <Alert variant='danger' className='text-center'>
                <Alert.Heading>Error</Alert.Heading>
                <p>{error}</p>
                {onRetry && (
                    <Button variant='outline-danger'
                            onClick={onRetry}
                    >
                        Retry
                    </Button>
                )}
            </Alert>
        );
    }

    return (
        <div className={`table-responsive ${size === 's' ? 'table-sm' : ''}`}>
            { renderFilters && renderFilters() }
            { renderHeader ? renderHeader() : defaultHeader }
            <Table striped bordered hover responsive
                   className={`text-center align-middle ${size === 's' ? 'table-sm' : ''}`}
            >
                <thead>
                    <tr>
                        {columns.map(column => (
                            <th key={column.key} className={column.className}>
                                {column.header}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                { data.length === 0 ? (
                    <tr>
                        <td colSpan={columns.length}>
                            <Alert variant='info' className='mt-4 mb-0'>
                                Nothing found matching the current filters
                            </Alert>
                        </td>
                    </tr>
                ) : (
                    paginatedData.map((item, index) => (
                        <tr key={ index }>
                            {columns.map(column => (
                                <td key={column.key} className={column.className}>
                                    {column.render(item)}
                                </td>
                            ))}
                        </tr>
                    ))
                )}
                </tbody>
            </Table>
            { totalPages > 1 && (
                <div className='d-flex justify-content-center mt-3'>
                    <Pagination className='flex-wrap'>
                        <Pagination.Prev onClick={handlePrev} disabled={currentPage === 1} />
                        {renderPaginationItems()}
                        <Pagination.Next onClick={handleNext} disabled={currentPage === totalPages} />
                    </Pagination>
                </div>
            )}
        </div>
    );
}
