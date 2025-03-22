import BehaviorForm from '../components/BehaviorForm';
import { BehaviorFormData } from '../services/types';
import { useSearchParams } from 'react-router-dom';
import { Container, Alert } from 'react-bootstrap';
import { submitBehavior } from '../services/api';
import { useState, useEffect } from 'react';

export default function SubmitBehaviorPage () {
    const [ searchParams ] = useSearchParams();
    const [ success, setSuccess ] = useState(false);
    const [ initialStudentID, setInitialStudentID ] = useState<number>();

    useEffect(() => {
        const studentID = Number(searchParams.get('studentID'));
        if (studentID) setInitialStudentID(studentID);
    }, [ searchParams ]);

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
            <BehaviorForm onSubmit={handleSubmit} initialStudentID={initialStudentID} />
        </Container>
    )
}
