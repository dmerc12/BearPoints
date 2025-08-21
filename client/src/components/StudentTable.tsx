import { Row, Table, Pagination, Spinner, Alert, Container, Button, Col, Form } from 'react-bootstrap';
import { formatName, fullName, clearNameCaches } from '../utils/formatNames';
import { useState, useMemo, useCallback, useEffect, useRef } from 'react';
import { useAppDispatch, useAppSelector } from '../store/hooks.ts';
import { fetchStudents } from '../store/slices/studentsSlice.ts';
import { formatGrade } from '../utils/formatGrades';
import { GradeLevel } from '../services/types.ts';
import { useReactToPrint } from 'react-to-print';
import { useNavigate } from 'react-router-dom';
import QRCodesPrint from './QRCodesPrint.tsx';
import { QRCodeSVG } from 'qrcode.react';

interface StudentTableProps {
    onQRScan?: (token: string) => void;
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
    const [ currentPage, setCurrentPage ] = useState(1);
    const [ filters, setFilters ] = useState({
        teacher: '',
        nameSearch: '',
        grade: ''
    });

    const navigate = useNavigate();
    const handleQRScan = (token: string) => {
        navigate(`/brag?token=${token}`);
    };

    const contentRef = useRef<HTMLDivElement>(null);
    const reactToPrintFn = useReactToPrint({ contentRef });

    useEffect(() => {
        clearNameCaches();
        dispatch(fetchStudents({ page: 0, size: 1000 }));
    }, [dispatch]);

    const filteredStudents = useMemo(() => {
        if (!students.length) return [];
        const teacherIdFilter = currentUser?.teacherId;
        const teacherFilter = filters.teacher.toLowerCase();
        const nameFilter = filters.nameSearch.toLowerCase();
        const gradeFilter = filters.grade;
        return students.filter(student => {
            const studentName = fullName(student).toLowerCase();
            const teacherName = fullName(student.teacher).toLowerCase();
            if (teacherIdFilter && student.teacher.id !== teacherIdFilter) {
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
    }, [students, currentUser, filters]);

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

    const totalPages = Math.ceil(filteredStudents.length / itemsPerPage);

    const paginatedStudents = useMemo(() => {
        if (filteredStudents.length === 0) return [];
        const startIndex = (currentPage - 1) * itemsPerPage;
        const endIndex = Math.min(startIndex + itemsPerPage, filteredStudents.length);
        return filteredStudents.slice(startIndex, endIndex);
    }, [filteredStudents, currentPage, itemsPerPage]);

    const updateNameFilter = useCallback((value: string) => {
        setFilters(prev => ({ ...prev, nameSearch: value }));
        setCurrentPage(1);
    }, []);

    const updateTeacherFilter = useCallback((value: string) => {
        setFilters(prev => ({ ...prev, teacher: value }));
        setCurrentPage(1);
    }, []);

    const updateGradeFilter = useCallback((value: string) => {
        setFilters(prev => ({ ...prev, grade: value }));
        setCurrentPage(1);
    }, []);

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
                <Pagination.Item key={1} active={1 === currentPage} onClick={() => setCurrentPage(1)}>
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
                    onClick={() => setCurrentPage(number)}
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
                    onClick={() => setCurrentPage(totalPages)}
                >
                    {totalPages}
                </Pagination.Item>
            );
        }
        return items;
    }

    const handlePrev = useCallback(() => {
        setCurrentPage(prev => Math.max(1, prev - 1));
    }, []);

    const handleNext = useCallback(() => {
        setCurrentPage(prev => Math.min(totalPages, prev + 1));
    }, [totalPages]);

    if (loading) {
        return (
            <Container className='d-flex justify-content-center align-items-center' style={{ minHeight: '200px' }}>
                <Spinner animation='border' variant='primary'>
                    <span className='visually-hidden'>Loading...</span>
                </Spinner>
                <span className='ms-2'>Loading students...</span>
            </Container>
        );
    }

    if (error) {
        return (
            <Alert variant='danger' className='text-center'>
                <Alert.Heading>Error Loading Students</Alert.Heading>
                <p>{error}</p>
                <Button variant='outline-danger' onClick={() =>
                    dispatch(fetchStudents({ page: 0, size: 1000, force: true }))}
                >
                    Retry
                </Button>
            </Alert>
        );
    }

    const renderFilters = () => {
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

    const qrCodeSize = useMemo(() => {
        if (size === 's') return 48;
        if (size === 'l') return window.innerWidth < 768 ? 80 : 100;
        return window.innerWidth < 768 ? 64 : 80
    }, [size]);

    return (
        <div className={`table-responsive ${size === 's' ? 'compact-table' : ''}`}>
            { showFilters && renderFilters() }
            <div className='m-2 d-flex justify-content-between align-items-center'>
                <span>Showing { filteredStudents.length } of { students.length } students</span>
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
            <Table striped bordered hover responsive
                className={`text-center align-middle ${size === 's' ? 'table-sm' : ''}`}
            >
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Grade</th>
                        <th>Teacher</th>
                        <th>Points</th>
                        { showQRCodes && (
                            <th>QR Code</th>
                        )}
                    </tr>
                </thead>
                <tbody>
                    { filteredStudents.length === 0 ? (
                        <tr>
                            <td colSpan={showQRCodes ? 5 : 4}>
                                <Alert variant='info' className='mt-4 mb-0'>
                                    No students found matching the current filters
                                </Alert>
                            </td>
                        </tr>
                    ) : (
                        paginatedStudents.map((student) => (
                            <tr key={ student.token }>
                                <td>{ fullName(student) }</td>
                                <td>{ formatGrade(student.teacher.grade) }</td>
                                <td>{ formatName(student.teacher) || 'N/A' }</td>
                                <td>{ student.points }</td>
                                { showQRCodes && (
                                    <td>
                                        <QRCodeSVG
                                            value={`${import.meta.env.VITE_APP_URL}/behavior?token=${student.token}`}
                                            size={ qrCodeSize }
                                            onClick={ () => handleQRScan(student.token) }
                                            bgColor='#FFFFFF'
                                            fgColor='#000000'
                                            level='L'
                                        />
                                    </td>
                                )}
                            </tr>
                        ))
                    )}
                </tbody>
            </Table>
            { totalPages > 1 && (
                <div className='d-flex justify-content-center mt-3'>
                    <Pagination className='flex-wrap'>
                        <Pagination.Prev onClick={handlePrev} disabled={currentPage === 1} />
                        {renderPaginationItems()}
                        <Pagination.Next onClick={handleNext} disabled={currentPage === totalPages} />
                    </Pagination>
                </div>
            )}
            <div style={{ display: 'none' }}>
                <QRCodesPrint ref={ contentRef } students={ filteredStudents } />
            </div>
        </div>
    );
}
