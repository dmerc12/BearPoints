import { Container, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { submitBehavior, getStudentByToken } from '../services/api';
import BehaviorForm from '../components/BehaviorForm';
import { BehaviorFormData } from '../services/types';
import { useSearchParams } from 'react-router-dom';
import { Student } from '../services/types';
import { useEffect, useState } from 'react';

export default function SubmitBehaviorPage () {
    const [ searchParams ] = useSearchParams();
    const [ success, setSuccess ] = useState(false);
    const [ loading, setLoading ] = useState(true);
    const [ student, setStudent ] = useState<Student | null>(null);
    
    const token = searchParams.get('token');

    useEffect(() => {
        const fetchStudent = async () => {
            if (!token) return;
            try {
                const data = await getStudentByToken(token);
                setStudent(data);
            } catch (error) {
                console.error('Error fetching student:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchStudent();
    }, [ token ]);

    if (loading) {
        return (
            <Container className='mt-4'>
                <Spinner animation='border' />
            </Container>
        );
    }
    
    if (!student) {
        return (
            <Container className='mt-4'>
                <Alert variant='danger'>Invalid or expired QR code</Alert>
            </Container>
        );
    }

    const handleSubmit = async (data: BehaviorFormData) => {
        try {
            await submitBehavior(data);
            setSuccess(true);
            setTimeout(() => setSuccess(false), 3000);
        } catch (error) {
            throw new Error(error instanceof Error ? error.message : 'Failed to submit behavior report');
        }
    };

    return (
        <Container className='mt-3 pt-2 mb-4'>
            <Row className='mb-4 justify-content-center'>
                <Col className='text-center'>
                    <h1>Behavior Report</h1>
                </Col>
            </Row>
            { success && <Alert variant='success'>Behavior report submitted successfully!</Alert> }
            <BehaviorForm onSubmit={ handleSubmit } studentID={ student.studentID } studentName={ student.name } />
        </Container>
    );
}
