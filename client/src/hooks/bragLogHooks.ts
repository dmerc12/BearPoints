import { RootState, useAppSelector, fetchBragLogs } from '../store';
import { BragLogFormData, bragLogValidationRules } from '../utils';
import { useTable, useForm } from './index';
import React, { useEffect } from 'react';
import { BragLog } from '../services';

export interface UseBragLogFormProps {
    show: boolean;
    isEdit?: boolean;
    bragLog?: BragLog | null;
}

export const useBragLogForm = ({ show, isEdit = false, bragLog }: UseBragLogFormProps) => {
    const { loading, error } = useAppSelector(state => state.bragLogs);
    const { data: behaviorTypes } = useAppSelector(state => state.behaviorTypes);
    const { data: students } = useAppSelector(state => state.students);
    const { data: teachers } = useAppSelector(state => state.teachers);
    const currentUser = useAppSelector(state => state.user.data);

    const initialData: BragLogFormData = {
        studentId: '',
        teacherId: '',
        behaviorIs: [],
        notes: '',
        pointsGenerated: 0
    };

    const form = useForm({
        initialData,
        validationRules: bragLogValidationRules
    });

    useEffect(() => {
        if (show && isEdit && bragLog) {
            form.setFormData({
                studentId: bragLog.student.id.toString(),
                teacherId: bragLog.teacher.id.toString(),
                behaviorIs: bragLog.behaviors.map(b => b.id.toString()),
                notes: bragLog.notes || '',
                pointsGenerated: bragLog.pointsGenerated
            });
        }
    }, [show, isEdit, bragLog, form]);

    useEffect(() => {
        if (form.formData.behaviorIs && behaviorTypes.length > 0) {
            const selectedBehaviors = behaviorTypes.filter(bt =>
                form.formData.behaviorIs.includes(bt.id.toString())
            );
            const totalPoints = selectedBehaviors.reduce((sum, bt) =>
                sum + bt.pointValue, 0);
            form.setFormData(prev => ({
                ...prev,
                pointsGenerated: totalPoints
            }));
        }
    }, [form, behaviorTypes]);

    const handleMultiSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const selectedOptions = Array.from(e.target.selectedOptions, option => option.value);
        form.setFormData(prev => ({
            ...prev,
            [e.target.name]: selectedOptions,
        }));
        form.setFormErrors(prev => {
            const newErrors = { ...prev };
            delete newErrors[e.target.name];
            return newErrors;
        });
    };

    return {
        formData: form.formData, setFormData: form.setFormData, formErrors: form.formErrors,
        setFormErrors: form.setFormErrors, currentUser, error, loading, students, teachers, handleMultiSelectChange,
        behaviorTypes: behaviorTypes.filter(bt => bt.active), handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange, validateForm: form.validateForm, resetForm: form.resetForm
    };
}

export function useBragLogTable() {
    return useTable<BragLog, { studentFilter: string; teacherFilter: string; dateFilter: string }>({
        fetchAction: fetchBragLogs,
        selector: (state: RootState) => state.bragLogs,
        initialFilters: {
            studentFilter: '',
            teacherFilter: '',
            dateFilter: ''
        }
    });
}
