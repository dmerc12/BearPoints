import { Container, Row, Col, Spinner, Alert, Form } from 'react-bootstrap';
import StudentTable from '../components/StudentTable';
import { useNavigate } from 'react-router-dom';
import { getStudents } from '../services/api';
import { useState, useEffect } from 'react';
import { Student } from '../services/types';

export default function StudentsPage () {
    const [ students, setStudents ] = useState<Student[]>([]);
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState('');
    const [ filter, setFilter ] = useState({
        teacher: '',
        search: ''
    });

    const navigate = useNavigate();

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

    const handleQRScan = (studentID: number) => {
        navigate(`/behavior?studentID=${studentID}`);
    };

    const filteredStudents = Array.isArray(students)
        ? students.filter(student =>
        student.teacher.includes(filter.teacher) &&
        (student.name.toLowerCase().includes(filter.search.toLowerCase()) ||
            student.studentID.toString().includes(filter.search))
        ) : [];

    return (
        <Container className='mt-4'>
            <Row className='mb-3 g-3'>
                <Col md={ 6 }>
                    <Form.Control placeholder='Search by ID or Name' onChange={ (e) => setFilter({ ...filter, search: e.target.value }) } />
                </Col>
                <Col md={ 6 }>
                    <Form.Select value={ filter.teacher } onChange={ (e) => setFilter({ ...filter, teacher: e.target.value }) }>
                        <option value=''>All Teachers</option>
                    </Form.Select>
                </Col>
            </Row>
            { loading && <Spinner animation='border' /> }
            { error && <Alert variant='danger'>{ error }</Alert> }
            <StudentTable students={ filteredStudents } onQRScan={ handleQRScan } />
        </Container>
    );
}
