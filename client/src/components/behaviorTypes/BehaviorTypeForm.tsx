import { BehaviorTypeFormData } from '../../utils/validationRules';
import { Form, Row, Col } from 'react-bootstrap';
import React from 'react';

interface BehaviorTypeFormProps {
    formData: BehaviorTypeFormData;
    formErrors: Record<string, string>;
    loading: boolean;
    onInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onCheckboxChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

export function BehaviorTypeForm({ formData, formErrors, loading, onInputChange, onCheckboxChange }: BehaviorTypeFormProps) {
    return (
        <Form>
            <Row>
                <Col md={8}>
                    <Form.Group className='mb-3'>
                        <Form.Label>Name</Form.Label>
                        <Form.Control
                            type='text'
                            name='name'
                            value={formData.name}
                            onChange={onInputChange}
                            isInvalid={!!formErrors.name}
                            disabled={loading}
                            maxLength={50}
                        />
                        <Form.Control.Feedback type='invalid'>
                            {formErrors.name}
                        </Form.Control.Feedback>
                    </Form.Group>
                </Col>
                <Col md={4}>
                    <Form.Group className='mb-3'>
                        <Form.Label>Point Value</Form.Label>
                        <Form.Control
                            type='number'
                            name='pointValue'
                            value={formData.pointValue}
                            onChange={onInputChange}
                            isInvalid={!!formErrors.pointValue}
                            disabled={loading}
                            min={1}
                            max={5}
                        />
                        <Form.Control.Feedback type='invalid'>
                            {formErrors.pointValue}
                        </Form.Control.Feedback>
                    </Form.Group>
                </Col>
            </Row>
            <Form.Group className='mb-3'>
                <Form.Check
                    type='switch'
                    name='active'
                    label='Active'
                    checked={formData.active}
                    onChange={onCheckboxChange}
                    disabled={loading}
                />
            </Form.Group>
        </Form>
    );
}
