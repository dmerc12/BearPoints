import { Table, Pagination, Spinner, Alert, Container, Button, Row, Col } from 'react-bootstrap';
import { TableColumn, TableFilters, SortingConfig } from '../hooks';
import { TextFilter, SelectFilter, DateFilter } from './index';
import React, { useMemo, useCallback } from 'react';

export interface FilterConfig {
    key: string;
    type: 'text' | 'select' | 'date';
    label: string;
    placeholder?: string;
    options?: Array<{value: string; label: string}>;
    showHelpText?: boolean;
    helpText?: string;
}

export interface HeaderConfig {
    title: string;
    itemName: string;
    showCreateButton?: boolean;
    createButtonText?: string;
    additionalElements?: React.ReactNode;
}

export interface BaseTableProps<T> {
    data: T[];
    loading: boolean;
    error: string | null;
    columns: TableColumn<T>[];
    currentPage: number;
    totalPages: number;
    totalCount: number;
    onPageChange: (page: number) => void;
    onRetry?: () => void;
    size?: 's' | 'm' | 'l';
    showCreateButton?: boolean;
    createButtonText?: string;
    onCreateClick?: () => void;
    renderFilters?: () => React.ReactNode;
    renderHeader?: () => React.ReactNode;
    sortConfig?: SortingConfig[];
    onSort?: (field: string) => void;
    filtersConfig?: FilterConfig[];
    headerConfig?: HeaderConfig;
    filters?: TableFilters;
    updateFilter?: (key: string, value: string) => void;
    hidePagination?: boolean;
    emptyMessage?: string;
}

export default function BaseTable<T>(props: BaseTableProps<T>) {
    const {
        data, loading, error, columns, currentPage, totalPages, totalCount, onPageChange, onRetry, size = 'm',
        showCreateButton = false, createButtonText = 'Create', onCreateClick, renderFilters, renderHeader,
        sortConfig = [], onSort, filtersConfig, headerConfig, filters, updateFilter, hidePagination = false,
        emptyMessage = 'Nothing found matching the current filters'
    } = props;

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
                <Pagination.Item key={1}
                                 active={1 === currentPage}
                                 onClick={() => onPageChange(1)}
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
                    onClick={() => onPageChange(number)}
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
                    onClick={() => onPageChange(totalPages)}
                >
                    {totalPages}
                </Pagination.Item>
            );
        }
        return items;
    };

    const handlePrev = useCallback(() => {
        onPageChange(Math.max(1, currentPage - 1));
    }, [currentPage, onPageChange]);

    const handleNext = useCallback(() => {
        onPageChange(Math.min(totalPages, currentPage + 1));
    }, [totalPages, currentPage, onPageChange]);

    const generatedFilters = useMemo(() => {
        if (renderFilters || !filtersConfig || !updateFilter) return null;
        return (
            <Row className='mb-3 g-3'>
                {filtersConfig.map((filter) => {
                    if (filter.type === 'text') {
                        return (
                            <Col key={filter.key} md={4}>
                                <TextFilter value={filters?.[filter.key] || ''}
                                            onChange={(value) => updateFilter(filter.key, value)}
                                            label={filter.label}
                                            placeholder={filter.placeholder}
                                            showHelpText={filter.showHelpText}
                                            helpText={filter.helpText}
                                />
                            </Col>
                        );
                    } else if (filter.type === 'select') {
                        return (
                            <Col key={filter.key} md={4}>
                                <SelectFilter value={filters?.[filter.key] || ''}
                                              onChange={(value) => updateFilter(filter.key, value)}
                                              label={filter.label}
                                              options={filter.options || []}
                                />
                            </Col>
                        );
                    } else if (filter.type === 'date') {
                        return (
                            <Col key={filter.key} md={4}>
                                <DateFilter
                                    value={filters?.[filter.key] || ''}
                                    onChange={(value) => updateFilter(filter.key, value)}
                                    label={filter.label}
                                    placeholder={filter.placeholder}
                                />
                            </Col>
                        );
                    }
                    return null;
                })}
            </Row>
        );
    }, [filtersConfig, filters, updateFilter, renderFilters]);

    const generatedHeader = useMemo(() => {
        if (renderHeader || !headerConfig) return null;
        return (
            <div className='d-flex justify-content-between align-items-center mb-3'>
                <div>
                    <h2>{headerConfig.title}</h2>
                    <p className='text-muted'>
                        Showing {data.length} of {totalCount} {headerConfig.itemName}
                    </p>
                </div>
                <div>
                    {headerConfig.showCreateButton && onCreateClick && (
                        <Button variant='primary'
                                onClick={onCreateClick}
                                className='me-2'
                                size={size === 's' ? 'sm' : undefined}
                        >
                            {headerConfig.createButtonText || `Create ${headerConfig.itemName}`}
                        </Button>
                    )}
                    {headerConfig.additionalElements}
                </div>
            </div>
        );
    }, [headerConfig, data.length, totalCount, onCreateClick, size, renderHeader]);

    const defaultHeader = useMemo(() => {
        if (generatedHeader) return generatedHeader;
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
    }, [generatedHeader, data.length, showCreateButton, onCreateClick, createButtonText, size]);

    const handleHeaderClick = useCallback((column: TableColumn<T>) => {
        if (column.sortable && onSort) {
            onSort(column.key);
        }
    }, [onSort]);

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

    const getSortIndicator = (columnKey: string): string | null => {
        const index = sortConfig.findIndex(s => s.propertyName === columnKey);
        if (index === -1) return null;
        const direction = sortConfig[index].sortType;
        const arrow = direction === 'asc' ? '↑' : '↓';
        if (sortConfig.length > 1) {
            return `${arrow}${index + 1}`;
        }
        return arrow;
    };

    return (
        <div className={`table-responsive ${size === 's' ? 'table-sm' : ''}`}>
            { generatedFilters || renderFilters?.() }
            { generatedHeader || renderHeader?.() || defaultHeader }
            <Table striped bordered hover responsive
                   className={`text-center align-middle ${size === 's' ? 'table-sm' : ''}`}
            >
                <thead>
                    <tr>
                        {columns.map(column => (
                            <th key={column.key}
                                className={column.className}
                                onClick={() => handleHeaderClick(column)}
                                style={{ cursor: column.sortable ? 'pointer' : 'default', position: 'relative' }}
                            >
                                {column.header}
                                {column.sortable &&  (() => {
                                    const indicator = getSortIndicator(column.key);
                                    if (indicator) {
                                        return <span className='ms-1'>{indicator}</span>;
                                    }
                                    return null;
                                })()}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                { data.length === 0 ? (
                    <tr>
                        <td colSpan={columns.length}>
                            <Alert variant='info' className='mt-4 mb-0'>
                                {emptyMessage}
                            </Alert>
                        </td>
                    </tr>
                ) : (
                    data.map((item, index) => (
                        <tr key={ index }>
                            {columns.map(column => (
                                <td key={column.key}
                                    className={column.className}
                                >
                                    {column.render(item)}
                                </td>
                            ))}
                        </tr>
                    ))
                )}
                </tbody>
            </Table>
            { !hidePagination && totalPages > 1 && (
                <div className='d-flex justify-content-center mt-3'>
                    <Pagination className='flex-wrap'>
                        <Pagination.Prev onClick={handlePrev}
                                         disabled={currentPage === 1}
                        />
                        {renderPaginationItems()}
                        <Pagination.Next onClick={handleNext}
                                         disabled={currentPage === totalPages}
                        />
                    </Pagination>
                </div>
            )}
        </div>
    );
}
