import { useAppDispatch, modifyStudentReward, fetchStudentRewards } from '../../store';
import { BaseModal, StudentRewardForm } from '../index';
import { useStudentRewardForm } from '../../hooks';
import { StudentRewardDTO } from '../../services';
import { Alert } from 'react-bootstrap';

interface EditStudentRewardModalProps {
    show: boolean;
    studentReward: StudentRewardDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditStudentRewardModal({ show, studentReward, onCancel, onSuccess }: EditStudentRewardModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, error, loading, isStudent, students, rewardItems,
        handleSelectChange, validateForm, resetForm } = useStudentRewardForm({ show, isEdit: true, studentReward });

    const handleSubmit = () => {
        if (!validateForm() || !studentReward || !studentReward.id) return;
        const studentRewardData = {
            studentId: formData.studentId,
            itemId: formData.itemId,
        };
        dispatch(modifyStudentReward({ id: studentReward.id, studentRewardData }))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
                dispatch(fetchStudentRewards({ page: 0, size: 10, force: true }));
            })
            .catch((error: Error) => {
                console.log('Failed to update student reward:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title="Edit Reward Redemption"
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText="Save"
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
