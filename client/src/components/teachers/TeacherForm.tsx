import { TeacherFormData, formatGrade } from '../../utils';
import { Form, Row, Col } from 'react-bootstrap';
import { GradeLevel } from '../../services';
import React from 'react';

interface TeacherFormProps {
    formData: TeacherFormData;
    formErrors: Record<string, string>;
    loading: boolean;
    onInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onSelectChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
}

export function TeacherForm({ formData, formErrors, loading, onInputChange, onSelectChange }: TeacherFormProps) {
    return (
        <Form>
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
            <Form.Group className='mb-3'>
                <Form.Label>Grade</Form.Label>
                <Form.Select
                    name='grade'
                    value={formData.grade || ''}
                    onChange={onSelectChange}
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
    );
}