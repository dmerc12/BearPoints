import { useAppDispatch, modifyUser, fetchUsers } from '../../store';
import { BaseModal, UserForm } from '../index';
import { useUserForm } from '../../hooks';
import { UserDTO } from '../../services';
import { Alert } from 'react-bootstrap';

interface EditUserModalProps {
    show: boolean;
    user: UserDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditUserModal({ show, user, onCancel, onSuccess }: EditUserModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, error, loading, handleInputChange, handleSelectChange,
        validateForm, resetForm } = useUserForm({ show, isEdit: true, user});

    const handleSubmit = () => {
        if (!validateForm() || !user || !user.id) return;
        const userData: UserDTO = {
            id: user.id,
            firstName: formData.firstName,
            lastName: formData.lastName,
            email: formData.email,
            role: formData.role,
        };
        dispatch(modifyUser({ id: user.id, userData: userData }))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
                dispatch(fetchUsers({ page: 0, size: 10, force: true }));
            })
            .catch((error: Error) => {
                console.log('Failed to update user:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title='Edit User'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Save'
            cancelText='Cancel'
            isLoading={loading}
            disableConfirm={loading}
        >
            {formErrors.general && <Alert variant='danger'>{formErrors.general}</Alert>}
            {error && <Alert variant='danger'>{error}</Alert>}
            <UserForm
                formData={formData}
                formErrors={formErrors}
                loading={loading}
                onInputChange={handleInputChange}
                onSelectChange={handleSelectChange}
            />
        </BaseModal>
    );
}
