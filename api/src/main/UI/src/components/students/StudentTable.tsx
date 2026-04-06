import { CreateStudentModal, EditStudentModal, DeleteStudentModal, CrudTable, QRCodesPrint } from '../index';
import { useMemo, useCallback, useRef } from 'react';
import { useReactToPrint } from 'react-to-print';
import { useNavigate } from 'react-router-dom';
import { useStudentTable } from '../../hooks';
import { QRCodeSVG } from 'qrcode.react';
import { Student } from '../../services';
import { Button } from 'react-bootstrap';

interface StudentTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    showPrintButton?: boolean;
    showQRCodes?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function StudentTable (props: StudentTableProps) {
    const {
        itemsPerPage = 10, showFilters = true, showPrintButton = true, showQRCodes = true, size = 'm'
    } = props;

    const navigate = useNavigate();
    const contentRef = useRef<HTMLDivElement>(null);
    const reactToPrintFn = useReactToPrint({ contentRef });

    const {
        data, loading, error, filters, updateFilter, canManageStudent, columns,
        showCreateModal, editingItem, deletingItem, handleCreateItem, handleEditItem, handleDeleteItem,
        handleCloseModals, retry, handleSuccess, filtersConfig, headerConfig,
        currentPage, totalPages, setCurrentPage, totalCount
    } = useStudentTable({ itemsPerPage });

    const enhancedHeaderConfig = useMemo(() => ({
        ...headerConfig,
        additionalElements: showPrintButton && data.length > 0 ? (
            <Button variant='primary'
                    onClick={() => reactToPrintFn()}
                    className='no-print'
                    style={{ marginLeft: '10px' }}
                    size={size === 's' ? 'sm' : undefined}
            >
                Print QR Codes ({data.length})
            </Button>
        ) : null,
    }), [headerConfig, showPrintButton, data.length, reactToPrintFn, size]);

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
                render: (student: Student) => (
                    <QRCodeSVG
                        value={`${import.meta.env.VITE_APP_URL}/behavior?token=${student.token}`}
                        size={ qrCodeSize }
                        onClick={ () => handleQRScan(student.token) }
                        bgColor='#FFFFFF'
                        fgColor='#000000'
                        level='L'
                    />
                )
            });
        }
        return enhanced;
    }, [columns, showQRCodes, qrCodeSize, handleQRScan]);

    return (
        <>
            <CrudTable<Student>
                data={data}
                loading={loading}
                error={error}
                columns={enhancedColumns}
                currentPage={currentPage}
                totalPages={totalPages}
                totalCount={totalCount}
                onPageChange={setCurrentPage}
                onRetry={retry}
                filtersConfig={showFilters ? filtersConfig : undefined}
                headerConfig={enhancedHeaderConfig}
                filters={filters}
                updateFilter={updateFilter}
                size={size}
                canEdit={canManageStudent}
                canDelete={canManageStudent}
                onEditItem={handleEditItem}
                onDeleteItem={handleDeleteItem}
                onCreateClick={handleCreateItem}
                createModal={
                    <CreateStudentModal
                        show={showCreateModal}
                        onCancel={handleCloseModals}
                        onSuccess={handleSuccess}
                    />
                }
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
                <QRCodesPrint ref={ contentRef } students={ data } />
            </div>
        </>
    );
}
