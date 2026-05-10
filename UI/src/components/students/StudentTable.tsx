import { CreateStudentModal, EditStudentModal, DeleteStudentModal, CrudTable, QRCodesPrint } from '../index';
import React, { useMemo, useCallback, useRef } from 'react';
import type { FilterConfig, HeaderConfig } from '../index';
import { useReactToPrint } from 'react-to-print';
import { useNavigate } from 'react-router-dom';
import { useStudentTable } from '../../hooks';
import { StudentDTO } from '../../services';
import { QRCodeSVG } from 'qrcode.react';
import { Button } from 'react-bootstrap';

interface StudentTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    showPrintButton?: boolean;
    showQRCodes?: boolean;
    size?: 's' | 'm' | 'l';
    customCreateModal?: React.ReactNode;
    customFiltersConfig?: FilterConfig[];
    customHeaderConfig?: HeaderConfig;
}

export default function StudentTable (props: StudentTableProps) {
    const {
        itemsPerPage = 10, showFilters = true, showPrintButton = true, showQRCodes = true, size = 'm',
        customCreateModal, customFiltersConfig, customHeaderConfig
    } = props;

    const navigate = useNavigate();
    const contentRef = useRef<HTMLDivElement>(null);
    const reactToPrintFn = useReactToPrint({ contentRef });

    const {
        data, loading, error, filters, updateFilter, sortConfig, handleSort, isAuthorized, columns,
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, retry, handleSuccess, filtersConfig, headerConfig,
        currentPage, totalPages, setCurrentPage, totalCount,
        selectedStudentIds, selectedStudents, handleSelectRow, handleSelectAll
    } = useStudentTable({ itemsPerPage });

    const printButton = showPrintButton ? (
        <Button variant='primary'
                onClick={() => reactToPrintFn()}
                className='no-print'
                style={{ marginLeft: '10px' }}
                size={size === 's' ? 'sm' : undefined}
                disabled={selectedStudents.length === 0}
        >
            Print QR Codes ({selectedStudents.length})
        </Button>
    ) : null;

    const qrCodeSize = useMemo(() => {
        if (size === 's') return 48;
        if (size === 'l') return window.innerWidth < 768 ? 80 : 100;
        return window.innerWidth < 768 ? 64 : 80
    }, [size]);

    const handleQRScan = useCallback((token: string) => {
        navigate(`/brag?token=${token}`);
    }, [navigate]);

    const enhancedColumns = useMemo(() => {
        const enhanced = [...columns];
        if (showQRCodes) {
            enhanced.push({
                key: 'qrCode',
                header: 'QR Code',
                render: (student: StudentDTO) => {
                    if (student.token === undefined || student.token === null) return null;
                    return (
                        <QRCodeSVG
                            value={`${import.meta.env.VITE_APP_URL}/behavior?token=${student.token}`}
                            size={qrCodeSize}
                            onClick={() => handleQRScan(student.token as string)}
                            bgColor='#FFFFFF'
                            fgColor='#000000'
                            level='L'
                        />
                    );
                }
            });
        }
        return enhanced;
    }, [columns, showQRCodes, qrCodeSize, handleQRScan]);

    const finalFiltersConfig = customFiltersConfig ?? (showFilters ? filtersConfig : undefined);

    return (
        <>
            <CrudTable<StudentDTO>
                data={data}
                loading={loading}
                error={error}
                columns={enhancedColumns}
                currentPage={currentPage}
                totalPages={totalPages}
                totalCount={totalCount}
                onPageChange={setCurrentPage}
                onRetry={retry}
                filtersConfig={finalFiltersConfig}
                headerConfig={customHeaderConfig || headerConfig}
                headerButtons={printButton}
                selectable={true}
                selectedIds={selectedStudentIds}
                onSelectRow={handleSelectRow}
                onSelectAll={handleSelectAll}
                getId={(student) => student.id!}
                filters={filters}
                updateFilter={updateFilter}
                size={size}
                sortConfig={sortConfig}
                onSort={handleSort}
                canEdit={isAuthorized}
                canDelete={isAuthorized}
                onEditItem={handleEditItem}
                onDeleteItem={handleDeleteItem}
                onCreateClick={handleCreateItem}
                createModal={customCreateModal || (
                    <CreateStudentModal
                        show={showCreateModal}
                        onCancel={handleCloseModals}
                        onSuccess={handleSuccess}
                    />
                )}
                editModal={
                    <EditStudentModal
                        show={!!editingItem}
                        student={editingItem}
                        onCancel={handleCloseModals}
                        onSuccess={handleSuccess}
                    />
                }
                deleteModal={
                    <DeleteStudentModal
                        show={!!deletingItem}
                        student={deletingItem}
                        onCancel={handleCloseModals}
                        onSuccess={handleSuccess}
                    />
                }
            />
            <div style={{ display: 'none' }}>
                <QRCodesPrint ref={ contentRef } students={ selectedStudents } />
            </div>
        </>
    );
}
