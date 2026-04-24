import { useAppSelector, useAppDispatch, fetchTeachers } from '../../store';
import { studentValidationRules } from '../../utils';
import { StudentDTO, Role } from '../../services';
import { useForm } from '../index';
import { useEffect } from 'react';

export interface UseStudentFormProps {
    show: boolean;
    isEdit?: boolean;
    student?: StudentDTO | null;
}

export const useStudentForm = ({ show, isEdit = false, student }: UseStudentFormProps) => {
    const dispatch = useAppDispatch();
    const { loading: studentsLoading, error: studentsError } = useAppSelector(
        state => state.students);
    const { data: teachers, loading: teachersLoading, error: teachersError } = useAppSelector(
        state => state.teachers);
    const currentUser = useAppSelector(
        state => state.user.data);

    const initialData = {
        firstName: '',
        lastName: '',
        email: '',
        teacherId: -1,
    };

    const form = useForm({ initialData, validationRules: studentValidationRules });

    const isAdmin = currentUser?.role === Role.ADMIN;
    const error = studentsError || teachersError;
    const isLoading = studentsLoading || teachersLoading;

    useEffect(() => {
        if (show && isAdmin && teachers.length === 0) {
            dispatch(fetchTeachers({ page: 0, size: 1000, force: true }));
        }
    }, [show, isAdmin, dispatch, teachers.length]);

    useEffect(() => {
        if (show && !isEdit) {
            form.resetForm();
        }
    }, [show, isEdit, form]);

    useEffect(() => {
        if(show && isEdit && student) {
            form.setFormData({
                firstName: student.user.firstName,
                lastName: student.user.lastName,
                email: student.user.email,
                teacherId: student.teacher?.id ?? -1
            });
        }
    }, [show, isEdit, student, form.setFormData, form]);

    return {
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        setFormErrors: form.setFormErrors,
        teachers,
        currentUser,
        isAdmin,
        error,
        isLoading,
        handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange,
        handleCheckboxChange: form.handleCheckboxChange,
        validateForm: form.validateForm,
        resetForm: form.resetForm
    };
};
