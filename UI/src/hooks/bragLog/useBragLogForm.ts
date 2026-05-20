import { bragLogValidationRules } from '../../utils';
import { useAppSelector } from '../../store';
import { BragLogDTO } from '../../services';
import { useForm } from '../index';
import { useMemo } from 'react';

export const useBragLogForm = () => {
    const { data: behaviorTypes } = useAppSelector(state => state.behaviorTypes);

    const initialData: BragLogDTO = {
        studentId: -1,
        teacherId: -1,
        behaviorIds: [],
        notes: '',
        submitterName: ''
    };

    const form = useForm({
        initialData,
        validationRules: bragLogValidationRules
    });

    const totalPoints = useMemo(() => {
        const ids = form.formData?.behaviorIds || [];
        return ids.reduce((sum, id) => {
            const behavior = behaviorTypes.find(bt => bt.id === id);
            return sum + (behavior?.pointValue || 0);
        }, 0);
    }, [form.formData.behaviorIds, behaviorTypes]);

    const toggleBehavior = (behaviorId: number) => {
        const current = form.formData.behaviorIds || [];
        const newIds = current.includes(behaviorId)
            ? current.filter(id => id !== behaviorId)
            : [...current, behaviorId];
        form.setFormData(prev => ({
            ...prev,
            behaviorIds: newIds
        }));
        form.setFormErrors(prev => {
            const newErrors = { ...prev };
            delete newErrors.behaviorIds;
            return newErrors;
        });
    };

    return { form, totalPoints, toggleBehavior };
}
