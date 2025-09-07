import { useState, useCallback } from 'react';

export interface TableFilters {
    [key: string]: string;
}

export function useTableFilters(initialFilters: TableFilters) {
    const [filters, setFilters] = useState<TableFilters>(initialFilters);

    const updateFilter = useCallback((filterKey: string, value: string) => {
        setFilters(prev => ({
            ...prev,
            [filterKey]: value
        }));
    }, []);

    const resetFilters = useCallback(() => {
        setFilters(initialFilters);
    }, [initialFilters]);

    return { filters, updateFilter, resetFilters, setFilters };
}

export function useTableModals<T>() {
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingItem, setEditingItem] = useState<T | null>(null);
    const [deletingItem, setDeletingItem] = useState<T | null>(null);

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

    return {
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, setShowCreateModal, setEditingItem, setDeletingItem
    };
}
