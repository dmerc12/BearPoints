import { fetchStudentByToken, searchStudentsInList, fetchStudents, useAppSelector, useAppDispatch } from '../../store';
import { BragLogDTO, Role } from '../../services';
import { useEffect, useMemo, useState } from 'react';
import { bragLogValidationRules } from '../../utils';
import { fullName } from '../../utils';
import { useForm } from '../index';

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
    const { data: behaviorTypes } = useAppSelector(state => state.behaviorTypes);
    const { data: students, loading: studentsLoading, error: studentsError, selectedStudent } = useAppSelector(state =>
        state.students);
    const { data: teachers } = useAppSelector(state => state.teachers);
    const currentUser = useAppSelector(state => state.user.data);

    const [selectedTeacherId, setSelectedTeacherId] = useState<number | null>(null);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

    const initialData: BragLogDTO = {
        studentId: 0,
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

    useEffect(() => {
        if (show && !isEdit && !isPublic && currentUser) {
            const name = fullName(currentUser);
            if (name && !form.formData.submitterName) {
                form.setFormData(prev => ({ ...prev, submitterName: name }));
            }
        }
    }, [show, isEdit, isPublic, currentUser, form]);

    useEffect(() => {
        if (!isPublic && selectedTeacherId) {
            dispatch(searchStudentsInList({
                page: 0,
                size: 100,
                teacherId: selectedTeacherId,
                force: true,
            }));
        } else if (!isPublic && !selectedTeacherId && students.length === 0) {
            dispatch(fetchStudents({
                page: 0,
                size: 100,
                force: true,
            }));
        }
    }, [selectedTeacherId, dispatch, isPublic, students.length]);

    useEffect(() => {
        if (isPublic && studentToken) {
            dispatch(fetchStudentByToken(studentToken));
        }
    }, [isPublic, studentToken, dispatch]);

    useEffect(() => {
        if (isPublic && selectedStudent && selectedStudent.id) {
            form.setFormData(prev => ({ ...prev, studentId: selectedStudent.id! }));
        }
    }, [isPublic, selectedStudent, form]);

    useEffect(() => {
        if (show && isEdit && bragLog) {
            form.setFormData({
                ...bragLog,
                notes: bragLog.notes || '',
            });
        }
    }, [show, isEdit, bragLog, form]);

    useEffect(() => {
        if (form.formData.studentId && !isEdit && !isPublic) {
            const selectedStudentObj = students.find(s =>
                s.id === form.formData.studentId);
            if (selectedStudentObj?.teacher?.id && selectedStudentObj.teacher.id !== selectedTeacherId) {
                setSelectedTeacherId(selectedStudentObj.teacher.id);
            }
        }
    }, [form.formData.studentId, students, isEdit, isPublic, selectedTeacherId]);

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
        isAdmin,
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
