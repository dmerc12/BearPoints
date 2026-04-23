import { useAppDispatch, useAppSelector, removeTeacher } from '../../store';
import { fullName, formatGrade } from '../../utils';
import { TeacherDTO, Role } from '../../services';
import { useEffect, useState } from 'react';
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
    const currentUser = useAppSelector(state => state.user.data);

    const [authError, setAuthError] = useState<string>('');

    const isAuthorized = currentUser?.role === Role.ADMIN;
    const disableConfirm = loading || !isAuthorized;

    useEffect(() => {
        if (show && !isAuthorized && teacher) {
            setAuthError('Only administrators can delete teachers');
            const timer = setTimeout(() => {
                setAuthError('');
            }, 3000);
            return () => clearTimeout(timer);
        } else {
            setAuthError('');
        }
    }, [show, isAuthorized, teacher]);

    const handleConfirmDelete = () => {
        if (!teacher || !isAuthorized || teacher.id === undefined || teacher.id === null) return;
        dispatch(removeTeacher(teacher.id))
            .unwrap()
            .then(() => {
                onSuccess();
            })
            .catch((error: Error) => {
                console.log('Failed to delete teacher:', error);
            });
    };

    const handleClose = () => {
        setAuthError('');
        onCancel();
    };

    return (
        <BaseModal
            title='Delete Teacher'
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
