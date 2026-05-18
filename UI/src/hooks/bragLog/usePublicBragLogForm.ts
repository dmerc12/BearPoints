import { useEffect, useRef, useState, FormEvent } from 'react';
import { useBragLogForm } from '../index';
import { BragLogDTO } from '../../services';
import {
    fetchStudentByToken,
    searchBehaviorTypesInList,
    addBragLog,
    useAppSelector,
    useAppDispatch,
} from '../../store';

export const usePublicBragLogForm = (studentToken: string) => {
    const dispatch = useAppDispatch();
    const { data: behaviorTypes, currentParams } = useAppSelector(state => state.behaviorTypes);
    const { loading, error, selectedStudent } = useAppSelector(state => state.students);
    const hasFetchedBehaviorTypesRef = useRef(false);
    const [ success, setSuccess ] = useState(false);
    const hasSetFormDataRef = useRef(false);

    const { form, totalPoints, toggleBehavior } = useBragLogForm();

    useEffect(() => {
        hasSetFormDataRef.current = false;
    }, [studentToken]);

    useEffect(() => {
        if (!hasFetchedBehaviorTypesRef.current || !currentParams?.active) {
            dispatch(searchBehaviorTypesInList({ active: true, page: 0, size: 100, force: false }));
            hasFetchedBehaviorTypesRef.current = true;
        }
    }, [behaviorTypes.length, dispatch, currentParams]);

    useEffect(() => {
        if (studentToken) {
            dispatch(fetchStudentByToken(studentToken));
        }
    }, [studentToken, dispatch]);

    useEffect(() => {
        if (selectedStudent?.id && !hasSetFormDataRef.current) {
            hasSetFormDataRef.current = true;
            form.setFormData(prev => ({
                ...prev,
                studentId: selectedStudent.id!,
            }));
        }
    }, [selectedStudent, form]);

    const handleSubmit = async (event: FormEvent) => {
        event.preventDefault();
        if (!form.validateForm()) return;
        try {

            const bragLogData: BragLogDTO = {
                studentId: form.formData.studentId,
                behaviorIds: form.formData.behaviorIds,
                notes: form.formData.notes,
                submitterName: form.formData.submitterName,
            };
            const result = await dispatch(addBragLog(bragLogData));
            if (addBragLog.fulfilled.match(result)) {
                form.resetForm();
                setSuccess(true);
                const timer = setTimeout(() => {
                    setSuccess(false);
                }, 3000);
                return () => clearTimeout(timer);
            }
        } catch (error) {
            console.error('Submission failed:', error);
        }
    };

    return {
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        error,
        loading,
        success,
        behaviorTypes: behaviorTypes.filter(bt => bt.active),
        selectedStudent,
        totalPoints,
        handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange,
        validateForm: form.validateForm,
        resetForm: form.resetForm,
        toggleBehavior,
        handleSubmit,
    };
}
