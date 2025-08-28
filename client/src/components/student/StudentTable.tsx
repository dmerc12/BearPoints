import { formatName, fullName, clearNameCaches } from '../../utils/formatNames';
import { useState, useMemo, useCallback, useEffect, useRef } from 'react';
import { Row, Button, Col, Form, ButtonGroup } from 'react-bootstrap';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { fetchStudents } from '../../store/slices/studentsSlice';
import { GradeLevel, Student, Role } from '../../services/types';
import { CreateStudentModal } from './CreateStudentModal';
import { DeleteStudentModal } from './DeleteStudentModal';
import { formatGrade } from '../../utils/formatGrades';
import { EditStudentModal } from './EditStudentModal';
import BaseTable, { TableColumn } from '../BaseTable';
import { useReactToPrint } from 'react-to-print';
import { useNavigate } from 'react-router-dom';
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
    const dispatch = useAppDispatch();
    const { students, loading, error } = useAppSelector(state => state.students);
    const currentUser = useAppSelector(state => state.user.data);
    const [ filters, setFilters ] = useState({
        teacher: '',
        nameSearch: '',
        grade: ''
    });

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingStudent, setEditingStudent] = useState<Student | null>(null);
    const [deletingStudent, setDeletingStudent] = useState<Student | null>(null);

    const navigate = useNavigate();
    const contentRef = useRef<HTMLDivElement>(null);
    const reactToPrintFn = useReactToPrint({ contentRef });

    useEffect(() => {
        clearNameCaches();
        dispatch(fetchStudents({ page: 0, size: 1000 }));
    }, [dispatch]);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);
    const isTeacher = useMemo(() => currentUser?.role === Role.TEACHER, [currentUser]);

    const canManageStudent = useCallback((student: Student) => {
        return isAdmin || (isTeacher && student.teacher.id === currentUser?.teacherId);
    }, [isAdmin, isTeacher, currentUser]);

    const filteredStudents = useMemo(() => {
        if (!students.length) return [];
        const teacherIdFilter = currentUser?.teacherId;
        const teacherFilter = filters.teacher.toLowerCase();
        const nameFilter = filters.nameSearch.toLowerCase();
        const gradeFilter = filters.grade;
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
        return Array.from(new Set(teacherGrades))
            .sort((a, b) => {
                if (a === GradeLevel.PRE_K) return -1;
                if (b === GradeLevel.PRE_K) return 1;
                if (a === GradeLevel.K) return -1;
                if (b === GradeLevel.K) return 1;
                return a.localeCompare(b);
            });
    }, [students]);

    const qrCodeSize = useMemo(() => {
        if (size === 's') return 48;
        if (size === 'l') return window.innerWidth < 768 ? 80 : 100;
        return window.innerWidth < 768 ? 64 : 80
    }, [size]);

    const handleQRScan = useCallback((token: string) => {
        navigate(`/brag?token=${token}`);
    }, [navigate]);

    const handleCreateStudent = useCallback(() => {
        setShowCreateModal(true);
    }, []);

    const handleEditStudent = useCallback((student: Student) => {
        setEditingStudent(student);
    }, []);

    const handleDeleteStudent = useCallback((student: Student) => {
        setDeletingStudent(student);
    }, []);

    const handleCloseModals = useCallback(() => {
        setShowCreateModal(false);
        setEditingStudent(null);
        setDeletingStudent(null);
    }, []);

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
    
    const updateNameFilter = useCallback((value: string) => {
        setFilters(prev => ({ ...prev, nameSearch: value }));
    }, []);

    const updateTeacherFilter = useCallback((value: string) => {
        setFilters(prev => ({ ...prev, teacher: value }));
    }, []);

    const updateGradeFilter = useCallback((value: string) => {
        setFilters(prev => ({ ...prev, grade: value }));
    }, []);
    
    const renderFilters = () => {
        if (!showFilters) return null;
        return (
            <Row className='mb-3 g-3'>
                {/* Name Search */ }
                <Col md={ 6 }>
                    <Form.Control placeholder='Search by name'
                                  value={ filters.nameSearch }
                                  onChange={ (e) =>
                                      updateNameFilter(e.target.value) }
                    />
                    <Form.Text className='text-muted'>Partial name matches accepted</Form.Text>
                </Col>
                {/* Teacher Filter */ }
                <Col md={ 6 }>
                    <Form.Select value={ filters.teacher }
                                 onChange={ (e) =>
                                     updateTeacherFilter(e.target.value) }
                    >
                        <option value=''>All Teachers</option>
                        { teacherNames.map(teacher => (
                            <option key={ teacher } value={ teacher }>{ teacher }</option>
                        )) }
                    </Form.Select>
                </Col>
                {/* Grade Filter */ }
                <Col md={ 6 }>
                    <Form.Select value={ filters.grade }
                                 onChange={ (e) =>
                                     updateGradeFilter(e.target.value) }
                    >
                        <option value=''>All Grades</option>
                        { grades.map(grade => (
                            <option key={ grade } value={ grade }>{ formatGrade(grade) }</option>
                        )) }
                    </Form.Select>
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
                onRetry={() => dispatch(fetchStudents({ page: 0, size: 1000, force: true }))}
                size={size}
            />
            <CreateStudentModal
                show={showCreateModal}
                onCancel={handleCloseModals}
                onSuccess={() => {
                    handleCloseModals();
                    dispatch(fetchStudents({ page: 0, size: 1000, force: true }));
                }}
            />
            <EditStudentModal 
                show={!!editingStudent}
                student={editingStudent}
                onCancel={handleCloseModals}
                onSuccess={() => {
                    handleCloseModals();
                    dispatch(fetchStudents({ page: 0, size: 1000, force: true }));
                }}
            />
            <DeleteStudentModal 
                show={!!deletingStudent} 
                student={deletingStudent} 
                onCancel={handleCloseModals} 
                onSuccess={() => {
                    handleCloseModals();
                    dispatch(fetchStudents({ page: 0, size: 1000, force: true }));
                }}
            />
            <div style={{ display: 'none' }}>
                <QRCodesPrint ref={ contentRef } students={ filteredStudents } />
            </div>
        </>
    );
}
