import { modifyStudent } from '../../store/slices/studentsSlice';
import { Student, Role, Teacher } from '../../services/types';
import { useStudentForm } from '../../hooks/useStudentForm';
import { Form, Row, Col, Alert } from 'react-bootstrap';
import { useAppDispatch } from '../../store/hooks';
import { fullName } from '../../utils/formatNames';
import BaseModal from '../BaseModal';
import { useEffect } from 'react';

interface EditStudentModalProps {
    show: boolean;
    student: Student | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditStudentModal({ show, student, onCancel, onSuccess }: EditStudentModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, setFormErrors, teachers, currentUser,
        isAdmin, isTeacher, error, isLoading, handleInputChange, handleSelectChange,
        validateForm, resetForm } = useStudentForm({ show, isEdit: true, student });

    useEffect(() => {
        if (show && isTeacher && student && student.teacher.id !== currentUser?.teacherId) {
            onCancel();
            setFormErrors({ general: 'You can only edit students in your own class' });
            const timer = setTimeout(() => {
                setFormErrors({});
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, isTeacher, student, currentUser, onCancel, setFormErrors]);

    const handleSubmit = () => {
        if (!validateForm() || !student) return;
        const userData = {
            id: student.user.id,
            email: formData.email,
            firstName: formData.firstName,
            lastName: formData.lastName,
            role: Role.STUDENT
        }
        const studentData: Partial<Student> = {
            id: student.id,
            user: userData,
            teacher: { id: parseInt(formData.teacherId) } as Teacher
        };
        dispatch(modifyStudent({ id: student.id, studentData }))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            }).catch((err: Error) => {
            console.log('Failed to update student:', err);
        });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title='Edit Student'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Save Changes'
            cancelText='Cancel'
            isLoading={isLoading}
            disableConfirm={isLoading}
        >
            {formErrors.general && <Alert variant='danger'>{formErrors.general}</Alert> }
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
                    <Form.Label>Email</Form.Label>
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
                {(isAdmin || isTeacher) && (
                    <Form.Group className='mb-3'>
                        <Form.Label>Teacher</Form.Label>
                        {isTeacher ? (
                            <Form.Control type='text'
                                          value={student?.teacher ? fullName(student?.teacher) : 'Loading'}
                                          disabled />
                        ) : isLoading ? (
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

