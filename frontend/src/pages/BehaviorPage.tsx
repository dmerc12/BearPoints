import BehaviorForm from '../components/BehaviorForm';
import { BehaviorFormData } from '../services/types';
import { useLocation } from 'react-router-dom';
import { Container, Alert } from 'react-bootstrap';
import { submitBehavior } from '../services/api';
import { useState } from 'react';

export default function SubmitBehaviorPage () {
    const location = useLocation();
    const [ success, setSuccess ] = useState(false);
    const { studentID, studentName } = location.state as {
        studentID: number;
        studentName: string;
    };
    
    if (!studentID || !studentName) {
        return (
            <Container className='mt-4'>
                <Alert variant='danger'>Invalid student information</Alert>
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
        <Container className='mt-4'>
            <h2 className='mb-4'>Behavior Report</h2>
            { success && <Alert variant='success'>Behavior report submitted successfully!</Alert> }
            <BehaviorForm onSubmit={ handleSubmit } studentID={ studentID } studentName={ studentName } />
        </Container>
    )
}
