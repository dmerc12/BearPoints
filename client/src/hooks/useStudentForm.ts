import { StudentFormData, studentValidationRules } from '../utils/validationRules';
import { useAppSelector, useAppDispatch } from '../store/hooks';
import { fetchTeachers } from '../store/slices/teachersSlice';
import { Student, Role } from '../services/types';
import { useForm } from './useForm';
import { useEffect } from 'react';

export interface UseStudentFormProps {
    show: boolean;
    isEdit?: boolean;
    student?: Student | null;
}

export const useStudentForm = ({ show, isEdit = false, student }: UseStudentFormProps) => {
    const dispatch = useAppDispatch();
    const { loading: studentsLoading, error: studentsError } = useAppSelector(
        state => state.students);
    const { teachers, loading: teachersLoading, error: teachersError } = useAppSelector(
        state => state.teachers);
    const currentUser = useAppSelector(
        state => state.user.data);

    const initialData: StudentFormData = {
        firstName: '',
        lastName: '',
        email: '',
        teacherId: '',
    };

    const form = useForm({ initialData, validationRules: studentValidationRules });

    const isAdmin = currentUser?.role === Role.ADMIN;
    const isTeacher = currentUser?.role === Role.TEACHER;
    const error = studentsError || teachersError;
    const isLoading = studentsLoading || teachersLoading;

    useEffect(() => {
        if (show && isAdmin && teachers.length === 0) {
            dispatch(fetchTeachers({ page: 0, size: 1000, force: true }));
        }
    }, [show, isAdmin, dispatch, teachers.length]);

    useEffect(() => {
        if(show && isEdit && student) {
            form.setFormData({
                firstName: student.user.firstName,
                lastName: student.user.lastName,
                email: student.user.email,
                teacherId: student.teacher.id.toString()
            });
        } else if (show && isTeacher && currentUser?.teacherId) {
            form.setFormData(prev => ({
                ...prev,
                teacherId: currentUser.teacherId ? currentUser.teacherId.toString() : '',
            }));
        }
    }, [show, isEdit, student, isTeacher, currentUser, form.setFormData, form]);

    return { formData: form.formData, setFormData: form.setFormData, formErrors: form.formErrors,
        setFormErrors: form.setFormErrors, teachers, currentUser, isAdmin, isTeacher, error, isLoading,
        handleInputChange: form.handleInputChange, handleSelectChange: form.handleSelectChange,
        handleCheckboxChange: form.handleCheckboxChange, validateForm: form.validateForm, resetForm: form.resetForm };
};
