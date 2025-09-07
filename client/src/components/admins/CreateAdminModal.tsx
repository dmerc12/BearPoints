import { addAdmin } from '../../store/slices/adminsSlice';
import { useAdminForm } from '../../hooks/useAdminForm';
import { useAppDispatch } from '../../store';
import { AdminForm } from './AdminForm';
import { Alert } from 'react-bootstrap';
import { Role } from '../../services';
import { BaseModal } from '../index';

interface CreateAdminModalProps {
    show: boolean;
    onCancel: () => void;
    onSuccess: () => void;
}

export function CreateAdminModal({ show, onCancel, onSuccess }: CreateAdminModalProps) {
    const dispatch = useAppDispatch();
    
    const { formData, formErrors, error, loading, handleInputChange, validateForm, resetForm } = useAdminForm({ show });
    
    const handleSubmit = () => {
        if (!validateForm()) return;
        const adminData = {
            firstName: formData.firstName,
            lastName: formData.lastName,
            email: formData.email,
            role: Role.ADMIN
        };
        dispatch(addAdmin(adminData))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            })
            .catch((error: Error) => {
                console.log('Failed to create admiin:', error);
            });
    };
    
    const handleClose = () => {
        resetForm();
        onCancel();
    };
    
    return (
        <BaseModal
            title='Create Admin'
            show={show}
            onConfirm={handleSubmit}
            onCancel={handleClose}
            confirmText='Create'
            cancelText='Cancel'
            isLoading={loading}
            disableConfirm={loading}
        >
            {error && <Alert variant='danger'>{error}</Alert>}
            <AdminForm 
                formData={formData}
                formErrors={formErrors}
                loading={loading}
                onInputChange={handleInputChange}
            />
        </BaseModal>
    );
}
