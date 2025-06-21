import { getActiveBehaviorTypes, submitPublicBragLog, getStudentByToken }
    from '../services/api';
import { Container, Alert, Spinner, Row, Col } from 'react-bootstrap';
import {BehaviorType, BragLogRequest, Student} from '../services/types';
import BehaviorForm from '../components/BehaviorForm';
import { useSearchParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

export default function SubmitBehaviorPage () {
    const [ searchParams ] = useSearchParams();
    const [ success, setSuccess ] = useState(false);
    const [ loading, setLoading ] = useState(true);
    const [ student, setStudent ] = useState<Student | null>(null);
    const [ behaviorTypes, setBehaviorTypes ] = useState<BehaviorType[]>([]);
    
    const token = searchParams.get('token');

    useEffect(() => {
        const fetchData = async () => {
            if (!token) return;
            try {
                setLoading(true);
                const [studentData, behaviorData] = await Promise.all([
                    getStudentByToken(token),
                    getActiveBehaviorTypes()
                ]);
                setStudent(studentData);
                setBehaviorTypes(behaviorData);
            } catch (error) {
                toast.error('Failed to load student data')
                console.error('Error fetching student:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchData().catch(error => {
            console.error('Unhandled fetch error:', error);
            toast.error('An unexpected error occurred')
        });
    }, [ token ]);

    const handleSubmit = async (data: BragLogRequest) => {
        try {
            await submitPublicBragLog(data);
            setSuccess(true);
            setTimeout(() => setSuccess(false), 3000);
        } catch (error) {
            throw new Error('Failed to submit behavior report: ' + error);
        }
    };

    if (loading) {
        return (
            <Container className='mt-4'>
                <Spinner animation='border' />
                <p>Loading student data...</p>
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

    return (
        <Container className='mt-3 pt-2 mb-4'>
            <Row className='mb-4 justify-content-center'>
                <Col className='text-center'>
                    <h1>Bear Brag</h1>
                </Col>
            </Row>
            { success &&
                <Alert variant='success' className='mb-4'>
                    Bear brag submitted successfully!
                </Alert>
            }
            <BehaviorForm onSubmit={ handleSubmit } student={ student } behaviorTypes={behaviorTypes} />
        </Container>
    );
}
