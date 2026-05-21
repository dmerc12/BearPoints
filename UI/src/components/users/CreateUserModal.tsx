import { useAppDispatch, addUser, fetchUsers } from '../../store';
import { BaseModal, UserForm } from '../index';
import { UserDTO, Role } from '../../services';
import { useUserForm } from '../../hooks';
import { Alert } from 'react-bootstrap';

interface CreateAdminModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateUserModal({ show, onCancel, onSuccess }: CreateAdminModalProps) {
    const dispatch = useAppDispatch();
    
    const { formData, formErrors, error, loading, handleInputChange, handleSelectChange,
        validateForm, resetForm } = useUserForm({ show });
    
    const handleSubmit = () => {
        if (!validateForm()) return;
        const userData: UserDTO = {
            firstName: formData.firstName,
            lastName: formData.lastName,
            email: formData.email,
            role: formData.role as Role,
        };
        dispatch(addUser(userData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
                dispatch(fetchUsers({ page: 0, size: 10, force: true }));
            })
            .catch((error: Error) => {
                console.log('Failed to create user:', error);
            });
    };
    
    const handleClose = () => {
        resetForm();
        onCancel();
    };
    
    return (
        <BaseModal
            title='Create User'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Create'
            cancelText='Cancel'
            isLoading={loading}
            disableConfirm={loading}
        >
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
