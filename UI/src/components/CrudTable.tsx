import { BaseTable, ManagementButtons, FilterConfig, HeaderConfig } from './index';
import { TableColumn, TableFilters, SortingConfig } from '../hooks';
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
    headerButtons?: React.ReactNode;
    filters?: TableFilters;
    updateFilter?: (key: string, value: string) => void;
    sortConfig?: SortingConfig[];
    onSort?: (field: string) => void;
    // CRUD-specific props
    canEdit: boolean | ((item: T) => boolean);
    canDelete: boolean | ((item: T) => boolean);
    onEditItem: (item: T) => void;
    onDeleteItem: (item: T) => void;
    onCreateClick: () => void;
    createModal: React.ReactNode
    editModal: React.ReactNode;
    deleteModal: React.ReactNode;
    actionColumnHeader?: string;
    managementButtonsSize?: 'sm' | 'lg';
    selectable?: boolean;
    selectedIds?: (number | string)[];
    onSelectRow?: (id: number | string, checked: boolean) => void;
    onSelectAll?: (checked: boolean, allIds: (number | string)[]) => void;
    getId?: (item: T) => number | string;
}

export function CrudTable<T>(props: CrudTableProps<T>) {
    const {
        data, loading, error, columns, currentPage, totalPages, totalCount, onPageChange, onRetry, size = 'm',
        filtersConfig, headerConfig, headerButtons, filters, updateFilter, sortConfig, onSort, canEdit, canDelete,
        onEditItem, onDeleteItem, onCreateClick, createModal, editModal, deleteModal,
        actionColumnHeader = 'Actions', managementButtonsSize = 'sm',
        selectable = false, selectedIds = [], onSelectRow, onSelectAll, getId
    } = props;

    const tableColumns = useMemo(() => {
        const hasEditCapability = canEdit || typeof canEdit === 'function';
        const hasDeleteCapability = canDelete || typeof canDelete === 'function';
        if (!hasEditCapability && !hasDeleteCapability) {
            return columns;
        }
        return [
            ...columns,
            {
                key: 'actions',
                header: actionColumnHeader,
                render: (item: T) => {
                    const itemCanEdit = typeof canEdit === 'function' ? canEdit(item) : canEdit;
                    const itemCanDelete = typeof canDelete === 'function' ? canDelete(item) : canDelete;
                    if (!itemCanEdit && !itemCanDelete) return null;
                    return (
                        <ManagementButtons
                            onEdit={() => onEditItem(item)}
                            onDelete={() => onDeleteItem(item)}
                            size={managementButtonsSize}
                            showEdit={itemCanEdit}
                            showDelete={itemCanDelete}
                        />
                    )
                }
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
                sortConfig={sortConfig}
                onSort={onSort}
                showCreateButton={headerConfig?.showCreateButton || false}
                createButtonText={headerConfig?.createButtonText || 'Create'}
                onCreateClick={onCreateClick}
                headerButtons={headerButtons}
                selectable={selectable}
                selectedIds={selectedIds}
                onSelectRow={onSelectRow}
                onSelectAll={onSelectAll}
                getId={getId}
            />
            {createModal}
            {editModal}
            {deleteModal}
        </>
    );
}
