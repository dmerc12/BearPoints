import { Student, Teacher, BehaviorType } from '../../services';
import { useAppDispatch, addBragLog } from '../../store';
import { BaseModal, BragLogForm } from '../index';
import { useBragLogForm } from '../../hooks';
import { Alert } from 'react-bootstrap';

interface CreateBragLogModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateBragLogModal({ show, onCancel, onSuccess }: CreateBragLogModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, error, loading, isAdmin, students, teachers, behaviorTypes, handleInputChange,
        handleSelectChange, toggleBehavior, validateForm, resetForm } = useBragLogForm({ show });

    const handleSubmit = () => {
        if (!validateForm()) return;
        const bragLogData = {
            student: { id: parseInt(formData.studentId) } as Student,
            teacher: { id: parseInt(formData.teacherId) } as Teacher,
            behaviors: formData.behaviorIds.map(id => ({ id: parseInt(id) } as BehaviorType)),
            notes: formData.notes,
            pointsGenerated: formData.pointsGenerated
        };
        dispatch(addBragLog(bragLogData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            })
            .catch((error: Error) => {
                console.log('Failed to create brag log:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title='Create Brag Log'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Create'
            cancelText='Cancel'
            isLoading={loading}
            disableConfirm={loading}
        >
            {error && <Alert variant='danger'>{error}</Alert>}
            <BragLogForm
                formData={formData}
                formErrors={formErrors}
                loading={loading}
                isAdmin={isAdmin}
                students={students}
                teachers={teachers}
                behaviorTypes={behaviorTypes}
                onInputChange={handleInputChange}
                onSelectChange={handleSelectChange}
                onToggleBehavior={toggleBehavior}
            />
        </BaseModal>
    );
}
