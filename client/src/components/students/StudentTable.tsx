import { 
    CreateStudentModal, EditStudentModal, DeleteStudentModal, BaseTable, TableColumn, TextFilter, SelectFilter 
} from '../index';
import {formatName, fullName, clearNameCaches, formatGrade, sortGrades} from '../../utils';
import { useMemo, useCallback, useEffect, useRef } from 'react';
import { Row, Button, Col, ButtonGroup } from 'react-bootstrap';
import { useReactToPrint } from 'react-to-print';
import { useNavigate } from 'react-router-dom';
import { Student, Role } from '../../services';
import { useStudentTable } from '../../hooks';
import { useAppSelector } from '../../store';
import QRCodesPrint from './QRCodesPrint';
import { QRCodeSVG } from 'qrcode.react';

interface StudentTableProps {
    itemsPerPage?: number;
    showFilters?: boolean;
    showPrintButton?: boolean;
    showQRCodes?: boolean;
    size?: 's' | 'm' | 'l';
}

export default function StudentTable ({ itemsPerPage = 10, showFilters = true, showPrintButton = true,
                                          showQRCodes = true, size = 'm' }: StudentTableProps) {
    const currentUser = useAppSelector(state => state.user.data);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);
    const isTeacher = useMemo(() => currentUser?.role === Role.TEACHER, [currentUser]);
    const canManageStudent = useCallback((student: Student) => {
        return isAdmin || (isTeacher && student.teacher.id === currentUser?.teacherId);
    }, [isAdmin, isTeacher, currentUser]);

    const {
        data: students, loading, error, filters, updateFilter, resetFilters,
        showCreateModal, editingItem: editingStudent, deletingItem: deletingStudent,
        handleCreateItem: handleCreateStudent, handleEditItem: handleEditStudent,
        handleDeleteItem: handleDeleteStudent, handleCloseModals, retryFetch, handleSuccess,
    } = useStudentTable();

    const navigate = useNavigate();
    const contentRef = useRef<HTMLDivElement>(null);
    const reactToPrintFn = useReactToPrint({ contentRef });

    useEffect(() => {
        clearNameCaches();
        return () => {
            resetFilters();
        }
    }, [resetFilters]);

    const filteredStudents = useMemo(() => {
        if (!students.length) return [];
        const teacherIdFilter = currentUser?.teacherId;
        const teacherFilter = filters.teacherFilter.toLowerCase();
        const nameFilter = filters.nameSearch.toLowerCase();
        const gradeFilter = filters.gradeFilter;
        return students.filter(student => {
            const studentName = fullName(student).toLowerCase();
            const teacherName = fullName(student.teacher).toLowerCase();
            if (teacherIdFilter && student.teacher.id !== teacherIdFilter && !isAdmin) {
                return false;
            }
            if (teacherFilter && !teacherName.includes(teacherFilter)) {
                return false;
            }
            if (gradeFilter && student.teacher.grade !== gradeFilter) {
                return false;
            }
            return !(nameFilter && !studentName.includes(nameFilter));
        });
    }, [students, currentUser, filters, isAdmin]);

    const teacherNames = useMemo(() => {
        const teachers = students.map(student => student.teacher);
        return Array.from(new Set(teachers.map(t => formatName(t))));
    }, [students]);

    const grades = useMemo(() => {
        const teacherGrades = students.map(student => student.teacher.grade);
        return Array.from(new Set(teacherGrades));
    }, [students]);

    const qrCodeSize = useMemo(() => {
        if (size === 's') return 48;
        if (size === 'l') return window.innerWidth < 768 ? 80 : 100;
        return window.innerWidth < 768 ? 64 : 80
    }, [size]);

    const handleQRScan = useCallback((token: string) => {
        navigate(`/brag?token=${token}`);
    }, [navigate]);
    
    const columns: TableColumn<Student>[] = useMemo(() => [
        {
            key: 'name',
            header: 'Name',
            render: (student: Student) => fullName(student)
        },
        {
            key: 'grade',
            header: 'Grade',
            render: (student: Student) => formatGrade(student.teacher.grade)
        },
        {
            key: 'teacher',
            header: 'Teacher',
            render: (student: Student) => formatName(student.teacher) || 'N/A'
        },
        {
            key: 'points',
            header: 'Points',
            render: (student: Student) => student.points.toString()
        },
        ...(showQRCodes ? [{
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
        }] : []),
        ...((isAdmin || isTeacher) ? [{
            key: 'actions',
            header: 'Actions',
            render: (student: Student) => (
                canManageStudent(student) ? (
                    <ButtonGroup size='sm'>
                        <Button variant='outline-primary'
                                onClick={() => handleEditStudent(student)}
                        >
                            Edit
                        </Button>
                        <Button variant='danger'
                                onClick={() => handleDeleteStudent(student)}
                        >
                            Delete
                        </Button>
                    </ButtonGroup>
                ) : null
            )
        }] : [])
    ], [showQRCodes, qrCodeSize, handleQRScan, isAdmin, isTeacher, canManageStudent,
        handleEditStudent, handleDeleteStudent]
    );
    
    const renderFilters = () => {
        if (!showFilters) return null;
        const teacherOptions = teacherNames.map(teacher => (
            { value: teacher, label: teacher }
        ));
        const sortedGrades = sortGrades(grades);
        const gradeOptions = sortedGrades.map(grade => (
            { value: grade, label: formatGrade(grade) }
        ));
        return (
            <Row className='mb-3 g-3'>
                <Col md={ 6 }>
                    <TextFilter
                        value={filters.nameSearch}
                        onChange={(value) => updateFilter('nameSearch', value)}
                        label='Search Students'
                        placeholder='Search by name...'
                    />
                </Col>
                <Col md={ 6 }>
                    <SelectFilter
                        value={filters.teacherFilter}
                        onChange={(value) => updateFilter('teacherFilter', value)}
                        label='Teacher'
                        options={teacherOptions}
                    />
                </Col>
                <Col md={ 6 }>
                    <SelectFilter
                        value={filters.gradeFilter}
                        onChange={(value) => updateFilter('gradeFilter', value)}
                        label='Grade'
                        options={gradeOptions}
                    />
                </Col>
            </Row>
        );
    };
    
    const renderHeader = () => {
        return (
            <div className='m-2 d-flex justify-content-between align-items-center'>
                <span>Showing { filteredStudents.length } of { students.length } students</span>
                { (isAdmin || isTeacher) && (
                    <Button variant='primary'
                            onClick={handleCreateStudent}
                            className='me-2'
                            size={size === 's' ? 'sm' : undefined}
                    >
                        Create Student
                    </Button>
                )}
                { showPrintButton && filteredStudents.length > 0 && (
                    <Button variant='primary' 
                            onClick={() => reactToPrintFn()} 
                            className='no-print' 
                            size={size === 's' ? 'sm' : undefined}
                    >
                        Print QR Codes ({filteredStudents.length})
                    </Button>
                )}
            </div>
        );
    };

    return (
        <>
            <BaseTable<Student> 
                data={filteredStudents} 
                loading={loading}
                error={error}
                columns={columns} 
                itemsPerPage={itemsPerPage}
                renderFilters={renderFilters}
                renderHeader={renderHeader}
                onRetry={retryFetch}
                size={size}
            />
            <CreateStudentModal
                show={showCreateModal}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
            <EditStudentModal 
                show={!!editingStudent}
                student={editingStudent}
                onCancel={handleCloseModals}
                onSuccess={handleSuccess}
            />
            <DeleteStudentModal 
                show={!!deletingStudent} 
                student={deletingStudent} 
                onCancel={handleCloseModals} 
                onSuccess={handleSuccess}
            />
            <div style={{ display: 'none' }}>
                <QRCodesPrint ref={ contentRef } students={ filteredStudents } />
            </div>
        </>
    );
}
