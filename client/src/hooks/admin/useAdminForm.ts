import { UserDTO, PersonFormData } from '../../services';
import { personValidationRules } from '../../utils';
import { useAppSelector } from '../../store';
import { useForm } from '../index';
import { useEffect } from 'react';

export interface UseAdminFormProps {
    show: boolean;
    isEdit?: boolean;
    admin?: UserDTO | null;
}

export const useAdminForm = ({ show, isEdit = false, admin }: UseAdminFormProps) => {
    const { loading, error } = useAppSelector(state => state.admins);
    const currentUser = useAppSelector(state => state.user.data);

    const initialData: PersonFormData = {
        firstName: '',
        lastName: '',
        email: ''
    };

    const form = useForm({
        initialData,
        validationRules: personValidationRules
    });

    useEffect(() => {
        if (show && isEdit && admin) {
            form.setFormData({
                firstName: admin.firstName,
                lastName: admin.lastName,
                email: admin.email,
            });
        }
    }, [show, isEdit, admin, form]);

    return {
        formData: form.formData, setFormData: form.setFormData, formErrors: form.formErrors,
        setFormErrors: form.setFormErrors, currentUser, error, loading, handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange, handleCheckboxChange: form.handleCheckboxChange,
        validateForm: form.validateForm, resetForm: form.resetForm
    };
}