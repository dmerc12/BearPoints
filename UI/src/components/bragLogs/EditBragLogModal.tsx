import { useAppDispatch, modifyBragLog } from '../../store';
import { useInternalBragLogForm } from '../../hooks';
import { BaseModal, BragLogForm } from '../index';
import { BragLogDTO } from '../../services';
import { Alert } from 'react-bootstrap';

interface EditBragLogModalProps {
    show: boolean;
    bragLog: BragLogDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditBragLogModal({ show, bragLog, onCancel, onSuccess }: EditBragLogModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, error, loading, students, teachers, behaviorTypes, totalPoints,
        handleInputChange, handleSelectChange, toggleBehavior, validateForm, resetForm, handleClose }
        = useInternalBragLogForm({ show, onCancel });

    const handleSubmit = () => {
        if (!validateForm() || !bragLog || !bragLog.id) return;
        const bragLogData: BragLogDTO = {
            studentId: formData.studentId,
            teacherId: formData.teacherId,
            behaviorIds: formData.behaviorIds,
            notes: formData.notes,
            submitterName: formData.submitterName
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
                students={students}
                teachers={teachers}
                totalPoints={totalPoints}
                behaviorTypes={behaviorTypes}
                onInputChange={handleInputChange}
                onSelectChange={handleSelectChange}
                onToggleBehavior={toggleBehavior}
            />
        </BaseModal>
    );
}
