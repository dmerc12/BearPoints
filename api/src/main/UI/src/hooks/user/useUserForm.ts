import { userValidationRules } from '../../utils';
import { UserDTO, Role } from '../../services';
import { useAppSelector } from '../../store';
import { useForm } from '../index';
import { useEffect } from 'react';

export interface UseUserFormProps {
    show: boolean;
    isEdit?: boolean;
    user?: UserDTO | null;
}

export const useUserForm = ({ show, isEdit = false, user }: UseUserFormProps) => {
    const { loading, error } = useAppSelector(state => state.users);
    const currentUser = useAppSelector(state => state.user.data);

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
        if (show && !isEdit) {
            form.resetForm();
        }
    }, [show, isEdit, form]);

    useEffect(() => {
        if (show && isEdit && user) {
            form.setFormData({
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                role: user.role,
            });
        } else if (show && !isEdit) {
            form.resetForm();
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