import { useAppDispatch, modifyTeacher } from '../../store';
import { TeacherDTO, Role, GradeLevel } from '../../services';
import { BaseModal, TeacherForm } from '../index';
import { useTeacherForm } from '../../hooks';
import { Alert } from 'react-bootstrap';

interface EditTeacherModalProps {
    show: boolean;
    teacher: TeacherDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditTeacherModal({ show, teacher, onCancel, onSuccess }: EditTeacherModalProps) {
    const dispatch = useAppDispatch();
    const { formData, formErrors, error, loading, handleInputChange,
        handleSelectChange, validateForm, resetForm } = useTeacherForm({ show, isEdit: true, teacher });

    const handleSubmit = () => {
        if (!validateForm() || !teacher || teacher.id === undefined || teacher.id === null) return;
        const userData = {
            id: teacher.user.id,
            firstName: formData.firstName,
            lastName: formData.lastName,
            email: formData.email,
            role: Role.TEACHER
        };
        const teacherData: TeacherDTO = {
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
