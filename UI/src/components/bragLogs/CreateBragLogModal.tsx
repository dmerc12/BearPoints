import { useAppDispatch, addBragLog } from '../../store';
import { BaseModal, BragLogForm } from '../index';
import { useBragLogForm } from '../../hooks';
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
        handleInputChange, handleSelectChange, toggleBehavior, validateForm, resetForm } = useBragLogForm({ show });

    const handleSubmit = () => {
        if (!validateForm() || !formData.studentId || !formData.teacherId) return;
        const bragLogData: BragLogDTO = {
            studentId: formData.studentId,
            teacherId: formData.teacherId,
            behaviorIds: formData.behaviorIds,
            notes: formData.notes,
            submitterName: formData.submitterName,
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
