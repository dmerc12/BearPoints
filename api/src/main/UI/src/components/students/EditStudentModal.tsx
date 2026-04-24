import { StudentDTO, Role, TeacherDTO } from '../../services';
import { useAppDispatch, modifyStudent } from '../../store';
import { BaseModal, StudentForm } from '../index';
import { useStudentForm } from '../../hooks';
import { useEffect } from 'react';

interface EditStudentModalProps {
    show: boolean;
    student: StudentDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditStudentModal({ show, student, onCancel, onSuccess }: EditStudentModalProps) {
    const dispatch = useAppDispatch();

    const {
        formData, formErrors, setFormErrors, teachers, currentUser,
        isAdmin, error, isLoading, handleInputChange, handleSelectChange,
        validateForm, resetForm
    } = useStudentForm({ show, isEdit: true, student });

    useEffect(() => {
        if (show && !isAdmin && student) {
            onCancel();
            setFormErrors({ general: 'Only administrators can update students' });
            const timer = setTimeout(() => {
                setFormErrors({});
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, student, isAdmin, currentUser, onCancel, setFormErrors]);

    const handleSubmit = () => {
        if (!validateForm() || !student || student.id === undefined || student.id === null) return;
        const userData = {
            id: student.user.id,
            email: formData.email,
            firstName: formData.firstName,
            lastName: formData.lastName,
            role: Role.STUDENT
        }
        const studentData: StudentDTO = {
            id: student.id,
            user: userData,
            teacher: { id: formData.teacherId } as TeacherDTO
        };
        dispatch(modifyStudent({ id: student.id, studentData }))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            }).catch((err: Error) => {
            console.log('Failed to update student:', err);
        });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title='Edit Student'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Save Changes'
            cancelText='Cancel'
            isLoading={isLoading}
            disableConfirm={isLoading}
        >
            <StudentForm
                formData={formData}
                formErrors={formErrors}
                teachers={teachers}
                loading={isLoading}
                error={error}
                onInputChange={handleInputChange}
                onSelectChange={handleSelectChange}
                showTeacherField={isAdmin}
            />
        </BaseModal>
    );
}
