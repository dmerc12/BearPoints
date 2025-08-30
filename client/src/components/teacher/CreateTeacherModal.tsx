import { Teacher, Role, GradeLevel } from '../../services/types';
import { addTeacher } from '../../store/slices/teachersSlice';
import { useTeacherForm } from '../../hooks/useTeacherForm';
import { Form, Row, Col, Alert } from 'react-bootstrap';
import { formatGrade } from '../../utils/formatGrades';
import { useAppDispatch } from '../../store/hooks';
import BaseModal from '../BaseModal';

interface CreateTeacherModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateTeacherModal({ show, onCancel, onSuccess }: CreateTeacherModalProps) {
    const dispatch = useAppDispatch();
    const { formData, formErrors, error, loading, handleInputChange,
        handleSelectChange, validateForm, resetForm } = useTeacherForm({ show });

    const handleSubmit = () => {
        if (!validateForm()) return;
        const userData = {
            id: null,
            firstName: formData.firstName,
            lastName: formData.lastName,
            email: formData.email,
            role: Role.TEACHER
        };
        const teacherData: Partial<Teacher> = {
            user: userData,
            grade: formData.grade as GradeLevel
        };
        dispatch(addTeacher(teacherData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            })
            .catch((error: Error) => {
                console.log('Failed to create teacher:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title='Create Teacher'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Create'
            cancelText='Cancel'
            isLoading={loading}
            disableConfirm={loading}
        >
            {error && <Alert variant='danger'>{error}</Alert>}
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
                                disabled={loading}
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
                                disabled={loading}
                            />
                            <Form.Control.Feedback type='invalid'>
                                {formErrors.lastName}
                            </Form.Control.Feedback>
                        </Form.Group>
                    </Col>
                </Row>
                <Form.Group className='mb-3'>
                    <Form.Label>Email</Form.Label>
                    <Form.Control
                        type='email'
                        name='email'
                        value={formData.email}
                        onChange={handleInputChange}
                        isInvalid={!!formErrors.email}
                        disabled={loading}
                    />
                    <Form.Text className='text-muted'>
                        Must be an @okcps.org email address
                    </Form.Text>
                    <Form.Control.Feedback type='invalid'>
                        {formErrors.email}
                    </Form.Control.Feedback>
                </Form.Group>
                <Form.Group className='mb-3'>
                    <Form.Label>Grade</Form.Label>
                    <Form.Select
                        name='grade'
                        value={formData.grade || ''}
                        onChange={handleSelectChange}
                        isInvalid={!!formErrors.grade}
                        disabled={loading}
                    >
                        <option value=''>Select a grade</option>
                        {Object.values(GradeLevel).map((grade) => (
                            <option key={grade} value={grade}>
                                {formatGrade(grade)}
                            </option>
                        ))}
                    </Form.Select>
                    <Form.Control.Feedback type='invalid'>
                        {formErrors.grade}
                    </Form.Control.Feedback>
                </Form.Group>
            </Form>
        </BaseModal>
    );
}
