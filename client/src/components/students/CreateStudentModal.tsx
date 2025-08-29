import { addStudent } from '../../store/slices/studentsSlice';
import { Student, Role, Teacher } from '../../services/types';
import { useStudentForm } from '../../hooks/useStudentForm';
import { Form, Row, Col, Alert } from 'react-bootstrap';
import { useAppDispatch } from '../../store/hooks';
import { fullName } from '../../utils/formatNames';
import BaseModal from '../BaseModal';

interface CreateStudentModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateStudentModal({ show, onCancel, onSuccess }: CreateStudentModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, teachers, isAdmin, error, isLoading, handleInputChange, handleSelectChange,
        validateForm, resetForm } = useStudentForm({ show });

    const handleSubmit = () => {
        if (!validateForm()) return;
        const userData = {
            id: null,
            email: formData.email,
            firstName: formData.firstName,
            lastName: formData.lastName,
            role: Role.STUDENT
        }
        const studentData: Partial<Student> = {
            user: userData,
            teacher: { id: parseInt(formData.teacherId) } as Teacher
        };
        dispatch(addStudent(studentData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            }).catch((err: Error) => {
                console.log('Failed to create student:', err);
        });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };
    
    return (
        <BaseModal
            title='Create Student'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Create'
            cancelText='Cancel'
            isLoading={isLoading}
            disableConfirm={isLoading}
        >
            {error && <Alert variant='danger'>{error}</Alert> }
            <Form>
                <Row>
                    <Col md={6}>
                        <Form.Group className='mb-3'>
                            <Form.Label>First Name</Form.Label>
                            <Form.Control
                                type='text'
                                name='firstName'
                                value={formData.firstName}
                                onChange={handleInputChange}
                                isInvalid={!!formErrors.firstName}
                                disabled={isLoading}
                            />
                            <Form.Control.Feedback type='invalid'>
                                {formErrors.firstName}
                            </Form.Control.Feedback>
                        </Form.Group>
                    </Col>
                    <Col md={6}>
                        <Form.Group className='mb-3'>
                            <Form.Label>Last Name</Form.Label>
                            <Form.Control
                                type='text'
                                name='lastName'
                                value={formData.lastName}
                                onChange={handleInputChange}
                                isInvalid={!!formErrors.lastName}
                                disabled={isLoading}
                            />
                            <Form.Control.Feedback type='invalid'>
                                {formErrors.lastName}
                            </Form.Control.Feedback>
                        </Form.Group>
                    </Col>
                </Row>
                <Form.Group className='mb-3'>
                    <Form.Label>Email *</Form.Label>
                    <Form.Control
                        type='email'
                        name='email'
                        value={formData.email}
                        onChange={handleInputChange}
                        isInvalid={!!formErrors.email}
                        disabled={isLoading}
                    />
                    <Form.Text className='text-muted'>
                        Must be an @okcps.org email address
                    </Form.Text>
                    <Form.Control.Feedback type='invalid'>
                        {formErrors.email}
                    </Form.Control.Feedback>
                </Form.Group>
                {isAdmin && (
                    <Form.Group className='mb-3'>
                        <Form.Label>Teacher *</Form.Label>
                        {isLoading ? (
                            <Form.Control  type='text' value='Loading teachers...' disabled />
                        ) : (
                            <Form.Select
                                name='teacherId'
                                value={formData.teacherId}
                                onChange={handleSelectChange}
                                isInvalid={!!formErrors.teacherId}
                                disabled={isLoading}
                            >
                                <option value=''>Select a teacher</option>
                                {teachers.map((teacher: Teacher) => (
                                    <option key={teacher.id} value={teacher.id}>
                                        {fullName(teacher)}
                                    </option>
                                ))}
                            </Form.Select>
                        )}
                        <Form.Control.Feedback type='invalid'>
                            {formErrors.teacherId}
                        </Form.Control.Feedback>
                    </Form.Group>
                )}
            </Form>
        </BaseModal>
    );
}
