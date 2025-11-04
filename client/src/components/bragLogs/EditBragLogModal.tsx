import { BragLog, Student, Teacher, BehaviorType } from '../../services';
import { useAppDispatch, modifyBragLog } from '../../store';
import { BaseModal, BragLogForm } from '../index';
import { useBragLogForm } from '../../hooks';
import { Alert } from 'react-bootstrap';

interface EditBragLogModalProps {
    show: boolean;
    bragLog: BragLog | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditBragLogModal({ show, bragLog, onCancel, onSuccess }: EditBragLogModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, error, loading, isAdmin, students, teachers, behaviorTypes, handleInputChange,
        handleSelectChange, toggleBehavior, validateForm, resetForm } = useBragLogForm({ show, isEdit: true, bragLog });

    const handleSubmit = () => {
        if (!validateForm() || !bragLog || !bragLog.id) return;
        const bragLogData = {
            student: { id: parseInt(formData.studentId) } as Student,
            teacher: { id: parseInt(formData.teacherId) } as Teacher,
            behaviors: formData.behaviorIds.map(id => ({ id: parseInt(id) } as BehaviorType)),
            notes: formData.notes,
            pointsGenerated: formData.pointsGenerated
        };
        dispatch(modifyBragLog({ id: bragLog.id, bragLogData }))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            })
            .catch((error: Error) => {
                console.log('Failed to update brag log:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title='Edit Brag Log'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Save'
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
