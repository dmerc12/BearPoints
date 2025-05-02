import { Container, Alert, Spinner, Row, Col } from 'react-bootstrap';
import { submitBehavior, getStudentByToken } from '../services/api';
import { BehaviorFormData, StudentToken } from '../services/types';
import BehaviorForm from '../components/BehaviorForm';
import { useSearchParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

export default function SubmitBehaviorPage () {
    const [ searchParams ] = useSearchParams();
    const [ success, setSuccess ] = useState(false);
    const [ loading, setLoading ] = useState(true);
    const [ student, setStudent ] = useState<StudentToken | null>(null);
    
    const token = searchParams.get('token');

    useEffect(() => {
        const fetchStudent = async () => {
            if (!token) return;
            try {
                const data = await getStudentByToken(token);
                setStudent(data);
            } catch (error) {
                toast.error('Failed to load student data')
                console.error('Error fetching student:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchStudent().catch(error => {
            console.error('Unhandled fetch error:', error);
            toast.error('An unexpected error occurred')
        });
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
            <BehaviorForm onSubmit={ handleSubmit } studentID={ student.studentID } teacherID={student.teacherID} grade={student.grade} studentName={ student.name } />
        </Container>
    );
}
