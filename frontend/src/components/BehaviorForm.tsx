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
            respectful: false,
            responsible: false,
            safe: false,
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
                    responsible: false,
                    respectful: false,
                    safe: false
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
                        <Form.Group className='mb-3'>
                            <Form.Control type='text' value={ studentName } disabled />
                        </Form.Group>
                        <Form.Group className='mb-3'>
                            <Form.Label>Behaviors</Form.Label>
                            <div className='d-flex gap-3'>
                                { Object.entries(formData.behaviors).map(([ behavior, checked ]) => (
                                    <Form.Check key={ behavior } type='checkbox' label={ behavior.charAt(0).toUpperCase() + behavior.slice(1) } checked={ checked } onChange={ (e) => setFormData({ ...formData, behaviors: { ...formData.behaviors, [ behavior ]: e.target.checked } }) } />
                                )) }
                            </div>
                        </Form.Group>
                        <Form.Group className='mb-3'>
                            <Form.Label>Notes</Form.Label>
                            <Form.Control as='textarea' rows={ 3 } value={ formData.notes } onChange={ (e) => setFormData({ ...formData, notes: e.target.value }) } />
                        </Form.Group>
                        <div className='d-flex justify-content-between align-items-center'>
                            <div>
                                <strong>Points: { formData.points }</strong>
                            </div>
                            <Button variant='primary' type='submit' disabled={ loading }>
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
