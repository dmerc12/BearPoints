import { useAppDispatch, modifyStudent } from '../../store';
import { Student, Role, Teacher } from '../../services';
import { BaseModal, StudentForm } from '../index';
import { useStudentForm } from '../../hooks';
import { useEffect } from 'react';

interface EditStudentModalProps {
    show: boolean;
    student: Student | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditStudentModal({ show, student, onCancel, onSuccess }: EditStudentModalProps) {
    const dispatch = useAppDispatch();

    const {
        formData, formErrors, setFormErrors, teachers, currentUser, teacherDisplayValue,
        isAdmin, isTeacher, error, isLoading, handleInputChange, handleSelectChange,
        validateForm, resetForm
    } = useStudentForm({ show, isEdit: true, student });

    useEffect(() => {
        if (show && isTeacher && student && student.teacher.id !== currentUser?.teacherId) {
            onCancel();
            setFormErrors({ general: 'You can only edit students in your own class' });
            const timer = setTimeout(() => {
                setFormErrors({});
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, isTeacher, student, currentUser, onCancel, setFormErrors]);

    const handleSubmit = () => {
        if (!validateForm() || !student) return;
        const userData = {
            id: student.user.id,
            email: formData.email,
            firstName: formData.firstName,
            lastName: formData.lastName,
            role: Role.STUDENT
        }
        const studentData: Partial<Student> = {
            id: student.id,
            user: userData,
            teacher: { id: parseInt(formData.teacherId) } as Teacher
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
                teacherDisplayValue={teacherDisplayValue}
                showTeacherField={isAdmin || isTeacher}
                isTeacherMode={isTeacher}
            />
        </BaseModal>
    );
}
