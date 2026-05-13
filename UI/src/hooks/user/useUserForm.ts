import { userValidationRules } from '../../utils';
import { UserDTO, Role } from '../../services';
import { useAppSelector } from '../../store';
import { useEffect, useRef } from 'react';
import { useForm } from '../index';

export interface UseUserFormProps {
    show: boolean;
    isEdit?: boolean;
    user?: UserDTO | null;
}

export const useUserForm = ({ show, isEdit = false, user }: UseUserFormProps) => {
    const { loading, error } = useAppSelector(state => state.users);
    const currentUser = useAppSelector(state => state.user.data);
    const prevIdRef = useRef<number | null | undefined>(null);
    const hasResetForCurrentShowRef = useRef(false);

    const initialData = {
        firstName: '',
        lastName: '',
        email: '',
        role: Role.ADMIN,
    };

    const form = useForm({
        initialData,
        validationRules: userValidationRules
    });

    useEffect(() => {
        if (!show) {
            prevIdRef.current = null;
            hasResetForCurrentShowRef.current = false;
        }
    }, [show]);

    useEffect(() => {
        if (!show) return;
        if (isEdit && user && user.id !== prevIdRef.current) {
            form.setFormData({
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                role: user.role,
            });
            prevIdRef.current = user.id;
        } else if (!isEdit && !hasResetForCurrentShowRef.current) {
            form.resetForm();
            hasResetForCurrentShowRef.current = true;
        }
    }, [show, isEdit, user, form]);

    return {
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        setFormErrors: form.setFormErrors,
        currentUser,
        error,
        loading,
        handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange,
        handleCheckboxChange: form.handleCheckboxChange,
        validateForm: form.validateForm,
        resetForm: form.resetForm
    };
}