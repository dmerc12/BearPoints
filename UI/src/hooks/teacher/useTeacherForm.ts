import { teacherValidationRules } from '../../utils';
import { TeacherDTO, Role } from '../../services';
import { useAppSelector } from '../../store';
import { useEffect, useRef } from 'react';
import { useForm } from '../index';

export interface UseTeacherFormProps {
    show: boolean;
    isEdit?: boolean;
    teacher?: TeacherDTO | null;
}

export function useTeacherForm ({ show, isEdit = false, teacher }: UseTeacherFormProps) {
    const { loading, error } = useAppSelector(
        state => state.teachers);
    const prevIdRef = useRef<number | null | undefined>(null);
    const hasResetForCurrentShowRef = useRef(false);

    const initialData = {
        firstName: '',
        lastName: '',
        email: '',
        role: Role.TEACHER,
        grade: '',
    };

    const form = useForm({ initialData, validationRules: teacherValidationRules });

    useEffect(() => {
        if (!show) {
            prevIdRef.current = null;
            hasResetForCurrentShowRef.current = false;
        }
    }, [show]);

    useEffect(() => {
        if (!show) return;
        if (isEdit && teacher && teacher.id !== prevIdRef.current) {
            form.setFormData({
                firstName: teacher.user.firstName,
                lastName: teacher.user.lastName,
                email: teacher.user.email,
                role: teacher.user.role,
                grade: teacher.grade
            });
            prevIdRef.current = teacher.id;
        } else if (!isEdit && !hasResetForCurrentShowRef.current) {
            form.resetForm();
            hasResetForCurrentShowRef.current = true;
        }
    }, [show, isEdit, teacher, form]);

    return {
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        setFormErrors: form.setFormErrors,
        error,
        loading,
        handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange,
        handleCheckboxChange: form.handleCheckboxChange,
        validateForm: form.validateForm,
        resetForm: form.resetForm
    };
}
