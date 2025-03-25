import { Form, Alert, Spinner, Card, Button } from 'react-bootstrap';
import { FormEvent, useState, useEffect } from 'react';
import { BehaviorFormData } from '../services/types';

interface BehaviorFormProps {
    onSubmit: (data: BehaviorFormData) => Promise<void>;
    studentID: number;
    studentName: string;
}

export default function BehaviorForm ({ onSubmit, studentID, studentName }: BehaviorFormProps) {
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState('');
    const [ formData, setFormData ] = useState<BehaviorFormData>({
        studentID: studentID,
        behaviors: {
            brilliant: false,
            excelled: false,
            answered: false,
            read: false,
            sensationalWriting: false
        },
        points: 0,
        notes: ''
    });

    useEffect(() => {
        setFormData(prev => ({
            ...prev,
            studentID: studentID
        }));
    }, [ studentID ]);

    useEffect(() => {
        const newPoints = Object.values(formData.behaviors).filter(Boolean).length;
        setFormData(prev => ({
            ...prev,
            points: newPoints
        }));
    }, [ formData.behaviors ]);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        try {
            setLoading(true);
            await onSubmit(formData);
            setFormData({
                studentID: studentID || -1,
                points: 0,
                behaviors: {
                    brilliant: false,
                    excelled: false,
                    answered: false,
                    read: false,
                    sensationalWriting: false
                },
                notes: ''
            });
        } catch (error) {
            setError('Submission failed');
            console.error('Submission failed with error:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <Card className='mb-4'>
                <Card.Body>
                    <Form onSubmit={ handleSubmit }>
                        <Form.Group className='mb-3' controlId='studentName'>
                            <Form.Control type='text' value={ studentName } disabled aria-label='Student name' />
                        </Form.Group>
                        <Form.Group className='mb-3' controlId='behaviors'>
                            <Form.Label>Behaviors</Form.Label>
                            <div className='d-flex flex-column flex-md-row gap-3'>
                                <Form.Check type='checkbox' label='Brilliant Behavior' checked={ formData.behaviors.brilliant } onChange={ (e) => setFormData({ ...formData, behaviors: { ...formData.behaviors,  brilliant: e.target.checked } }) } aria-label='Check student had brilliant behavior' />
                                <Form.Check type='checkbox' label='Excelled in Math' checked={ formData.behaviors.excelled } onChange={ (e) => setFormData({ ...formData, behaviors: { ...formData.behaviors, excelled: e.target.checked } }) } aria-label='Check student excelled in Math' />
                                <Form.Check type='checkbox' label='Answered and participated' checked={ formData.behaviors.answered } onChange={ (e) => setFormData({ ...formData, behaviors: { ...formData.behaviors, answered: e.target.checked } }) } aria-label='Check student answered and participated' />
                                <Form.Check type='checkbox' label='Read and thought carefully' checked={ formData.behaviors.read } onChange={ (e) => setFormData({ ...formData, behaviors: { ...formData.behaviors, read: e.target.checked } }) } aria-label='Check student read and thought carefully' />
                                <Form.Check type='checkbox' label='Sensational writing / Bear Time' checked={ formData.behaviors.sensationalWriting } onChange={ (e) => setFormData({ ...formData, behaviors: { ...formData.behaviors, sensationalWriting: e.target.checked } }) } aria-label='Check student had sensational writing and / or Bear Time' />
                            </div>
                        </Form.Group>
                        <Form.Group className='mb-3' controlId='notes'>
                            <Form.Label>Notes</Form.Label>
                            <Form.Control as='textarea' rows={ 3 } value={ formData.notes } onChange={ (e) => setFormData({ ...formData, notes: e.target.value }) } aria-label='Enter any notes to report' />
                        </Form.Group>
                        <div className='d-flex justify-content-between align-items-center'>
                            <div>
                                <strong>Points: { formData.points }</strong>
                            </div>
                            <Button variant='primary' type='submit' disabled={ loading } style={{ minWidth: '120px', minHeight: '48px' }}>
                                { loading ? <Spinner size='sm' /> : 'Submit' }
                            </Button>
                        </div>
                        { error && <Alert variant='danger' className='mt-3'>{ error }</Alert> }
                    </Form>
                </Card.Body>
            </Card>
        </>
    );
}
