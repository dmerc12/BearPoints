import { StudentDTO, TeacherDTO, BehaviorTypeDTO, Role } from '../../services';
import { Form, Row, Col, Card } from 'react-bootstrap';
import { fullName } from '../../utils';
import React from 'react';

interface BragLogFormProps {
    formData: {
        teacherId?: number | null;
        studentId: number;
        behaviorIds: number[];
        notes?: string | null;
        submitterName: string;
    };
    formErrors: Record<string, string>;
    loading: boolean;
    isPublic?: boolean;
    publicStudent?: StudentDTO | null;
    students: StudentDTO[];
    teachers: TeacherDTO[];
    behaviorTypes: BehaviorTypeDTO[];
    totalPoints: number;
    onInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onSelectChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
    onToggleBehavior: (behaviorId: number) => void;
}

export function BragLogForm({ formData, formErrors, loading, students, teachers, behaviorTypes,
                                isPublic = false, publicStudent = null, totalPoints, onInputChange,
                                onSelectChange, onToggleBehavior }: BragLogFormProps) {
    const displayStudent = isPublic && publicStudent
        ? publicStudent
        : students.find(s => s.id === formData.studentId);
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
                    </>
                ) : (
                    <>
                        <Col md={6}>
                            <Form.Group className='mb-3'>
                                <Form.Label>Teacher (Optional)</Form.Label>
                                <Form.Select key={teachers.length}
                                             name='teacherId'
                                             value={formData.teacherId ?? ''}
                                             onChange={onSelectChange}
                                             isInvalid={!!formErrors.teacherId}
                                             disabled={loading}
                                >
                                    <option value=''>Select a teacher</option>
                                    {teachers.filter(s => s.user?.role === Role.TEACHER).map(teacher => {
                                            if (teacher.id === undefined || teacher.id === null) return;
                                            return (
                                                <option key={teacher.id} value={teacher.id}>
                                                    {fullName(teacher)}
                                                </option>
                                            );
                                        }
                                    )}
                                </Form.Select>
                                <Form.Text className='text-muted'>
                                    Selecting a teacher will filter the students list
                                </Form.Text>
                                <Form.Control.Feedback type='invalid'>
                                    {formErrors.teacherId}
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Col>
                        <Col md={6}>
                            <Form.Group className='mb-3'>
                                <Form.Label>Student</Form.Label>
                                <Form.Select key={students.length}
                                             name='studentId'
                                             value={formData.studentId}
                                             onChange={onSelectChange}
                                             isInvalid={!!formErrors.studentId}
                                             disabled={loading}
                                >
                                    <option value=''>Select a student</option>
                                    {students.filter(s => s.user?.role === Role.STUDENT).map(student => {
                                        if (student.id === undefined || student.id === null) return;
                                        return (
                                            <option key={student.id} value={student.id}>
                                                {fullName(student)}
                                            </option>
                                        );
                                    }
                                )}
                                </Form.Select>
                                <Form.Control.Feedback type='invalid'>
                                    {formErrors.studentId}
                                </Form.Control.Feedback>
                            </Form.Group>
                        </Col>
                    </>
                )}
            </Row>
            <Form.Group className='mb-3'>
                <Form.Label>Your Name (Submitter)</Form.Label>
                <Form.Control
                    type='text'
                    name='submitterName'
                    value={formData.submitterName}
                    onChange={onInputChange}
                    isInvalid={!!formErrors.submitterName}
                    disabled={loading}
                    placeholder='First and last name'
                />
                <Form.Control.Feedback type='invalid'>
                    {formErrors.submitterName}
                </Form.Control.Feedback>
            </Form.Group>
            <Form.Group className='mb-3'>
                <Form.Label>Behaviors</Form.Label>
                <Card>
                    <Card.Body>
                        <div className='d-flex flex-column flex-md-row flex-wrap gap-3'>
                            {behaviorTypes.map(behaviorType => {
                                if (behaviorType.id === undefined || behaviorType.id === null) return;
                                return (
                                    <Form.Check
                                        key={behaviorType.id}
                                        type='checkbox'
                                        label={`${behaviorType.name} (${behaviorType.pointValue} pts)`}
                                        checked={formData.behaviorIds.includes(behaviorType.id)}
                                        onChange={() => onToggleBehavior(behaviorType.id!)}
                                        disabled={loading}
                                        id={`behavior-${behaviorType.id}`}
                                    />
                                );
                            })}
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
                              value={totalPoints}
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
