import { useAppDispatch, addBragLog, fetchBragLogs } from '../../store';
import { useInternalBragLogForm } from '../../hooks';
import { BaseModal, BragLogForm } from '../index';
import { BragLogDTO } from '../../services';
import { Alert } from 'react-bootstrap';

interface CreateBragLogModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateBragLogModal({ show, onCancel, onSuccess }: CreateBragLogModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, error, loading, students, teachers, behaviorTypes, totalPoints,
        handleInputChange, handleSelectChange, toggleBehavior, validateForm, resetForm, handleClose }
        = useInternalBragLogForm({ show, onCancel });

    const handleSubmit = () => {
        if (!validateForm() || !formData.studentId) return;
        const bragLogData: BragLogDTO = {
            studentId: formData.studentId,
            behaviorIds: formData.behaviorIds,
            notes: formData.notes,
            submitterName: formData.submitterName,
        };
        dispatch(addBragLog(bragLogData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
                dispatch(fetchBragLogs({ page: 0, size: 10, force: true }));
            })
            .catch((error: Error) => {
                console.log('Failed to create brag log:', error);
            });
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
