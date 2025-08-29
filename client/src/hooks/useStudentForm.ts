import { useAppSelector, useAppDispatch } from '../store/hooks';
import { fetchTeachers } from '../store/slices/teachersSlice';
import { createFormHandlers } from '../utils/handleChange';
import { Student, Role } from '../services/types';
import { useEffect, useState } from 'react';

export interface StudentFormData {
    firstName: string;
    lastName: string;
    email: string;
    teacherId: string;
}

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

    const [formErrors, setFormErrors] = useState<Record<string, string>>({});
    const [formData, setFormData] = useState<StudentFormData>({
        firstName: '',
        lastName: '',
        email: '',
        teacherId: '',
    });

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
            setFormData({
                firstName: student.user.firstName,
                lastName: student.user.lastName,
                email: student.user.email,
                teacherId: student.teacher.id.toString()
            });
        } else if (show && isTeacher && currentUser?.teacherId) {
            setFormData(prev => ({
                ...prev,
                teacherId: currentUser.teacherId ? currentUser.teacherId.toString() : '',
            }));
        }
    }, [show, isEdit, student, isTeacher, currentUser]);

    const { handleInputChange, handleSelectChange } = createFormHandlers(setFormData, setFormErrors);

    const validateForm = () => {
        const errors: Record<string, string> = {};
        if (!formData.firstName.trim()) errors.firstName = 'First name is required';
        if (!formData.lastName.trim()) errors.lastName = 'Last name is required';
        if (!formData.email.trim()) {
            errors.email = 'Email is required';
        } else if (!formData.email.endsWith('@okcps.org')) {
            errors.email = 'Email must be from @okcps.org domain';
        }
        if (!formData.teacherId.trim()) errors.teacherId = 'Teacher Id is required';
        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const resetForm = () => {
        setFormData({
            firstName: '',
            lastName: '',
            email: '',
            teacherId: '',
        });
        setFormErrors({});
    }

    return { formData, setFormData, formErrors, setFormErrors, teachers, currentUser, isAdmin, isTeacher, error,
        isLoading, handleInputChange, handleSelectChange, validateForm, resetForm };
};
