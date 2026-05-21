import { useAppDispatch, useAppSelector, removeStudent, fetchStudents } from '../../store';
import { StudentDTO } from '../../services';
import { Alert } from 'react-bootstrap';
import { fullName } from '../../utils';
import { BaseModal } from '../index';

interface DeleteStudentModalProps {
    show: boolean;
    student: StudentDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function DeleteStudentModal({ show, student, onCancel, onSuccess }: DeleteStudentModalProps) {
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector(state => state.students);

    const handleConfirmDelete = () => {
        if (!student || student.id === undefined || student.id === null) return;
        dispatch(removeStudent(student.id))
            .unwrap()
            .then(() => {
                onSuccess();
                dispatch(fetchStudents({ page: 0, size: 10, force: true }));
            })
            .catch((error: Error) => {
                console.log('Failed to delete student:', error);
            });
    };

    return (
        <BaseModal
            title='Delete Student'
            show={show}
            onConfirm={handleConfirmDelete}
            onCancel={onCancel}
            confirmText='Delete'
            cancelText='Cancel'
            confirmVariant='danger'
            isLoading={loading}
            disableConfirm={loading}
        >
            {error && <Alert variant='danger'>{error}</Alert>}
            <p>Are you sure you want to delete { student ? fullName(student) : 'this student'}?</p>
            <p className='text-muted'>This action cannot be undone.</p>
            {student && (
                <div className='mt-3 p-3 bg-light rounded'>
                    <h6>Student Details:</h6>
                    <p><strong>Name:</strong> {fullName(student)}</p>
                    <p><strong>Teacher:</strong> {fullName(student.teacher)}</p>
                    <p><strong>Grade:</strong> {student.teacher.grade}</p>
                    <p><strong>Points:</strong> {student.points}</p>
                </div>
            )}
        </BaseModal>
    );
}
