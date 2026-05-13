import { useAppSelector, useAppDispatch, fetchStudents, fetchRewardItems } from '../../store';
import { studentRewardValidationRules } from '../../utils';
import { StudentRewardDTO, Role } from '../../services';
import { useEffect, useMemo, useRef } from 'react';
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
    const prevIdRef = useRef<number | null | undefined>(null);
    const hasResetForCurrentShowRef = useRef(false);
    const hasFetchedDataRef = useRef(false);

    const isStudent = useMemo(() => currentUser?.role === Role.STUDENT, [currentUser]);

    useEffect(() => {
        if (!hasFetchedDataRef.current) {
            if (students.length === 0 && !studentsLoading) {
                dispatch(fetchStudents({ page: 0, size: 100, force: true }));
            }
            if (rewardItems.length === 0 && !rewardItemsLoading) {
                dispatch(fetchRewardItems({ page: 0, size: 100, force: true }));
            }
            hasFetchedDataRef.current = true;
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
        if (!show) {
            prevIdRef.current = null;
            hasResetForCurrentShowRef.current = false;
            hasFetchedDataRef.current = false;
        }
    }, [show]);

    useEffect(() => {
        if (!show) return;
        if (isEdit && studentReward && studentReward.id !== prevIdRef.current) {
            form.setFormData({
                studentId: studentReward.studentId,
                itemId: studentReward.itemId,
            });
            prevIdRef.current = studentReward.id;
        } else if (!isEdit && !hasResetForCurrentShowRef.current) {
            form.resetForm();
            hasResetForCurrentShowRef.current = true;
        }
    }, [show, isEdit, studentReward, form]);

    useEffect(() => {
        if (show && isStudent && currentUser?.studentId && !isEdit && !hasResetForCurrentShowRef.current) {
            form.setFormData({ studentId: currentUser.studentId!, itemId: 0 });
        }
    }, [show, isStudent, isEdit, currentUser, form]);

    const isLoading = loading || studentsLoading || rewardItemsLoading;

    return {
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        setFormErrors: form.setFormErrors,
        error,
        loading: isLoading,
        students,
        rewardItems,
        isStudent,
        handleSelectChange: form.handleSelectChange,
        validateForm: form.validateForm,
        resetForm: form.resetForm,
    };
};
