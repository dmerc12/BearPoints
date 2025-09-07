import { useAppDispatch, useAppSelector } from '../store/hooks';
import { useState, useCallback, useEffect } from 'react';
import { RootState } from '../store';

export interface TableFilters {
    [key: string]: string;
}

export interface UseTableOptions<T, F extends TableFilters> {
    fetchAction: (params: { page: number; size: number; force?: boolean }) => unknown;
    selector: (state: RootState) => {
        data: T[];
        loading: boolean;
        error: string | null;
    };
    initialFilters: F;
}

export function useTable<T, F extends TableFilters>({ fetchAction, selector, initialFilters }: UseTableOptions<T, F>) {
    const dispatch = useAppDispatch();
    const { data, loading, error } = useAppSelector(selector);

    const [filters, setFilters] = useState<F>(initialFilters);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingItem, setEditingItem] = useState<T | null>(null);
    const [deletingItem, setDeletingItem] = useState<T | null>(null);

    // Fetch data on mount
    useEffect(() => {
        dispatch(fetchAction({ page: 0, size: 1000 }) as never);
    }, [dispatch, fetchAction]);

    const updateFilter = useCallback((filterKey: keyof F, value: string) => {
        setFilters(prev => ({
            ...prev,
            [filterKey]: value,
        }));
    }, []);

    const resetFilters = useCallback(() => {
        setFilters(initialFilters);
    }, [initialFilters]);

    const handleCreateItem = useCallback(() => {
        setShowCreateModal(true);
    }, []);

    const handleEditItem = useCallback((item: T) => {
        setEditingItem(item);
    }, []);

    const handleDeleteItem = useCallback((item: T) => {
        setDeletingItem(item);
    }, []);

    const handleCloseModals = useCallback(() => {
        setShowCreateModal(false);
        setEditingItem(null);
        setDeletingItem(null);
    }, []);

    const retryFetch = useCallback(() => {
        dispatch(fetchAction({ page: 0, size: 1000, force: true }) as never);
    }, [dispatch, fetchAction]);

    const handleSuccess = useCallback(() => {
        handleCloseModals();
        retryFetch();
    }, [handleCloseModals, retryFetch]);

    return {
        // Data
        data, loading, error,
        // Filters
        filters, updateFilter, resetFilters,
        // Modals
        showCreateModal, editingItem, deletingItem,
        handleCreateItem, handleEditItem, handleDeleteItem, handleCloseModals,
        // Utilities
        retryFetch, handleSuccess
    };
}
