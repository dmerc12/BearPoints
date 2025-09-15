import { Student, Teacher, BehaviorType, BragLogFormData } from '../../services';
import { Form, Row, Col, Card } from 'react-bootstrap';
import { fullName } from '../../utils';
import React from 'react';

interface BragLogFormProps {
    formData: BragLogFormData;
    formErrors: Record<string, string>;
    loading: boolean;
    isAdmin: boolean;
    isPublic?: boolean;
    publicStudent?: Student | null;
    students: Student[];
    teachers: Teacher[];
    behaviorTypes: BehaviorType[];
    onInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onSelectChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
    onToggleBehavior: (behaviorId: string) => void;
}

export function BragLogForm({ formData, formErrors, loading, students, teachers, behaviorTypes, isAdmin,
                                isPublic = false, publicStudent = null, onInputChange, onSelectChange,
                                onToggleBehavior }: BragLogFormProps) {
    const selectedTeacher = teachers.find(t => t.id.toString() === formData.teacherId);
    const teacherName = selectedTeacher ? fullName(selectedTeacher) : '';

    const displayStudent = isPublic && publicStudent
        ? publicStudent
        : students.find(s => s.id.toString() === formData.studentId);
    const studentName = displayStudent ? fullName(displayStudent) : '';

    return (
        <Form>
            <Row>
                {isPublic ? (
                    <>
                        <Col md={6}>
                            <Form.Group className='mb-3'>
                                <Form.Label>Student</Form.Label>
                                <Form.Control
                                    type='text'
                                    value={studentName}
                                    readOnly
                                    disabled
                                />
                            </Form.Group>
                        </Col>
                        <Col md={6}>
                            <Form.Group className='mb-3'>
                                <Form.Label>Teacher</Form.Label>
                                <Form.Control
                                    type='text'
                                    value={teacherName}
                                    readOnly
                                    disabled
                                />
                            </Form.Group>
                        </Col>
                    </>
                ) : (
                    <>
                        {isAdmin && (
                            <Col md={6}>
                                <Form.Group className='mb-3'>
                                    <Form.Label>Teacher</Form.Label>
                                    <Form.Select name='teacherId'
                                                 value={formData.teacherId}
                                                 onChange={onSelectChange}
                                                 isInvalid={!!formErrors.teacherId}
                                                 disabled={loading}
                                    >
                                        <option value=''>Select a teacher</option>
                                        {teachers.map(teacher => (
                                            <option key={teacher.id} value={teacher.id}>
                                                {fullName(teacher)}
                                            </option>
                                        ))}
                                    </Form.Select>
                                    <Form.Text className='text-muted'>
                                        Selecting a teacher will filter the students list
                                    </Form.Text>
                                    <Form.Control.Feedback type='invalid'>
                                        {formErrors.teacherId}
                                    </Form.Control.Feedback>
                                </Form.Group>
                            </Col>
                        )}
                        <Col md={6}>
                            <Form.Group className='mb-3'>
                                <Form.Label>Student</Form.Label>
                                <Form.Select name='studentId'
                                             value={formData.studentId}
                                             onChange={onSelectChange}
                                             isInvalid={!!formErrors.studentId}
                                             disabled={loading}
                                >
                                    <option value=''>Select a student</option>
                                    {students.map(student => (
                                        <option key={student.id} value={student.id}>
                                            {fullName(student)}
                                        </option>
                                    ))}
                                </Form.Select>
                                <Form.Control.Feedback type='invalid'>
                                    {formErrors.studentId}
                                </Form.Control.Feedback>
                                {!isAdmin && (
                                    <Form.Text className='text-muted'>
                                        Teacher will automatically set based on student selection
                                    </Form.Text>
                                )}
                            </Form.Group>
                        </Col>
                        {!isAdmin && !isPublic && (
                            <Col md={6}>
                                <Form.Group className='mb-3'>
                                    <Form.Label>Teacher</Form.Label>
                                    <Form.Control type='text'
                                                  value={teacherName}
                                                  readOnly
                                                  disabled
                                    />
                                    <Form.Text className='text-muted'>
                                        Automatically set based on student selection
                                    </Form.Text>
                                </Form.Group>
                            </Col>
                        )}
                    </>
                )}
            </Row>
            <Form.Group className='mb-3'>
                <Form.Label>Behaviors</Form.Label>
                <Card>
                    <Card.Body>
                        <div className='d-flex flex-column flex-md-row flex-wrap gap-3'>
                            {behaviorTypes.map(behaviorType => (
                                <Form.Check
                                    key={behaviorType.id}
                                    type='checkbox'
                                    label={`${behaviorType.name} (${behaviorType.pointValue} pts)`}
                                    checked={formData.behaviorIds.includes(behaviorType.id.toString())}
                                    onChange={() => onToggleBehavior(behaviorType.id.toString())}
                                    disabled={loading}
                                    id={`behavior-${behaviorType.id}`}
                                />
                            ))}
                        </div>
                    </Card.Body>
                </Card>
                {formErrors.behaviorIds && (
                    <div className='text-danger mt-2'>
                        {formErrors.behaviorIds}
                    </div>
                )}
            </Form.Group>
            <Form.Group className='mb-3'>
                <Form.Label>Notes</Form.Label>
                <Form.Control as='textarea'
                              rows={3}
                              name='notes'
                              value={formData.notes || ''}
                              onChange={onInputChange}
                              isInvalid={!!formErrors.notes}
                              disabled={loading}
                              maxLength={500}
                />
                <Form.Control.Feedback type='invalid'>
                    {formErrors.notes}
                </Form.Control.Feedback>
            </Form.Group>
            <Form.Group className='mb-3'>
                <Form.Label>Points Generated</Form.Label>
                <Form.Control type='text'
                              value={formData.pointsGenerated || 0}
                              disabled
                              readOnly
                />
                <Form.Text className='text-muted'>
                    Calculated from selected behaviors
                </Form.Text>
            </Form.Group>
        </Form>
    );
}
