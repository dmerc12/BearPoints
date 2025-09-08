import { TeacherFormData, teacherValidationRules } from '../utils';
import { RootState, useAppSelector, fetchTeachers } from '../store';
import { Teacher, Role } from '../services';
import { useForm, useTable } from './index';
import { useEffect } from 'react';

export interface UseTeacherFormProps {
    show: boolean;
    isEdit?: boolean;
    teacher?: Teacher | null;
}

export const useTeacherForm = ({ show, isEdit = false, teacher }: UseTeacherFormProps) => {
    const { loading, error } = useAppSelector(
        state => state.teachers);
    const currentUser = useAppSelector(
        state => state.user.data);

    const initialData: TeacherFormData = {
        firstName: '',
        lastName: '',
        email: '',
        grade: null
    };
    
    const form = useForm({ initialData, validationRules: teacherValidationRules });

    const isAdmin = currentUser?.role === Role.ADMIN;
    const isTeacher = currentUser?.role === Role.TEACHER;

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
        formData: form.formData, setFormData: form.setFormData, formErrors: form.formErrors,
        setFormErrors: form.setFormErrors, currentUser, isAdmin, isTeacher, error, loading, 
        handleInputChange: form.handleInputChange, handleSelectChange: form.handleSelectChange,
        handleCheckboxChange: form.handleCheckboxChange, validateForm: form.validateForm, resetForm: form.resetForm
    };
};

export function useTeacherTable() {
    return useTable<Teacher, { nameSearch: string; gradeFilter: string }>({
        fetchAction: fetchTeachers,
        selector: (state: RootState) => state.teachers,
        initialFilters: {
            nameSearch: '',
            gradeFilter: '',
        }
    });
};
