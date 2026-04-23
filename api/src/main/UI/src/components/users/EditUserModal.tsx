import { useAppDispatch, modifyUser } from '../../store';
import { BaseModal, UserForm } from '../index';
import { Role, UserDTO } from '../../services';
import { useUserForm } from '../../hooks';
import { Alert } from 'react-bootstrap';
import { useEffect } from 'react';

interface EditUserModalProps {
    show: boolean;
    user: UserDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditUserModal({ show, user, onCancel, onSuccess }: EditUserModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, setFormErrors, error, loading, handleInputChange, handleSelectChange,
        validateForm, resetForm, currentUser } = useUserForm({ show, isEdit: true, user});

    useEffect(() => {
        if (show && currentUser?.role !== Role.ADMIN) {
            onCancel();
            setFormErrors({ general: 'Only administrators can edit users'});
            const timer = setTimeout(() => {
                setFormErrors({});
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, currentUser, onCancel, setFormErrors]);

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
