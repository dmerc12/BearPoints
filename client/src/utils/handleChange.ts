import React, { Dispatch, SetStateAction } from 'react';

export interface FormHandlers {
    handleInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    handleSelectChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
}

export const createFormHandlers =
    <T>(setFormData: Dispatch<SetStateAction<T>>, setFormErrors: Dispatch<SetStateAction<Record<string, string>>>)
        : FormHandlers => {
    const handleChange = (name: string, value: string | number) => {
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        setFormErrors(prev => {
            const newErrors = { ...prev };
            delete newErrors[name];
            return newErrors;
        });
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        handleChange(e.target.name, e.target.value);
    };

    const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        handleChange(e.target.name, e.target.value);
    };

    return { handleInputChange, handleSelectChange };
};
