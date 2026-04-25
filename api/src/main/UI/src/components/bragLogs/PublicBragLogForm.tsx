import { Form, Alert, Spinner, Card, Button, Container } from 'react-bootstrap';
import { useAppDispatch, addBragLog } from '../../store';
import { useBragLogForm } from '../../hooks';
import { BragLogDTO } from '../../services';
import { FormEvent, useState } from 'react';
import { fullName } from '../../utils';
import { BragLogForm } from '../index';

interface PublicBragLogFormProps {
    studentToken: string;
}

export default function PublicBragLogForm ({ studentToken }: PublicBragLogFormProps) {
    const dispatch = useAppDispatch();

    const [ success, setSuccess ] = useState(false);

    const { formData, formErrors, loading, error, students, teachers, behaviorTypes, totalPoints, selectedStudent,
        handleInputChange, toggleBehavior, resetForm } = useBragLogForm({ show: true, isPublic: true, studentToken });

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        try {

            const bragLogData: BragLogDTO = {
                studentId: formData.studentId,
                behaviorIds: formData.behaviorIds,
                notes: formData.notes,
                submitterName: formData.submitterName,
            };
            const result = await dispatch(addBragLog(bragLogData));
            if (addBragLog.fulfilled.match(result)) {
                resetForm();
                setSuccess(true);
                const timer = setTimeout(() => {
                    setSuccess(false);
                }, 3000);
                return () => clearTimeout(timer);
            }
        } catch (error) {
            console.error('Submission failed:', error);
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

    if (!selectedStudent) {
        return (
            <Container className='mt-4'>
                <Alert variant='danger'>Invalid or expired QR code</Alert>
            </Container>
        );
    }

    return (
        <Card className='mb-4'>
            <Card.Header>
                <h3>Submit Bear Brag for {fullName(selectedStudent)}</h3>
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
                        students={students}
                        teachers={teachers}
                        behaviorTypes={behaviorTypes}
                        totalPoints={totalPoints}
                        publicStudent={selectedStudent}
                        isPublic={true}
                        onInputChange={handleInputChange}
                        onSelectChange={() => {}}
                        onToggleBehavior={toggleBehavior}
                    />
                    <div className='d-flex justify-content-between align-items-center'>
                        <div>
                            <strong>Points: { totalPoints }</strong>
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
