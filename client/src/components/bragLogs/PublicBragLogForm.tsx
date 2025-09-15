import {Form, Alert, Spinner, Card, Button, Container} from 'react-bootstrap';
import { useAppDispatch, addPublicBragLog } from '../../store';
import { fullName, PublicBragLogFormData } from '../../utils';
import { useBragLogForm } from '../../hooks';
import { FormEvent, useState } from 'react';
import { BragLogForm } from '../index';

interface BehaviorFormProps {
    studentToken: string;
}

export default function PublicBragLogForm ({ studentToken }: BehaviorFormProps) {
    const dispatch = useAppDispatch();

    const [ success, setSuccess ] = useState(false);

    const { formData, formErrors, loading, error, behaviorTypes, handleInputChange, toggleBehavior,
        publicStudent, resetForm } = useBragLogForm({ show: true, isPublic: true, studentToken });

    if (!publicStudent) {
        return (
            <Alert variant='danger' className='mb-4'>
                Student not found. Please check your link.
            </Alert>
        );
    }

    const points = formData.behaviorIds.reduce((total, id) => {
        const behavior = behaviorTypes.find(bt => bt.id.toString() === id);
        return total + (behavior?.pointValue || 0);
    }, 0);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        try {
            const bragLogData: PublicBragLogFormData = {
                studentId: publicStudent.id,
                teacherId: publicStudent.teacher.id,
                behaviorIds: formData.behaviorIds.map(id => parseInt(id)),
                notes: formData.notes
            };
            const result = await dispatch(addPublicBragLog(bragLogData));
            if (addPublicBragLog.fulfilled.match(result)) {
                resetForm();
                setSuccess(true);
                const timer = setTimeout(() => {
                    setSuccess(false);
                }, 3000);
                return () => clearTimeout(timer);
            }
        } catch (error) {
            console.error('Submission failed with error:', error);
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

    if (!publicStudent) {
        return (
            <Container className='mt-4'>
                <Alert variant='danger'>Invalid or expired QR code</Alert>
            </Container>
        );
    }

    return (
        <Card className='mb-4'>
            <Card.Header>
                <h3>Submit Bear Brag for {fullName(publicStudent)}</h3>
            </Card.Header>
            <Card.Body>
                { success &&
                    <Alert variant='success' className='mb-4'>
                        Bear brag submitted successfully!
                    </Alert>
                }
                <Form onSubmit={ handleSubmit }>
                    <BragLogForm
                        formData={formData}
                        formErrors={formErrors}
                        loading={loading}
                        isAdmin={false}
                        students={[publicStudent]}
                        teachers={[publicStudent.teacher]}
                        behaviorTypes={behaviorTypes}
                        onInputChange={handleInputChange}
                        onSelectChange={() => {}}
                        onToggleBehavior={toggleBehavior}
                        isPublic={true}
                        publicStudent={publicStudent}
                    />
                    <div className='d-flex justify-content-between align-items-center'>
                        <div>
                            <strong>Points: { points }</strong>
                        </div>
                        <Button variant='primary'
                                type='submit'
                                disabled={ loading || formData.behaviorIds.length === 0 }
                                style={{ minWidth: '120px', minHeight: '48px' }}
                        >
                            { loading ? <Spinner size='sm' animation='border' /> : 'Submit' }
                        </Button>
                    </div>
                    { error && <Alert variant='danger' className='mt-3'>{ error }</Alert> }
                </Form>
            </Card.Body>
        </Card>
    );
}
