import { BragLogDTO, Role, Student, BragLogFormData } from '../../services';
import { useEffect, useMemo, useState } from 'react';
import { bragLogValidationRules } from '../../utils';
import { useAppSelector } from '../../store';
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
    const { loading, error } = useAppSelector(state => state.bragLogs);
    const { data: behaviorTypes } = useAppSelector(state => state.behaviorTypes);
    const { data: students } = useAppSelector(state => state.students);
    const { data: teachers } = useAppSelector(state => state.teachers);
    const currentUser = useAppSelector(state => state.user.data);

    const [filteredStudents, setFilteredStudents] = useState<Student[]>(students);
    const [publicStudent, setPublicStudent] = useState<Student | null>(null);

    const isAdmin = useMemo(() => currentUser?.role === Role.ADMIN, [currentUser]);

    const initialData: BragLogFormData = {
        studentId: '',
        teacherId: '',
        behaviorIds: [],
        notes: '',
        pointsGenerated: 0
    };

    const form = useForm({
        initialData,
        validationRules: bragLogValidationRules
    });

    useEffect(() => {
        if (isPublic && studentToken && students.length > 0) {
            const student = students.find(s => s.token === studentToken);
            if (student) {
                setPublicStudent(student);
                form.setFormData(prev => ({
                    ...prev,
                    studentId: student.id.toString(),
                    teacherId: student.teacher.id.toString()
                }));
            }
        }
    }, [isPublic, studentToken, students, form]);

    useEffect(() => {
        if (show && isEdit && bragLog) {
            form.setFormData({
                studentId: bragLog.student.id.toString(),
                teacherId: bragLog.teacher.id.toString(),
                behaviorIds: bragLog.behaviors.map(b => b.id.toString()),
                notes: bragLog.notes || '',
                pointsGenerated: bragLog.pointsGenerated
            });
        }
    }, [show, isEdit, bragLog, form]);

    useEffect(() => {
        if (form.formData.teacherId) {
            const filtered = students.filter(student =>
                student.teacher.id.toString() === form.formData.teacherId
            );
            setFilteredStudents(filtered);
            if (form.formData.studentId) {
                const currentStudent = students.find(s =>
                    s.id.toString() === form.formData.studentId);
                if (currentStudent && currentStudent.teacher.id.toLocaleString() !== form.formData.teacherId) {
                    form.setFormData(prev => ({
                        ...prev,
                        studentId: ''
                    }));
                }
            }
        } else {
            setFilteredStudents(students);
        }
    }, [form, students]);

    useEffect(() => {
        if (form.formData.studentId && !isEdit) {
            const selectedStudent = students.find(s =>
                s.id.toString() === form.formData.studentId);
            if (selectedStudent) {
                form.setFormData(prev => ({
                    ...prev,
                    teacherId: selectedStudent.teacher.id.toString()
                }));
            }
        }
    }, [form, students, isEdit]);

    useEffect(() => {
        if (form.formData.behaviorIds && behaviorTypes.length > 0) {
            const selectedBehaviors = behaviorTypes.filter(bt =>
                form.formData.behaviorIds.includes(bt.id.toString())
            );
            const totalPoints = selectedBehaviors.reduce((sum, bt) =>
                sum + bt.pointValue, 0);
            form.setFormData(prev => ({
                ...prev,
                pointsGenerated: totalPoints
            }));
        }
    }, [form, behaviorTypes]);

    const toggleBehavior = (behaviorId: string) => {
        const currentBehaviorIds = form.formData.behaviorIds;
        const newBehaviorIds = currentBehaviorIds.includes(behaviorId)
            ? currentBehaviorIds.filter(id => id !== behaviorId)
            : [...currentBehaviorIds, behaviorId];
        form.setFormData(prev => ({
            ...prev,
            behaviorIds: newBehaviorIds
        }));
        form.setFormErrors(prev => {
            const newErrors = { ...prev };
            delete newErrors.behaviorIds;
            return newErrors;
        });
    };

    return {
        formData: form.formData, setFormData: form.setFormData, formErrors: form.formErrors,
        setFormErrors: form.setFormErrors, currentUser, error, loading, isAdmin, students: filteredStudents, teachers,
        behaviorTypes: behaviorTypes.filter(bt => bt.active), handleInputChange: form.handleInputChange,
        handleSelectChange: form.handleSelectChange, toggleBehavior, validateForm: form.validateForm,
        resetForm: form.resetForm, publicStudent
    };
}
