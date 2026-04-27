import { useAppDispatch, addStudentReward } from '../../store';
import { BaseModal, StudentRewardForm } from '../index';
import { useStudentRewardForm } from '../../hooks';
import { Alert } from 'react-bootstrap';

interface CreateStudentRewardModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateStudentRewardModal({ show, onCancel, onSuccess }: CreateStudentRewardModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, error, loading, students, rewardItems, isStudent,
        handleSelectChange, validateForm, resetForm } = useStudentRewardForm({ show });

    const handleSubmit = () => {
        if (!validateForm()) return;
        const studentRewardData = {
            studentId: formData.studentId,
            itemId: formData.itemId,
        };
        dispatch(addStudentReward(studentRewardData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            })
            .catch((error: Error) => {
                console.log('Failed to create student reward:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title="Redeem Rewards"
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText="Create"
            cancelText="Cancel"
            isLoading={loading}
            disableConfirm={loading}
        >
            {error && <Alert variant="danger">{error}</Alert>}
            <StudentRewardForm
                formData={formData}
                formErrors={formErrors}
                loading={loading}
                students={students}
                rewardItems={rewardItems}
                onSelectChange={handleSelectChange}
                isStudent={isStudent}
            />
        </BaseModal>
    );
}
