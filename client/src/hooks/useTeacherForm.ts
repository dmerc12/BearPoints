import { Teacher, Role, GradeLevel } from '../services/types';
import { createFormHandlers } from '../utils/handleChange';
import { useAppSelector } from '../store/hooks';
import { useEffect, useState } from 'react';

export interface TeacherFormData {
    firstName: string;
    lastName: string;
    email: string;
    grade: GradeLevel | null;
}

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

    const [formErrors, setFormErrors] = useState<Record<string, string>>({});
    const [formData, setFormData] = useState<TeacherFormData>({
        firstName: '',
        lastName: '',
        email: '',
        grade: null
    });

    const isAdmin = currentUser?.role === Role.ADMIN;
    const isTeacher = currentUser?.role === Role.TEACHER;

    useEffect(() => {
        if (show && isEdit && teacher) {
            setFormData({
                firstName: teacher.user.firstName,
                lastName: teacher.user.lastName,
                email: teacher.user.email,
                grade: teacher.grade
            });
        }
    }, [show, isEdit, teacher]);

    const { handleInputChange, handleSelectChange } = createFormHandlers(setFormData, setFormErrors);
    
    const validateForm = () => {
        const errors: Record<string, string> = {};
        if (!formData.firstName.trim()) errors.firstName = 'First name is required';
        if (!formData.lastName.trim()) errors.lastName = 'Last name is required';
        if (!formData.email.trim()) {
            errors.email = 'Email is required';
        } else if (!formData.email.endsWith('@okcps.org')) {
            errors.email = 'Email must be from @okcps.org';
        }
        if (!formData.grade) errors.grade = 'Grade is required';
        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };
    
    const resetForm = () => {
        setFormData({
            firstName: '',
            lastName: '',
            email: '',
            grade: null
        });
        setFormErrors({});
    }
    
    return { formData, setFormData, formErrors, setFormErrors, currentUser, isAdmin, isTeacher, error, loading, 
        handleInputChange, handleSelectChange, validateForm, resetForm };
};
