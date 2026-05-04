import { useAppSelector, useAppDispatch, fetchTeachers } from '../../store';
import { studentValidationRules, fullName } from '../../utils';
import { StudentDTO } from '../../services';
import { useEffect, useMemo } from 'react';
import { useForm } from '../index';

export interface UseStudentFormProps {
    show: boolean;
    isEdit?: boolean;
    student?: StudentDTO | null;
    defaultTeacherId?: number;
}

export const useStudentForm = ({ show, isEdit = false, student, defaultTeacherId }: UseStudentFormProps) => {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(state => state.user.data);
    const { loading: studentsLoading, error: studentsError } = useAppSelector(
        state => state.students);
    const { data: teachers, loading: teachersLoading, error: teachersError } = useAppSelector(
        state => state.teachers);

    const initialData = {
        firstName: '',
        lastName: '',
        email: '',
        teacherId: -1,
    };

    const form = useForm({ initialData, validationRules: studentValidationRules });

    const error = studentsError || teachersError;
    const isLoading = studentsLoading || teachersLoading;

    useEffect(() => {
        if (show && teachers.length === 0) {
            dispatch(fetchTeachers({ page: 0, size: 1000, force: true }));
        }
    }, [show, dispatch, teachers.length]);

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

    useEffect(() => {
        if (show && !isEdit && defaultTeacherId && defaultTeacherId !== -1) {
            form.setFormData(prev => ({ ...prev, teacherId: defaultTeacherId }));
        }
    }, [show, isEdit, defaultTeacherId, form]);

    const selectedTeacherName = useMemo(() => {
        if (!defaultTeacherId || defaultTeacherId === -1) return '';
        if (currentUser?.teacherId === defaultTeacherId) {
            return fullName(currentUser);
        }
        const selectedTeacher = teachers.find(t => t.id === defaultTeacherId);
        return selectedTeacher ? fullName(selectedTeacher) : '';
    }, [defaultTeacherId, currentUser, teachers]);

    const isTeacherSelectDisabled = !isEdit && !!defaultTeacherId && defaultTeacherId !== -1;

    return {
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        setFormErrors: form.setFormErrors,
        teachers,
        error,
        isLoading,
        selectedTeacherName,
        isTeacherSelectDisabled,
        handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange,
        handleCheckboxChange: form.handleCheckboxChange,
        validateForm: form.validateForm,
        resetForm: form.resetForm
    };
};
