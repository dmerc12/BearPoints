import { Container, Row, Button, Col, Spinner, Alert, Form } from 'react-bootstrap';
import { getStudents, getTeachers, getCurrentUser } from '../services/api';
import { Student, Teacher, UserDTO } from '../services/types';
import QRCodesPrint from '../components/QRCodesPrint';
import StudentTable from '../components/StudentTable';
import { useState, useEffect, useRef } from 'react';
import { useReactToPrint } from 'react-to-print';
import { useNavigate } from 'react-router-dom';
import Auth from '../components/Auth';

export default function StudentsPage () {
    const [ students, setStudents ] = useState<Student[]>([]);
    const [ teachers, setTeachers ] = useState<Teacher[]>([]);
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState('');
    const [ currentUser, setCurrentUser ] = useState<UserDTO | null>(null);
    const [ filter, setFilter ] = useState({
        teacher: '',
        nameSearch: '',
        grade: ''
    });

    const navigate = useNavigate();

    const contentRef = useRef<HTMLDivElement>(null);
    const reactToPrintFn = useReactToPrint({ contentRef });

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError('');
                const user = await getCurrentUser();
                setCurrentUser(user);
                const [studentsData, teachersData] = await Promise.all([
                    getStudents(),
                    getTeachers()
                ]);
                setStudents(studentsData);
                setTeachers(teachersData);
            } catch (error) {
                setError('Failed to load students');
                console.error('Failed to load students:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchData().catch(error => {
            console.error('Unhandled fetch error:', error);
            setError('An unexpected error occurred');
        });
    }, []);

    const handleQRScan = (token: string) => {
        navigate(`/brag?token=${token}`);
    };

    const teacherNames = Array.from(new Set(teachers.map(t => t.name.split(' ').pop() || t.name)));

    const grades = Array.from(new Set(teachers.map(t => t.grade))).sort((a, b) => {
        if (a === 'Pre-K') return -1;
        if (b === 'Pre-K') return 1;
        if (a === 'K') return -1;
        if (b === 'K') return 1;
        return a.localeCompare(b);
    });

    const filteredStudents = students
        .filter(student =>
            !currentUser?.teacherId || student.teacher.id === currentUser.teacherId)
        .filter(student => {
            const teacherMatch = filter.teacher === '' ||
                student.teacher.name.toLowerCase().includes(filter.teacher.toLowerCase());
            const gradeMatch = filter.grade === '' || student.grade === filter.grade;
            const nameMatch = student.name.toLowerCase().includes(filter.nameSearch.toLowerCase());
            return teacherMatch && gradeMatch && nameMatch;
        });

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
                                          setFilter({ ...filter, nameSearch: e.target.value }) }
                        />
                        <Form.Text className='text-muted'>Partial name matches accepted</Form.Text>
                    </Col>
                    {/* Teacher Filter */ }
                    <Col md={ 6 }>
                        <Form.Select value={ filter.teacher }
                                     onChange={ (e) =>
                                         setFilter({ ...filter, teacher: e.target.value }) }
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
                                         setFilter({ ...filter, grade: e.target.value }) }
                        >
                            <option value=''>All Grades</option>
                            { grades.map(grade => (
                                <option key={ grade } value={ grade }>{ grade }</option>
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
                            <Alert variant='info' className='mt-4'>No students found matching the current filters</Alert>
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
                <QRCodesPrint ref={ contentRef } students={ filteredStudents } />
            </Container>
        </Auth>
    );
}
