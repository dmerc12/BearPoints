import { Form, Alert, Spinner, Card, Button } from 'react-bootstrap';
import { FormEvent, useState, useEffect } from 'react';
import { BehaviorFormData } from '../services/types';
import { Scanner } from '@yudiel/react-qr-scanner';

interface BehaviorFormProps {
    onSubmit: (data: BehaviorFormData) => Promise<void>;
    initialStudentID?: number;
}

export default function BehaviorForm ({ onSubmit }: BehaviorFormProps) {
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState('');
    const [ formData, setFormData ] = useState<BehaviorFormData>({
        studentID: -1,
        behaviors: {
            respectful: false,
            responsible: false,
            safe: false,
        },
        points: 0,
        notes: ''
    });

    useEffect(() => {
        const newPoints = Object.values(formData.behaviors).filter(Boolean).length;
        setFormData(prev => ({
            ...prev,
            points: newPoints
        }));
    }, [ formData.behaviors ]);

    const handleScan = (result: string) => {
        try {
            const data = JSON.parse(result);
            if (data.studentID) {
                setFormData(prev => ({
                    ...prev,
                    studentID: Number(data.studentID)
                }));
            }
        } catch (error) {
            setError('Invalid QR code format');
            console.error('Invalid QR code error:', error);
        }
    };

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        try {
            setLoading(true);
            await onSubmit(formData);
            setFormData({
                studentID: -1,
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
                    <div className='mb-4' style={ { maxWidth: '500px' } }>
                        <Scanner onScan={ (results) => { if (results?.[ 0 ]?.rawValue) { handleScan(results[ 0 ].rawValue); } } } onError={ (error) => setError(error instanceof Error ? error.message: String(error)) } constraints={{ facingMode: 'environment' }} />
                    </div>
                    <Form onSubmit={ handleSubmit }>
                        <Form.Group className='mb-3'>
                            <Form.Control type='number' value={ formData.studentID } onChange={ (e) => setFormData({ ...formData, studentID: Number(e.target.value) || -1 }) } required />
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
    )

}
