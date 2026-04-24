import { StudentDTO, Role, TeacherDTO } from '../../services';
import { useAppDispatch, addStudent } from '../../store';
import { BaseModal, StudentForm } from '../index';
import { useStudentForm  } from '../../hooks';
import { useEffect } from 'react';

interface CreateStudentModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateStudentModal({ show, onCancel, onSuccess }: CreateStudentModalProps) {
    const dispatch = useAppDispatch();

    const {
        formData, formErrors, setFormErrors, teachers, isAdmin, error, isLoading, handleInputChange,
        handleSelectChange, validateForm, resetForm } = useStudentForm({ show });

    useEffect(() => {
        if (show && !isAdmin) {
            onCancel();
            setFormErrors({ general: 'Only administrators can update students' });
            const timer = setTimeout(() => {
                setFormErrors({});
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, isAdmin, onCancel, setFormErrors]);

    const handleSubmit = () => {
        if (!validateForm()) return;
        const userData = {
            id: null,
            email: formData.email,
            firstName: formData.firstName,
            lastName: formData.lastName,
            role: Role.STUDENT
        }
        const studentData: StudentDTO = {
            user: userData,
            teacher: { id: formData.teacherId } as TeacherDTO,
        };
        dispatch(addStudent(studentData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            }).catch((err: Error) => {
                console.log('Failed to create student:', err);
        });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };
    
    return (
        <BaseModal
            title='Create Student'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Create'
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
