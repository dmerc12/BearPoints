import { Form, Row, Col } from 'react-bootstrap';
import React from 'react';

interface RewardItemFormProps {
    formData: {
        name: string;
        pointCost: number;
        stock: number;
    };
    formErrors: Record<string, string>;
    loading: boolean;
    onInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

export function RewardItemForm({ formData, formErrors, loading, onInputChange }: RewardItemFormProps) {
    return (
        <Form>
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
            <Row>
                <Col md={6}>
                    <Form.Group className='mb-3'>
                        <Form.Label>Point Cost</Form.Label>
                        <Form.Control
                            type='number'
                            name='pointCost'
                            value={formData.pointCost}
                            onChange={onInputChange}
                            isInvalid={!!formErrors.pointCost}
                            disabled={loading}
                            min={0}
                            step={1}
                        />
                        <Form.Control.Feedback type='invalid'>
                            {formErrors.pointCost}
                        </Form.Control.Feedback>
                    </Form.Group>
                </Col>
                <Col md={6}>
                    <Form.Group className='mb-3'>
                        <Form.Label>Stock</Form.Label>
                        <Form.Control
                            type='number'
                            name='stock'
                            value={formData.stock}
                            onChange={onInputChange}
                            isInvalid={!!formErrors.stock}
                            disabled={loading}
                            min={0}
                            step={1}
                        />
                        <Form.Control.Feedback type='invalid'>
                            {formErrors.stock}
                        </Form.Control.Feedback>
                    </Form.Group>
                </Col>
            </Row>
        </Form>
    );
}
