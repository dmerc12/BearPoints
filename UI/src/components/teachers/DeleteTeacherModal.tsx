import { useAppDispatch, useAppSelector, removeTeacher, fetchTeachers } from '../../store';
import { fullName, formatGrade } from '../../utils';
import { TeacherDTO } from '../../services';
import { Alert } from 'react-bootstrap';
import { BaseModal } from '../index';

interface DeleteTeacherModalProps {
    show: boolean;
    teacher: TeacherDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function DeleteTeacherModal({ show, teacher, onCancel, onSuccess }: DeleteTeacherModalProps) {
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector(state => state.teachers);

    const handleConfirmDelete = () => {
        if (!teacher || teacher.id === undefined || teacher.id === null) return;
        dispatch(removeTeacher(teacher.id))
            .unwrap()
            .then(() => {
                onSuccess();
                dispatch(fetchTeachers({ page: 0, size: 10, force: true }));
            })
            .catch((error: Error) => {
                console.log('Failed to delete teacher:', error);
            });
    };

    return (
        <BaseModal
            title='Delete Teacher'
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
            <p>Are you sure you want to delete {teacher ? fullName(teacher.user) : 'this teacher'}?</p>
            <p className='text-muted'>This action cannot be undone.</p>
            {teacher && (
                <div className='mt-3 p-3 bg-light rounded'>
                    <h6>Teacher Details:</h6>
                    <p><strong>Name:</strong> {fullName(teacher.user)}</p>
                    <p><strong>Email:</strong> {teacher.user.email}</p>
                    <p><strong>Grade:</strong> {formatGrade(teacher.grade)}</p>
                </div>
            )}
        </BaseModal>
    );
}
