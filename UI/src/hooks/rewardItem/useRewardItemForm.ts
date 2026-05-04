import { rewardItemValidationRules } from '../../utils';
import { RewardItemDTO } from '../../services';
import { useAppSelector } from '../../store';
import { useForm } from '../index';
import { useEffect } from 'react';

export interface UseRewardItemFormProps {
    show: boolean;
    isEdit?: boolean;
    rewardItem?: RewardItemDTO | null;
}

export const useRewardItemForm = ({ show, isEdit = false, rewardItem }: UseRewardItemFormProps) => {
    const { loading, error } = useAppSelector(state => state.rewardItems);

    const initialData = {
        name: '',
        pointCost: 0,
        stock: 0,
    };

    const form = useForm({
        initialData,
        validationRules: rewardItemValidationRules
    });

    useEffect(() => {
        if (show && !isEdit) {
            form.resetForm();
        }
    }, [show, isEdit, form]);

    useEffect(() => {
        if (show && isEdit && rewardItem) {
            form.setFormData({
                name: rewardItem.name,
                pointCost: rewardItem.pointCost,
                stock: rewardItem.stock,
            })
        }
    }, [show, isEdit, rewardItem, form]);

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
        resetForm: form.resetForm,
    };
};
