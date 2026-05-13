import { useEffect, useMemo, useRef, useState } from 'react';
import { bragLogValidationRules } from '../../utils';
import { BragLogDTO } from '../../services';
import { fullName } from '../../utils';
import { useForm } from '../index';
import {
    fetchStudentByToken,
    searchStudentsInList,
    fetchStudents,
    fetchTeachers,
    searchBehaviorTypesInList,
    useAppSelector,
    useAppDispatch,
} from '../../store';

export interface UseBragLogFormProps {
    show: boolean;
    isEdit?: boolean;
    bragLog?: BragLogDTO | null;
    isPublic?: boolean;
    studentToken?: string;
}

export const useBragLogForm = ({ show, isEdit = false, bragLog, isPublic = false,
                                   studentToken }: UseBragLogFormProps) => {
    const dispatch = useAppDispatch();
    const { loading: bragLogLoading, error: bragLogError } = useAppSelector(state =>
        state.bragLogs);
    const { data: behaviorTypes, currentParams } = useAppSelector(state => state.behaviorTypes);
    const { data: students, loading: studentsLoading, error: studentsError, selectedStudent } = useAppSelector(state =>
        state.students);
    const { data: teachers } = useAppSelector(state => state.teachers);
    const currentUser = useAppSelector(state => state.user.data);
    const prevIdRef = useRef<number | null | undefined>(null);
    const hasResetForCurrentShowRef = useRef(false);
    const hasSetSelectedTeacherRef = useRef(false);
    const hasFetchedBehaviorTypesRef = useRef(false);

    const [selectedTeacherId, setSelectedTeacherId] = useState<number | null>(null);

    const initialData: BragLogDTO = {
        studentId: 0,
        teacherId: 0,
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

    // Fetch active behavior types
    useEffect(() => {
        if (show && !hasFetchedBehaviorTypesRef.current || !currentParams?.active) {
            dispatch(searchBehaviorTypesInList({ active: true, page: 0, size: 100, force: false }));
            hasFetchedBehaviorTypesRef.current = true;
        }
    }, [show, behaviorTypes.length, dispatch, currentParams]);

    // Reset state when modal closes
    useEffect(() => {
        if (!show) {
            prevIdRef.current = null;
            hasResetForCurrentShowRef.current = false;
            hasSetSelectedTeacherRef.current = false;
            hasFetchedBehaviorTypesRef.current = false;
            setSelectedTeacherId(null);
        }
    }, [show]);

    // Fetch teachers for internal form
    useEffect(() => {
        if (show && !isPublic && teachers.length === 0) {
            dispatch(fetchTeachers({ page: 0, size: 1000, force: true }));
        }
    }, [show, isPublic, teachers.length, dispatch])

    // Initialize form data for create/edit
    useEffect(() => {
        if (!show) return;
        if (isEdit && bragLog && bragLog.id !== prevIdRef.current) {
            form.setFormData({
                ...bragLog,
                notes: bragLog.notes || '',
            });
            prevIdRef.current = bragLog.id;
        } else if (!isEdit && !hasResetForCurrentShowRef.current) {
            form.resetForm();
            if (!isPublic && currentUser && fullName(currentUser)) {
                form.setFormData(prev => ({ ...prev, submitterName: fullName(currentUser )}));
            }
            hasResetForCurrentShowRef.current = true;
        }
    }, [show, isEdit, bragLog, form, isPublic, currentUser]);

    // Public mode: when selectedStudent is loaded, populate studentId and teacherId
    useEffect(() => {
        if (show && isPublic && selectedStudent && selectedStudent.id) {
            form.setFormData(prev => ({
                ...prev,
                studentId: selectedStudent.id!,
                teacherId: selectedStudent.teacher.id!,
            }));
            hasSetSelectedTeacherRef.current = true;
        }
    }, [show, isPublic, selectedStudent, form]);

    // Public mode: fetch student by token
    useEffect(() => {
        if (show && isPublic && studentToken) {
            dispatch(fetchStudentByToken(studentToken));
        }
    }, [show, isPublic, studentToken, dispatch]);

    // Internal mode: when teacherId changes in form, update selectedTeacherId to filter students
    useEffect(() => {
        if (show && !isPublic && form.formData.teacherId && form.formData.teacherId !== selectedTeacherId) {
            setSelectedTeacherId(form.formData.teacherId);
        } else if (show && !isPublic && form.formData.teacherId === 0 && selectedTeacherId !== null) {
            setSelectedTeacherId(null);
        }
    }, [show, isPublic, form.formData.teacherId, selectedTeacherId]);

    // Internal mode: fetch students based on selectedTeacherId
    useEffect(() => {
        if (!show || isPublic) return;
        if (selectedTeacherId) {
            dispatch(searchStudentsInList({
                page: 0,
                size: 100,
                teacherId: selectedTeacherId,
                force: true,
            }));
        } else if (!selectedTeacherId && students.length === 0) {
            dispatch(fetchStudents({
                page: 0,
                size: 100,
                force: true,
            }));
        }
    }, [selectedTeacherId, dispatch, isPublic, students.length, show]);

    // Internal mode: when student is selected, automatically set teacherId
    useEffect(() => {
        if (!show || isEdit || isPublic) return;
        const studentId = form.formData.studentId;
        if (studentId) {
            const selectedStudentObj = students.find(s => s.id === studentId);
            if (selectedStudentObj?.teacher?.id && selectedStudentObj.teacher.id !== selectedTeacherId) {
                setSelectedTeacherId(selectedStudentObj.teacher.id);
                form.setFormData(prev => ({ ...prev, teacherId: selectedStudentObj.teacher.id! }));
            }
        }
    }, [students, isEdit, isPublic, selectedTeacherId, show, form]);

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

    const loading = bragLogLoading || (isPublic && studentsLoading);
    const error = bragLogError || (isPublic ? studentsError : null);

    return {
        formData: form.formData,
        setFormData: form.setFormData,
        formErrors: form.formErrors,
        setFormErrors: form.setFormErrors,
        currentUser,
        error,
        loading,
        students,
        teachers,
        behaviorTypes: behaviorTypes.filter(bt => bt.active),
        selectedTeacherId,
        setSelectedTeacherId,
        selectedStudent,
        totalPoints,
        handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange,
        toggleBehavior,
        validateForm: form.validateForm,
        resetForm: form.resetForm,
    };
}
