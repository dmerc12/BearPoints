import { Teacher, Role, GradeLevel } from '../../services/types';
import { addTeacher } from '../../store/slices/teachersSlice';
import { useTeacherForm } from '../../hooks/useTeacherForm';
import { useAppDispatch } from '../../store/hooks';
import { TeacherForm } from './TeacherForm';
import { Alert } from 'react-bootstrap';
import BaseModal from '../BaseModal';

interface CreateTeacherModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateTeacherModal({ show, onCancel, onSuccess }: CreateTeacherModalProps) {
    const dispatch = useAppDispatch();
    const { formData, formErrors, error, loading, handleInputChange,
        handleSelectChange, validateForm, resetForm } = useTeacherForm({ show });

    const handleSubmit = () => {
        if (!validateForm()) return;
        const userData = {
            id: null,
            firstName: formData.firstName,
            lastName: formData.lastName,
            email: formData.email,
            role: Role.TEACHER
        };
        const teacherData: Partial<Teacher> = {
            user: userData,
            grade: formData.grade as GradeLevel
        };
        dispatch(addTeacher(teacherData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            })
            .catch((error: Error) => {
                console.log('Failed to create teacher:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title='Create Teacher'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Create'
            cancelText='Cancel'
            isLoading={loading}
            disableConfirm={loading}
        >
            {error && <Alert variant='danger'>{error}</Alert>}
            <TeacherForm 
                formData={formData}
                formErrors={formErrors}
                loading={loading}
                onInputChange={handleInputChange}
                onSelectChange={handleSelectChange}
            />
        </BaseModal>
    );
}
