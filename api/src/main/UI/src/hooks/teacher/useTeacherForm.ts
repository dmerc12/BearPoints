import { teacherValidationRules } from '../../utils';
import { TeacherDTO, Role } from '../../services';
import { useAppSelector } from '../../store';
import { useForm } from '../index';
import { useEffect } from 'react';

export interface UseTeacherFormProps {
    show: boolean;
    isEdit?: boolean;
    teacher?: TeacherDTO | null;
}

export function useTeacherForm ({ show, isEdit = false, teacher }: UseTeacherFormProps) {
    const { loading, error } = useAppSelector(
        state => state.teachers);
    const currentUser = useAppSelector(
        state => state.user.data);

    const initialData = {
        firstName: '',
        lastName: '',
        email: '',
        grade: '',
    };

    const form = useForm({ initialData, validationRules: teacherValidationRules });

    const isAdmin = currentUser?.role === Role.ADMIN;

    useEffect(() => {
        if (show && !isEdit) {
            form.resetForm();
        }
    }, [show, isEdit, form]);

    useEffect(() => {
        if (show && isEdit && teacher) {
            form.setFormData({
                firstName: teacher.user.firstName,
                lastName: teacher.user.lastName,
                email: teacher.user.email,
                grade: teacher.grade
            });
        }
    }, [show, isEdit, teacher, form]);

    return {
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        setFormErrors: form.setFormErrors,
        currentUser,
        isAdmin, 
        error,
        loading,
        handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange,
        handleCheckboxChange: form.handleCheckboxChange,
        validateForm: form.validateForm,
        resetForm: form.resetForm
    };
}
