import { useAppDispatch, addStudentReward } from '../../store';
import { BaseModal, StudentRewardForm } from '../index';
import { useStudentRewardForm } from '../../hooks';
import { Alert } from 'react-bootstrap';
import { useEffect } from 'react';

interface CreateStudentRewardModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateStudentRewardModal({ show, onCancel, onSuccess }: CreateStudentRewardModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, setFormErrors, error, loading, isAdmin, students, rewardItems,
        handleSelectChange, validateForm, resetForm } = useStudentRewardForm({ show });

    useEffect(() => {
        if (show && !isAdmin) {
            onCancel();
            setFormErrors({ general: 'Only administrators can create student rewards' });
            const timer = setTimeout(() => {
                setFormErrors({});
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, isAdmin, onCancel, setFormErrors]);

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
            />
        </BaseModal>
    );
}
