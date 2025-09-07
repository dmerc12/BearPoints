import { removeStudent } from '../../store/slices/studentsSlice';
import { useAppDispatch, useAppSelector } from '../../store';
import { Student, Role } from '../../services';
import { useEffect, useState } from 'react';
import { Alert } from 'react-bootstrap';
import { fullName } from '../../utils';
import { BaseModal } from '../index';

interface DeleteStudentModalProps {
    show: boolean;
    student: Student | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function DeleteStudentModal({ show, student, onCancel, onSuccess }: DeleteStudentModalProps) {
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector(state => state.students);
    const currentUser = useAppSelector(state => state.user.data);

    const [authError, setAuthError] = useState<string>('');

    const isAdmin = currentUser?.role === Role.ADMIN;
    const isTeacher = currentUser?.role === Role.TEACHER;
    const isAuthorized = !authError;
    const disableConfirm = loading || !isAuthorized;

    useEffect(() => {
        if (show && !isAdmin && isTeacher && student && student.teacher.id !== currentUser?.teacherId) {
            setAuthError('You can only delete students in your own class');
            const timer = setTimeout(() => {
                setAuthError('');
            }, 3000);
            return () => clearTimeout(timer);
        } else {
            setAuthError('');
        }
    }, [show, isAdmin, isTeacher, student, currentUser]);

    const handleConfirmDelete = () => {
        if (!student || authError) return;
        dispatch(removeStudent(student.id))
            .unwrap()
            .then(() => {
                onSuccess();
            })
            .catch((error: Error) => {
                console.log('Failed to delete student:', error);
            });
    };

    const handleClose = () => {
        setAuthError('');
        onCancel();
    };

    return (
        <BaseModal
            title='Delete Student'
            show={show}
            onConfirm={handleConfirmDelete}
            onCancel={handleClose}
            confirmText='Delete'
            cancelText='Cancel'
            confirmVariant='danger'
            isLoading={loading}
            disableConfirm={disableConfirm}
        >
            {authError && <Alert variant='danger'>{authError}</Alert>}
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
