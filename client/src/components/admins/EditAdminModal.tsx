import { modifyAdmin } from '../../store/slices/adminsSlice';
import { useAdminForm } from '../../hooks/useAdminForm';
import { Role, UserDTO } from '../../services/types';
import { useAppDispatch } from '../../store/hooks';
import { AdminForm } from './AdminForm';
import { Alert } from 'react-bootstrap';
import BaseModal from '../BaseModal';
import { useEffect } from 'react';

interface EditAdminModalProps {
    show: boolean;
    admin: UserDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function EditAdminModal({ show, admin, onCancel, onSuccess }: EditAdminModalProps) {
    const dispatch = useAppDispatch();

    const { formData, formErrors, setFormErrors, error, loading,
        handleInputChange, validateForm, resetForm } = useAdminForm({ show, isEdit: true, admin});

    useEffect(() => {
        if (show && admin && admin.role !== Role.ADMIN) {
            onCancel();
            setFormErrors({ general: 'Only other administrators can edit administrators'});
            const timer = setTimeout(() => {
                setFormErrors({});
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [show, admin, onCancel, setFormErrors]);

    const handleSubmit = () => {
        if (!validateForm() || !admin || !admin.id) return;
        const adminData = {
            id: admin.id,
            firstName: formData.firstName,
            lastName: formData.lastName,
            email: formData.email,
            role: Role.ADMIN
        };
        dispatch(modifyAdmin({ id: admin.id, userData: adminData }))
            .unwrap()
            .then(() => {
                onSuccess();
                resetForm();
            })
            .catch((error: Error) => {
                console.log('Failed to update admin:', error);
            });
    };

    const handleClose = () => {
        resetForm();
        onCancel();
    };

    return (
        <BaseModal
            title='Edit Admin'
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
            <AdminForm
                formData={formData}
                formErrors={formErrors}
                loading={loading}
                onInputChange={handleInputChange}
            />
        </BaseModal>
    );
}
