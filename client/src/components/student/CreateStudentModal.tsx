import { useAppSelector, useAppDispatch } from '../../store/hooks.ts';
import { fetchTeachers } from '../../store/slices/teachersSlice.ts';
import { addStudent } from '../../store/slices/studentsSlice.ts';
import { Student, Role, Teacher } from '../../services/types';
import { Form, Row, Col, Alert } from 'react-bootstrap';
import { fullName } from '../../utils/formatNames.ts';
import React, { useEffect, useState } from 'react';
import BaseModal from '../BaseModal';

interface CreateStudentModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateStudentModal({ show, onCancel, onSuccess }: CreateStudentModalProps) {
    const dispatch = useAppDispatch();
    const { loading: studentsLoading, error: studentsError } = useAppSelector(
        state => state.students);
    const { teachers, loading: teachersLoading, error: teachersError } = useAppSelector(
        state => state.teachers);
    const currentUser = useAppSelector(
        state => state.user.data);
    const [formErrors, setFormErrors] = useState<Record<string, string>>({});
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        teacherId: '',
    });

    const isAdmin = currentUser?.role === Role.ADMIN;
    const isTeacher = currentUser?.role === Role.TEACHER;
    const error = studentsError || teachersError;

    useEffect(() => {
        if (show && isAdmin && teachers.length === 0) {
            dispatch(fetchTeachers({ page: 0, size: 1000, force: true }));
        }
    }, [show, isAdmin, dispatch, teachers.length]);

    useEffect(() => {
        if (show && isTeacher && currentUser?.teacherId) {
            setFormData(prev => ({
                ...prev,
                teacherId: currentUser.teacherId ? currentUser.teacherId.toString() : '',
            }));
        }
    }, [show, isTeacher, currentUser]);

    const handleChange = (name: string, value: string | number) => {
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        if (formErrors[name]) {
            setFormErrors(prev => {
                const newErrors = { ...prev };
                delete newErrors[name];
                return newErrors;
            });
        }
    }

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        handleChange(e.target.name, e.target.value);
    };

    const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        handleChange(e.target.name, e.target.value);
    };

    const validateForm = () => {
        const errors: Record<string, string> = {};
        if (!formData.firstName.trim()) errors.firstName = 'First name is required';
        if (!formData.lastName.trim()) errors.lastName = 'Last name is required';
        if (!formData.email.trim()) {
            errors.email = 'Email is required';
        } else if (!formData.email.endsWith('@okcps.org')) {
            errors.email = 'Email must be from @okcps.org domain';
        }
        if (!formData.teacherId.trim()) errors.teacherId = 'Teacher Id is required';
        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

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
            teacher: {
                id: parseInt(formData.teacherId)
            } as Teacher
        };
        dispatch(addStudent(studentData))
            .unwrap()
            .then(() => {
                onSuccess();
                setFormData({
                    firstName: '',
                    lastName: '',
                    email: '',
                    teacherId: '',
                });
                setFormErrors({});
            }).catch((err: Error) => {
                console.log('Failed to create student:', err);
        });
    };

    const handleClose = () => {
        setFormData({
            firstName: '',
            lastName: '',
            email: '',
            teacherId: '',
        });
        setFormErrors({});
        onCancel();
    };

    const isLoading = studentsLoading || teachersLoading;

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
                        {teachersLoading ? (
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
                                {teachers.map(teacher => (
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
