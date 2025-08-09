import { Container, Row, Button, Col, Spinner, Alert, Form } from 'react-bootstrap';
import { formatName, fullName, clearNameCaches } from '../utils/formatNames';
import { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { Student, Teacher, GradeLevel } from '../services/types';
import { getStudents, getTeachers } from '../services/api';
import QRCodesPrint from '../components/QRCodesPrint';
import StudentTable from '../components/StudentTable';
import { formatGrade} from '../utils/formatGrades';
import { useReactToPrint } from 'react-to-print';
import { useAppSelector } from '../store/hooks';
import { useNavigate } from 'react-router-dom';
import Auth from '../components/Auth';

export default function StudentsPage () {
    const [ students, setStudents ] = useState<Student[]>([]);
    const [ teachers, setTeachers ] = useState<Teacher[]>([]);
    const [ loading, setLoading ] = useState(true);
    const [ error, setError ] = useState('');
    const [ filter, setFilter ] = useState({
        teacher: '',
        nameSearch: '',
        grade: ''
    });

    const currentUser = useAppSelector((state) => state.user.data);

    const navigate = useNavigate();

    const contentRef = useRef<HTMLDivElement>(null);
    const reactToPrintFn = useReactToPrint({ contentRef });

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError('');
                clearNameCaches();
                const [studentsData, teachersData] = await Promise.all([
                    getStudents(),
                    getTeachers()
                ]);
                setStudents(studentsData);
                setTeachers(teachersData);
            } catch (error) {
                setError('Failed to load students. Please try again later');
                console.error('Failed to load students:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchData().catch(error => {
            console.error('Unhandled fetch error:', error);
            setError('An unexpected error occurred. Please try again later');
        });
    }, []);

    useEffect(() => {
        setFilter({
            teacher: '',
            nameSearch: '',
            grade: ''
        })
    }, [students]);

    const updateNameFilter = useCallback((value: string) => {
        setFilter(prev => ({ ...prev, nameSearch: value }));
    }, []);

    const updateTeacherFilter = useCallback((value: string) => {
        setFilter(prev => ({ ...prev, teacher: value }));
    }, []);

    const updateGradeFilter = useCallback((value: string) => {
        setFilter(prev => ({ ...prev, grade: value }));
    }, []);

    const teacherNames = useMemo(() => {
        return Array.from(new Set(teachers.map(t => formatName(t))));
    }, [teachers]);

    const grades = useMemo(() => {
        return Array.from(new Set(teachers.map(t => t.grade)))
            .sort((a, b) => {
                if (a === GradeLevel.PRE_K) return -1;
                if (b === GradeLevel.PRE_K) return 1;
                if (a === GradeLevel.K) return -1;
                if (b === GradeLevel.K) return 1;
                return a.localeCompare(b);
            });
    }, [teachers]);

    const filteredStudents = useMemo(() => {
        if (!students.length) return [];
        const teacherIdFilter = currentUser?.teacherId;
        const teacherFilter = filter.teacher.toLowerCase();
        const nameFilter = filter.nameSearch.toLowerCase();
        const gradeFilter = filter.grade;
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
    }, [students, currentUser, filter]);

    const handleQRScan = (token: string) => {
        navigate(`/brag?token=${token}`);
    };

    return (
        <Auth>
            <Container className='mt-3 pt-2 mb-4'>
                <Row className='mb-4 justify-content-center'>
                    <Col md={ 6 } className='text-center'>
                        <h1 className='mb-4'>Students</h1>
                    </Col>
                </Row>
                <Row className='mb-3 g-3'>
                    {/* Name Search */ }
                    <Col md={ 6 }>
                        <Form.Control placeholder='Search by name'
                                      value={ filter.nameSearch }
                                      onChange={ (e) =>
                                          updateNameFilter(e.target.value) }
                        />
                        <Form.Text className='text-muted'>Partial name matches accepted</Form.Text>
                    </Col>
                    {/* Teacher Filter */ }
                    <Col md={ 6 }>
                        <Form.Select value={ filter.teacher }
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
                        <Form.Select value={ filter.grade }
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
                {/* Loading and Error States */ }
                { loading && (
                    <div className='text-center my-4'>
                        <Spinner animation='border' role='status'>
                            <span className='visually-hidden'>Loading...</span>
                        </Spinner>
                        <p>Loading students...</p>
                    </div>
                ) }
                { error && <Alert variant='danger'>{ error }</Alert> }
                {/* Results Section */ }
                { !loading && !error && (
                    <>
                        { filteredStudents.length === 0 ? (
                            <Alert variant='info' className='mt-4'>
                                No students found matching the current filters
                            </Alert>
                        ) : (
                            <div className='border rounded-3 overflow-hidden'>
                                <div className='m-2'>
                                    Showing { filteredStudents.length } of { students.length } students
                                </div>
                                <StudentTable students={ filteredStudents } onQRScan={ handleQRScan } />
                            </div>
                        ) }
                    </>
                ) }
                {/* Print QR codes button */ }
                {!loading && !error && filteredStudents.length > 0 && (
                    <Row className='mt-4'>
                        <Col className='text-center'>
                            <Button
                                variant='primary'
                                onClick={ () => reactToPrintFn() }
                                disabled={ filteredStudents.length === 0 }
                                className='no-print'
                            >
                                Print QR Codes ({ filteredStudents.length })
                            </Button>
                        </Col>
                    </Row>
                )}
                <QRCodesPrint ref={ contentRef } students={ filteredStudents } />
            </Container>
        </Auth>
    );
}
