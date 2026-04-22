import { useAppSelector, useAppDispatch, fetchStudents, fetchRewardItems } from '../../store';
import { studentRewardValidationRules } from '../../utils';
import { StudentRewardDTO, Role } from '../../services';
import { useEffect, useMemo } from 'react';
import { useForm } from '../index';

export interface UseStudentRewardFormProps {
    show: boolean;
    isEdit?: boolean;
    studentReward?: StudentRewardDTO | null;
}

export const useStudentRewardForm = ({ show, isEdit = false, studentReward }
                                     : UseStudentRewardFormProps) => {
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector(state => state.studentRewards);
    const { data: students, loading: studentsLoading } = useAppSelector(state => state.students);
    const { data: rewardItems, loading: rewardItemsLoading } = useAppSelector(state => state.rewardItems);
    const currentUser = useAppSelector(state => state.user.data);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

    useEffect(() => {
        if (students.length === 0 && !studentsLoading) {
            dispatch(fetchStudents({ page: 0, size: 100, force: true }));
        }
        if (rewardItems.length === 0 && !rewardItemsLoading) {
            dispatch(fetchRewardItems({ page: 0, size: 100, force: true }));
        }
    }, [students.length, studentsLoading, rewardItems.length, rewardItemsLoading, dispatch]);

    const initialData: StudentRewardDTO = {
        studentId: 0,
        itemId: 0,
    };

    const form = useForm({
        initialData,
        validationRules: studentRewardValidationRules,
    });

    useEffect(() => {
        if (show && !isEdit) {
            form.resetForm();
        }
    }, [show, isEdit, form]);

    useEffect(() => {
        if (show && isEdit && studentReward) {
            form.setFormData({
                studentId: studentReward.studentId,
                itemId: studentReward.itemId,
            });
        }
    }, [show, isEdit, studentReward, form]);

    const isLoading = loading || studentsLoading || rewardItemsLoading;

    return {
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        setFormErrors: form.setFormErrors,
        currentUser,
        error,
        loading: isLoading,
        isAdmin,
        students,
        rewardItems,
        handleSelectChange: form.handleSelectChange,
        validateForm: form.validateForm,
        resetForm: form.resetForm,
    };
};
