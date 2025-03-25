import { Container, Row, Button, Col, Spinner, Alert, Form } from 'react-bootstrap';
import QRCodesPrint from '../components/QRCodesPrint';
import StudentTable from '../components/StudentTable';
import { useState, useEffect, useRef } from 'react';
import { useReactToPrint } from 'react-to-print';
import { useNavigate } from 'react-router-dom';
import { getStudents } from '../services/api';
import { Student } from '../services/types';

export default function StudentsPage () {
    const [ students, setStudents ] = useState<Student[]>([]);
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState('');
    const [ filter, setFilter ] = useState({
        teacher: '',
        nameSearch: '',
        idSearch: '',
        grade: ''
    });

    const navigate = useNavigate();

    const contentRef = useRef<HTMLDivElement>(null);
    const reactToPrintFn = useReactToPrint({ contentRef });

    useEffect(() => {
        const fetchStudents = async () => {
            try {
                setLoading(true);
                const data = await getStudents();
                setStudents(Array.isArray(data) ? data : []);
            } catch (error) {
                setError('Failed to load students');
                console.error('Failed to load students:', error);
                setStudents([]);
            } finally {
                setLoading(false);
            }
        };
        fetchStudents();
    }, []);

    const handleQRScan = (token: string) => {
        navigate(`/behavior?token=${token}`);
    };

    const teachers = Array.from(new Set(students.map(s => s.teacher)));

    const grades = Array.from(new Set(students.map(s => s.grade))).sort((a, b) => {
        if (a === 'Pre-K') return -1;
        if (b === 'Pre-K') return 1;
        if (a === 'K') return -1;
        if (b === 'K') return 1;
        return a.localeCompare(b);
    });

    const filteredStudents = students.filter(student =>
        student.teacher.toLowerCase().includes(filter.teacher.toLowerCase()) &&
        (filter.grade === '' || student.grade === filter.grade) &&
        student.name.toLowerCase().includes(filter.nameSearch.toLowerCase()) &&
            (filter.idSearch === '' || student.studentID.toString() === filter.idSearch)
    );

    return (
        <Container className='mt-5 mt-md-5'>
            <Row className='mb-4 justify-content-center'>
                <Col md={ 6 } className='text-center'>
                    <h1 className='mb-4'>Students</h1>
                </Col>
            </Row>
            <Row className='mb-3 g-3'>
                {/* ID Search */ }
                <Col md={ 6 }>
                    <Form.Control placeholder='Search by exact ID' value={ filter.idSearch } onChange={ (e) => setFilter({ ...filter, idSearch: e.target.value }) } />
                    <Form.Text className='text-muted'>Enter full student ID for exact match</Form.Text>
                </Col>
                {/* Name Search */ }
                <Col md={ 6 }>
                    <Form.Control placeholder='Search by name' value={ filter.nameSearch } onChange={ (e) => setFilter({ ...filter, nameSearch: e.target.value }) } />
                    <Form.Text className='text-muted'>Partial name matches accepted</Form.Text>
                </Col>
                {/* Teacher Filter */ }
                <Col md={ 6 }>
                    <Form.Select value={ filter.teacher } onChange={ (e) => setFilter({ ...filter, teacher: e.target.value }) }>
                        <option value=''>All Teachers</option>
                        { teachers.map(teacher => (
                            <option key={ teacher } value={ teacher }>{ teacher }</option>
                        )) }
                    </Form.Select>
                </Col>
                {/* Grade Filter */ }
                <Col md={ 6 }>
                    <Form.Select value={ filter.grade } onChange={ (e) => setFilter({ ...filter, grade: e.target.value }) }>
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
                        <Alert variant='info' className='mt-4'>No sudents found matching the current filters</Alert>
                    ) : (
                        <div className='border rounded-3 overflow-hidden'>
                            <div className='mb-2 mt-2'>Showing { filteredStudents.length } of { students.length } students</div>
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
    );
}
