import { StudentDTO, RewardItemDTO } from '../../services';
import { Form, Row, Col } from 'react-bootstrap';
import { fullName } from '../../utils';
import React from 'react';

interface StudentRewardFormProps {
    formData: {
        studentId: number;
        itemId: number;
    };
    formErrors: Record<string, string>;
    loading: boolean;
    students: StudentDTO[];
    rewardItems: RewardItemDTO[];
    onSelectChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
}

export function StudentRewardForm({ formData, formErrors, loading, students, rewardItems, onSelectChange }: StudentRewardFormProps) {
    return (
        <Form>
            <Row>
                <Col md={6}>
                    <Form.Group className="mb-3">
                        <Form.Label>Student</Form.Label>
                        <Form.Select
                            name="studentId"
                            value={formData.studentId}
                            onChange={onSelectChange}
                            isInvalid={!!formErrors.studentId}
                            disabled={loading}
                        >
                            <option value="">Select a student</option>
                            {students.map(student => {
                                if (!student.id) return null;
                                return (
                                    <option key={student.id} value={student.id}>
                                        {fullName(student)}
                                    </option>
                                );
                            })}
                        </Form.Select>
                        <Form.Control.Feedback type="invalid">
                            {formErrors.studentId}
                        </Form.Control.Feedback>
                    </Form.Group>
                </Col>
                <Col md={6}>
                    <Form.Group className="mb-3">
                        <Form.Label>Reward Item</Form.Label>
                        <Form.Select
                            name="itemId"
                            value={formData.itemId}
                            onChange={onSelectChange}
                            isInvalid={!!formErrors.itemId}
                            disabled={loading}
                        >
                            <option value="">Select a reward item</option>
                            {rewardItems.map(item => {
                                if (!item.id) return null;
                                return (
                                    <option key={item.id} value={item.id}>
                                        {item.name} ({item.pointCost} pts)
                                    </option>
                                );
                            })}
                        </Form.Select>
                        <Form.Control.Feedback type="invalid">
                            {formErrors.itemId}
                        </Form.Control.Feedback>
                    </Form.Group>
                </Col>
            </Row>
        </Form>
    );
}
