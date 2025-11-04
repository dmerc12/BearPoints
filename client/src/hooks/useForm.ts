import React, { useState, useCallback } from 'react';
import { createFormHandlers } from '../utils';

export type FormData = Record<string, unknown>;

export interface ValidationRule {
    field: string;
    validator: (value: unknown, formData: FormData) => string | null;
}

export interface UseFormProps<T> {
    initialData: T;
    validationRules: ValidationRule[];
    onSubmit?: (data: T) => void;
}

export function useForm<T>({ initialData, validationRules, onSubmit }: UseFormProps<T>) {
    const [formData, setFormData] = useState<T>(initialData);
    const [formErrors, setFormErrors] = useState<Record<string, string>>({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    const validateField = useCallback((field: string, value: unknown) => {
        const rule = validationRules.find(r => r.field === field);
        if (!rule) return null;
        return rule.validator(value, formData as FormData);
    }, [validationRules, formData]);

    const validateForm = useCallback(() => {
        const errors: Record<string, string> = {};
        validationRules.forEach(rule => {
            const value = formData[rule.field as keyof T] as unknown;
            const error = validateField(rule.field, value);
            if (error) {
                errors[rule.field] = error;
            }
        });
        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    }, [validationRules, validateField, formData]);

    const { handleInputChange, handleSelectChange, handleCheckboxChange } = createFormHandlers(setFormData, setFormErrors);

    const handleSubmit = useCallback(async (e?: React.FormEvent) => {
        e?.preventDefault();
        if(!validateForm()) return false;
        setIsSubmitting(true);
        try {
            if (onSubmit) {
                onSubmit(formData);
            }
            return true
        } catch (error) {
            console.error('Form submission error:', error);
            return false;
        } finally {
            setIsSubmitting(false);
        }
    }, [validateForm, onSubmit, formData]);

    const resetForm = useCallback(() => {
        setFormData(initialData);
        setFormErrors({});
    }, [initialData]);

    return { formData, formErrors, isSubmitting, handleInputChange, handleSelectChange, handleCheckboxChange,
        handleSubmit, validateForm, resetForm, setFormData, setFormErrors };
}
