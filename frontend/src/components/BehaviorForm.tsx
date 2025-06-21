import { BragLogRequest, BehaviorType, Student } from '../services/types';
import { Form, Alert, Spinner, Card, Button } from 'react-bootstrap';
import { FormEvent, useState } from 'react';

interface BehaviorFormProps {
    onSubmit: (data: BragLogRequest) => Promise<void>;
    student: Student;
    behaviorTypes: BehaviorType[];
}

export default function BehaviorForm ({ onSubmit, student, behaviorTypes }: BehaviorFormProps) {
    const [ loading, setLoading ] = useState(false);
    const [ error, setError ] = useState('');
    const [ selectedBehaviorIds, setSelectedBehaviorIds ] = useState<number[]>([]);
    const [ notes, setNotes ] = useState('');

    const points = selectedBehaviorIds.reduce((total, id) => {
        const behavior = behaviorTypes.find(bt => bt.id === id);
        return total + (behavior?.pointValue || 0);
    }, 0);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        try {
            setLoading(true);
            await onSubmit({
                studentId: student.id,
                teacherId: student.teacher.id,
                behaviorIds: selectedBehaviorIds,
                notes
            });
            setSelectedBehaviorIds([]);
            setNotes('')
        } catch (error) {
            setError('Submission failed');
            console.error('Submission failed with error:', error);
        } finally {
            setLoading(false);
        }
    };

    const toggleBehavior = (id: number) => {
        setSelectedBehaviorIds(prev =>
            prev.includes(id)
                ? prev.filter(behaviorId => behaviorId !== id)
                : [...prev, id]
        );
    };

    return (
        <>
            <Card className='mb-4'>
                <Card.Body>
                    <Form onSubmit={ handleSubmit }>
                        <Form.Group className='mb-3' controlId='studentName'>
                            <Form.Control type='text' value={ student.name } disabled aria-label='Student name' />
                        </Form.Group>
                        <Form.Group className='mb-3' controlId='behaviors'>
                            <Form.Label>Behaviors</Form.Label>
                            <div className='d-flex flex-column flex-md-row flex-wrap gap-3'>
                                { behaviorTypes.filter(bt => bt.active)
                                    .map(behavior => (
                                        <Form.Check key={behavior.id} type='checkbox'
                                                    label={`behavior.name (${behavior.pointValue} pts)`}
                                                    checked={selectedBehaviorIds.includes(behavior.id)}
                                                    onChange={() => toggleBehavior(behavior.id)}
                                                    aria-label={`Select ${behavior.name}`}
                                        />
                                    ))
                                }
                            </div>
                        </Form.Group>
                        <Form.Group className='mb-3' controlId='notes'>
                            <Form.Label>Notes</Form.Label>
                            <Form.Control as='textarea' rows={ 3 } value={ notes }
                                          onChange={ (e) =>
                                              setNotes(e.target.value )
                                          } aria-label='Enter any notes to report'
                            />
                        </Form.Group>
                        <div className='d-flex justify-content-between align-items-center'>
                            <div>
                                <strong>Points: { points }</strong>
                            </div>
                            <Button variant='primary' type='submit'
                                    disabled={ loading || selectedBehaviorIds.length === 0 }
                                    style={{ minWidth: '120px', minHeight: '48px' }}
                            >
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
