import { useEffect, useRef, useState } from 'react';
import { BragLogDTO } from '../../services';
import { useBragLogForm } from '../index';
import { fullName } from '../../utils';
import {
    searchStudentsInList,
    fetchTeachers,
    searchBehaviorTypesInList,
    useAppSelector,
    useAppDispatch,
} from '../../store';

export interface UseInternalBragLogFormProps {
    show: boolean;
    isEdit?: boolean;
    bragLog?: BragLogDTO | null;
    onCancel?: () => void;
}

export const useInternalBragLogForm = ({ show, isEdit = false, bragLog, onCancel }
                                       : UseInternalBragLogFormProps) => {
    const dispatch = useAppDispatch();
    const { loading: bragLogLoading, error: bragLogError } = useAppSelector(state =>
        state.bragLogs);
    const { data: behaviorTypes, currentParams } = useAppSelector(state => state.behaviorTypes);
    const { data: students, loading: studentsLoading, error: studentsError } = useAppSelector(state =>
        state.students);
    const { data: teachers, loading: teachersLoading, error: teachersError } = useAppSelector(state =>
        state.teachers);
    const currentUser = useAppSelector(state => state.user.data);
    const prevIdRef = useRef<number | null | undefined>(null);
    const hasResetForCurrentShowRef = useRef(false);
    const hasFetchedBehaviorTypesRef = useRef(false);

    const [selectedTeacherId, setSelectedTeacherId] = useState<number | null | undefined>(null);
    const [selectedStudentId, setSelectedStudentId] = useState<number | null | undefined>(null);

    const { form, totalPoints, toggleBehavior } = useBragLogForm();

    // Fetch active behavior types
    useEffect(() => {
        if ((!hasFetchedBehaviorTypesRef.current && behaviorTypes.length === 0) || !currentParams?.active) {
            dispatch(searchBehaviorTypesInList({ active: true, page: 0, size: 100, force: false }));
            hasFetchedBehaviorTypesRef.current = true;
        }
    }, [show, behaviorTypes.length, dispatch, currentParams]);

    // Reset state when modal closes
    useEffect(() => {
        if (!show) {
            prevIdRef.current = null;
            hasResetForCurrentShowRef.current = false;
            setSelectedTeacherId(null);
        }
    }, [show]);

    // Fetch teachers (once)
    useEffect(() => {
        if (show && teachers.length === 0) {
            dispatch(fetchTeachers({ page: 0, size: 1000, force: true }));
        }
    }, [show, teachers.length, dispatch]);

    // Filter current user out of dropdowns using user.id
    const filteredTeachers = teachers.filter(t => t.user.role === 'TEACHER');
    const filteredStudents = students.filter(s => s.user.role === 'STUDENT');

    // Initialize form data for create/edit
    useEffect(() => {
        if (!show) return;
        if (isEdit && bragLog && bragLog.id !== prevIdRef.current) {
            form.setFormData({
                studentId: bragLog.studentId,
                teacherId: bragLog.teacherId,
                behaviorIds: bragLog.behaviorIds,
                notes: bragLog.notes || '',
                submitterName: bragLog.submitterName,
            });
            setSelectedTeacherId(bragLog.teacherId);
            prevIdRef.current = bragLog.id;
        } else if (!isEdit && !hasResetForCurrentShowRef.current) {
            form.resetForm();
            if (currentUser && fullName(currentUser)) {
                form.setFormData(prev => ({ ...prev, submitterName: fullName(currentUser) }));
            }
            setSelectedTeacherId(null);
            setSelectedStudentId(null);
            hasResetForCurrentShowRef.current = true;
        }
    }, [show, isEdit, bragLog, form, currentUser]);

    // Sync selectedTeacherId with formData.teacherId
    useEffect(() => {
        if (!show || isEdit) return;
        const currentTeacherId = form.formData.teacherId;
        if (currentTeacherId && currentTeacherId !== selectedTeacherId) {
            setSelectedTeacherId(currentTeacherId);
        } else if (!currentTeacherId && selectedTeacherId !== null) {
            setSelectedTeacherId(null);
        }
    }, [form.formData.teacherId, selectedTeacherId, show, isEdit]);

    // Sync selectedStudentId with formData.studentId
    useEffect(() => {
        if (!show || isEdit) return;
        const currentStudentId = form.formData.studentId;
        if (currentStudentId && currentStudentId !== selectedStudentId) {
            setSelectedStudentId(currentStudentId);
        } else if (!currentStudentId && selectedStudentId !== null) {
            setSelectedStudentId(selectedStudentId);
        }
    }, [form, selectedStudentId, show, isEdit]);

    // When teacher selection changes, filter students
    useEffect(() => {
        if (show && !isEdit && selectedTeacherId !== undefined) {
            dispatch(searchStudentsInList({
                page: 0,
                size: 1000,
                teacherId: selectedTeacherId || undefined,
                force: true,
            }));
        }
    }, [show, isEdit, selectedTeacherId, dispatch]);

    const handleClose = () => {
        form.resetForm();
        onCancel?.();
    };

    const loading = bragLogLoading || studentsLoading || teachersLoading;
    const error = bragLogError || studentsError || teachersError;

    return {
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        setFormErrors: form.setFormErrors,
        currentUser,
        error,
        loading,
        students: filteredStudents,
        teachers: filteredTeachers,
        behaviorTypes: behaviorTypes.filter(bt => bt.active),
        selectedTeacherId,
        setSelectedTeacherId,
        totalPoints,
        handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange,
        validateForm: form.validateForm,
        resetForm: form.resetForm,
        toggleBehavior,
        handleClose,
    };
};
