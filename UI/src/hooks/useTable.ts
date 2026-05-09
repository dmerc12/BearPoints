import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {RootState, useAppDispatch, useAppSelector} from '../store';
import {UnknownAction} from '@reduxjs/toolkit';
import {ThunkAction} from 'redux-thunk';

export interface SortingConfig {
    propertyName: string;
    sortType: 'asc' | 'desc';
}

export interface TableFilters {
    [key: string]: string;
}

export interface TableHelpers<T> {
    handleEditItem?: (item: T) => void;
    handleDeleteItem?: (item: T) => void;
}

export interface TableColumn<T> {
    key: string;
    header: string;
    render: (item: T, helpers?: TableHelpers<T>) => React.ReactNode;
    className?: string;
    sortable?: boolean;
}

type ThunkResult<R> = ThunkAction<R, any, any, UnknownAction>;

export interface UseTableOptions<T, F extends TableFilters> {
    selector: (state: RootState) => {
        data: T[];
        loading: boolean;
        error: string | null;
        pagination: {
            totalPages: number;
            totalElements: number;
        };
    };
    initialFilters: F;
    mode?: 'read-only' | 'crud';
    fetchAction?: (params: {
        page: number;
        size: number;
        sort?: string;
        force?: boolean;
        [key: string]: unknown;
    }) => ThunkResult<unknown>;
    itemsPerPage?: number;
    columnsBuilder?: (helpers: TableHelpers<T>) => TableColumn<T>[];
    defaultColumns?: TableColumn<T>[];
    getServerSortField?: (clientField: string) => string;
    getFetchParams?: (
        filters: F,
        page: number,
        size: number,
        sort?: string
    ) => {
        page: number;
        size: number;
        sort?: string;
        force?: boolean;
        [key: string]: unknown;
    };
}

export function useTable<T, F extends TableFilters>(props: UseTableOptions<T, F>) {
    const {
        selector, initialFilters, mode = 'read-only', fetchAction, itemsPerPage = 10,
        columnsBuilder, defaultColumns = [], getServerSortField, getFetchParams
    } = props;

    const dispatch = useAppDispatch();
    const { data, loading, error, pagination } = useAppSelector(selector);

    const [filters, setFilters] = useState<F>(initialFilters);
    const [currentPage, setCurrentPage] = useState(1);
    const [sortConfig, setSortConfig] = useState<SortingConfig[]>([]);

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingItem, setEditingItem] = useState<T | null>(null);
    const [deletingItem, setDeletingItem] = useState<T | null>(null);

    const lastFetchedRef = useRef<string>('');
    const abortControllerRef = useRef<AbortController | null>(null);

    const getSortField = useCallback(
        (clientField: string): string => {
            if (getServerSortField) {
                return getServerSortField(clientField);
            }
            return clientField;
    }, [getServerSortField]);

    const sortParam = useMemo(() => {
        if (sortConfig.length === 0) return undefined;
        return sortConfig.map((config) => {
            const serverField = getSortField(config.propertyName);
            return `sort=${serverField},${config.sortType}`;
        }).join('&');
    }, [sortConfig, getSortField]);

    // Build fetch params using getFetchParams if provided, otherwise basic pagination
    const buildFetchParams = useCallback(() => {
        const baseParams = {
            page: currentPage - 1,
            size: itemsPerPage,
            sort: sortParam,
        };
        if (getFetchParams) {
            return getFetchParams(filters, currentPage - 1, itemsPerPage, sortParam);
        }
        return baseParams;
    }, [currentPage, itemsPerPage, sortParam, getFetchParams, filters])

    // Fetch data on mount
    useEffect(() => {
        if (!fetchAction) return
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }
        abortControllerRef.current = new AbortController();
        const params = buildFetchParams();
        const paramsKey = JSON.stringify(params);
        if (paramsKey ===  lastFetchedRef.current) return;
        lastFetchedRef.current = paramsKey;
        dispatch(fetchAction(params));
    }, [dispatch, fetchAction, buildFetchParams]);

    const helpers = useMemo(() => ({
        handleEditItem: mode === 'crud' ? (item: T) => setEditingItem(item) : undefined,
        handleDeleteItem: mode === 'crud' ? (item: T) => setDeletingItem(item) : undefined,
    }), [mode]);

    const columns = useMemo(() => {
        if (columnsBuilder) {
            return columnsBuilder(helpers);
        }
        return defaultColumns;
    }, [columnsBuilder, helpers, defaultColumns]);

    const totalPages = pagination.totalPages;
    const totalCount = pagination.totalElements;

    const handleSort = useCallback((field: string) => {
        setSortConfig((currentConfig) => {
            const pendingChange = [...currentConfig];
            const existingIndex = pendingChange.findIndex((config) =>
                config.propertyName === field);
            if (existingIndex > -1) {
                const existingConfig = pendingChange[existingIndex];
                pendingChange.splice(existingIndex, 1);
                if (existingConfig.sortType === 'asc') {
                    pendingChange.push({ propertyName: field, sortType: 'desc' });
                }
            } else {
                pendingChange.push({ propertyName: field, sortType: 'asc' });
            }
            return pendingChange;
        });
        setCurrentPage(1);
    }, []);

    const updateFilter = useCallback((filterKey: keyof F, value: string) => {
        setFilters(prev => {
            if (prev[filterKey] === value) return prev;
            return { ...prev, [filterKey]: value };
        });
        setCurrentPage(1);
    }, []);

    const updateFilterAny = useCallback((key: string, value: string) => {
        if (key in initialFilters) {
            updateFilter(key as keyof F, value);
        }
    }, [updateFilter, initialFilters]);

    const resetFilters = useCallback(() => {
        setFilters(initialFilters);
        setCurrentPage(1);
    }, [initialFilters]);

    const resetSorting = useCallback(() => {
        setSortConfig([]);
        setCurrentPage(1);
    }, []);

    const handleCreateItem = useCallback(() => {
        if (mode === 'crud') {
            setShowCreateModal(true);
        }
    }, [mode]);

    const handleEditItem = useCallback((item: T) => {
        if (mode === 'crud') {
            setEditingItem(item);
        }
    }, [mode]);

    const handleDeleteItem = useCallback((item: T) => {
        if (mode === 'crud') {
            setDeletingItem(item);
        }
    }, [mode]);

    const handleCloseModals = useCallback(() => {
        if (mode === 'crud') {
            setShowCreateModal(false);
            setEditingItem(null);
            setDeletingItem(null);
        }
    }, [mode]);

    const retryFetch = useCallback(() => {
        if (fetchAction) {
            const params = buildFetchParams();
            dispatch(fetchAction({ ...params, force: true }));
        }
    }, [dispatch, fetchAction, buildFetchParams]);

    const handleSuccess = useCallback(() => {
        if (mode === 'crud') {
            handleCloseModals();
            retryFetch();
        }
    }, [mode, handleCloseModals, retryFetch]);

    const baseReturn = {
        // Data
        data, totalCount, loading, error,
        // Columns
        columns,
        // Filters
        filters, updateFilter: updateFilterAny, resetFilters,
        // Sorting
        sortConfig, handleSort, resetSorting,
        // Pagination
        currentPage, setCurrentPage, totalPages, itemsPerPage,
        // Data refresh
        retry: retryFetch,
    };

    if (mode === 'crud') {
        return {
            ...baseReturn,
            // CRUD state
            showCreateModal, editingItem, deletingItem,
            // CRUD actions
            handleCreateItem, handleEditItem, handleDeleteItem,
            handleCloseModals, handleSuccess
        };
    }

    return baseReturn;
}
