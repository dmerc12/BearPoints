import { Form, Row, Col, Alert } from 'react-bootstrap';
import { TeacherDTO } from '../../services';
import { fullName } from '../../utils';
import React from 'react';

interface StudentFormProps {
    formData: {
        firstName: string;
        lastName: string;
        email: string;
        teacherId: number;
    };
    formErrors: Record<string, string>;
    teachers: TeacherDTO[];
    loading: boolean;
    error?: string | null;
    onInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onSelectChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
    showTeacherField?: boolean;
}

export function StudentForm({ formData, formErrors, teachers, loading, onInputChange, onSelectChange,
                                showTeacherField = true, error }: StudentFormProps) {
    return (
        <Form>
            {formErrors.general && <Alert variant='danger'>{formErrors.general}</Alert>}
            {error && <Alert variant='danger'>{error}</Alert> }
            <Row>
                <Col md={6}>
                    <Form.Group className='mb-3'>
                        <Form.Label>First Name</Form.Label>
                        <Form.Control
                            type='text'
                            name='firstName'
                            value={formData.firstName}
                            onChange={onInputChange}
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
                            onChange={onInputChange}
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
                    onChange={onInputChange}
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
            {showTeacherField && (
                <Form.Group className='mb-3'>
                    <Form.Label>Teacher</Form.Label>
                    {loading ? (
                        <Form.Control
                            type='text'
                            value='Loading teachers...'
                            disabled
                        />
                    ) : (
                        <Form.Select
                            name='teacherId'
                            value={formData.teacherId}
                            onChange={onSelectChange}
                            isInvalid={!!formErrors.teacherId}
                            disabled={loading}
                        >
                            <option value={-1}>Select a teacher</option>
                            {teachers.map((teacher: TeacherDTO) => {
                                if (teacher.id === undefined || teacher.id === null) return;
                                return (
                                    <option key={teacher.id} value={teacher.id}>
                                        {fullName(teacher.user)}
                                    </option>
                                );
                            })}
                        </Form.Select>
                    )}
                    <Form.Control.Feedback type='invalid'>
                        {formErrors.teacherId}
                    </Form.Control.Feedback>
                </Form.Group>
            )}
        </Form>
    );
}
