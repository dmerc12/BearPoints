import { modifyTeacher } from '../../store/slices/teachersSlice';
import { useTeacherForm } from '../../hooks/useTeacherForm';
import { Teacher, Role, GradeLevel } from '../../services';
import { useAppDispatch } from '../../store';
import { TeacherForm } from './TeacherForm';
import { Alert } from 'react-bootstrap';
import { BaseModal } from '../index';
import { useEffect } from 'react';

interface EditTeacherModalProps {
    show: boolean;
    teacher: Teacher | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditTeacherModal({ show, teacher, onCancel, onSuccess }: EditTeacherModalProps) {
    const dispatch = useAppDispatch();
    const { formData, formErrors, setFormErrors, currentUser, isAdmin, isTeacher, error, loading, handleInputChange,
        handleSelectChange, validateForm, resetForm } = useTeacherForm({ show, isEdit: true, teacher });

    useEffect(() => {
        if (show && isTeacher && !isAdmin && teacher && teacher.user.id !== currentUser?.id) {
            onCancel();
            setFormErrors({ general: 'You can only edit your own information' });
            const timer = setTimeout(() => {
                setFormErrors({});
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, isTeacher, isAdmin, teacher, currentUser, onCancel, setFormErrors]);

    const handleSubmit = () => {
        if (!validateForm() || !teacher) return;
        const userData = {
            id: teacher.user.id,
            firstName: formData.firstName,
            lastName: formData.lastName,
            email: formData.email,
            role: Role.TEACHER
        };
        const teacherData: Partial<Teacher> = {
            id: teacher.id,
            user: userData,
            grade: formData.grade as GradeLevel
        };
        dispatch(modifyTeacher({ id: teacher.id, teacherData }))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            })
            .catch((error: Error) => {
                console.log('Failed to update teacher:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title='Edit Teacher'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Save Changes'
            cancelText='Cancel'
            isLoading={loading}
            disableConfirm={loading}
        >
            {formErrors.general && <Alert variant='danger'>{formErrors.general}</Alert>}
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
