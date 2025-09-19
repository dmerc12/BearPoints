import { BaseTable, ManagementButtons, FilterConfig, HeaderConfig } from './index';
import { TableColumn, TableFilters } from '../hooks';
import React, { useMemo } from 'react';

export interface CrudTableProps<T> {
    // BaseTable props
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
    filtersConfig?: FilterConfig[];
    headerConfig?: HeaderConfig;
    filters?: TableFilters;
    updateFilter?: (key: string, value: string) => void;
    // CRUD-specific props
    canEdit: boolean;
    canDelete: boolean;
    onEditItem: (item: T) => void;
    onDeleteItem: (item: T) => void;
    onCreateClick: () => void;
    createModal: React.ReactNode
    editModal: React.ReactNode;
    deleteModal: React.ReactNode;
    actionColumnHeader?: string;
    managementButtonsSize?: 'sm' | 'lg';
}

export function CrudTable<T>(props: CrudTableProps<T>) {
    const {
        data, loading, error, columns, currentPage, totalPages, totalCount, onPageChange, onRetry, size = 'm',
        filtersConfig, headerConfig, filters, updateFilter, canEdit, canDelete, onEditItem, onDeleteItem,
        onCreateClick, createModal, editModal, deleteModal,
        actionColumnHeader = 'Actions', managementButtonsSize = 'sm'
    } = props;
    const tableColumns = useMemo(() => {
        if (!canEdit && !canDelete) return columns;
        return [
            ...columns,
            {
                key: 'actions',
                header: actionColumnHeader,
                render: (item: T) => (
                    <ManagementButtons
                        onEdit={() => onEditItem(item)}
                        onDelete={() => onDeleteItem(item)}
                        size={managementButtonsSize}
                        showEdit={canEdit}
                        showDelete={canDelete}
                    />
                )
            }
        ];
    }, [columns, canEdit, canDelete, onEditItem, onDeleteItem, actionColumnHeader, managementButtonsSize]);

    return (
        <>
            <BaseTable<T>
                data={data}
                loading={loading}
                error={error}
                columns={tableColumns}
                currentPage={currentPage}
                totalPages={totalPages}
                totalCount={totalCount}
                onPageChange={onPageChange}
                onRetry={onRetry}
                size={size}
                filtersConfig={filtersConfig}
                filters={filters}
                updateFilter={updateFilter}
                showCreateButton={canEdit && canDelete}
                createButtonText={headerConfig?.createButtonText || 'Create'}
                onCreateClick={onCreateClick}
            />
            {createModal}
            {editModal}
            {deleteModal}
        </>
    );
}
