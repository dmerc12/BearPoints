import { useAppDispatch, addStudent, fetchStudents } from '../../store';
import { StudentDTO, TeacherDTO } from '../../services';
import { BaseModal, StudentForm } from '../index';
import { useStudentForm  } from '../../hooks';

interface CreateStudentModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
    defaultTeacherId?: number;
}

export function CreateStudentModal({ show, onCancel, onSuccess, defaultTeacherId }: CreateStudentModalProps) {
    const dispatch = useAppDispatch();

    const {
        formData, formErrors, teachers, error, isLoading, handleInputChange, isTeacherSelectDisabled, selectedTeacherName,
        handleSelectChange, validateForm, resetForm } = useStudentForm({ show, defaultTeacherId });

    const handleSubmit = () => {
        if (!validateForm()) return;
        const userData = {
            id: null,
            email: formData.email,
            firstName: formData.firstName,
            lastName: formData.lastName,
            role: formData.role,
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
                dispatch(fetchStudents({ page: 0, size: 10, force: true }));
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
                selectedTeacherName={selectedTeacherName}
                disableTeacherSelect={isTeacherSelectDisabled}
            />
        </BaseModal>
    );
}
