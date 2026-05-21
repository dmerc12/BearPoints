import { useAppDispatch, useAppSelector, removeBehaviorType, fetchBehaviorTypes } from '../../store';
import { BehaviorTypeDTO } from '../../services';
import { Alert } from 'react-bootstrap';
import { BaseModal } from '../index';

interface DeleteBehaviorTypeModal {
    show: boolean;
    behaviorType: BehaviorTypeDTO | null;
    onCancel: () => void;
    onSuccess: () => void;
}

export function DeleteBehaviorTypeModal({ show, behaviorType, onCancel, onSuccess }: DeleteBehaviorTypeModal) {
    const dispatch = useAppDispatch();
    const { loading, error } = useAppSelector(state => state.behaviorTypes);

    const handleConfirmDelete = () => {
        if (!behaviorType || !behaviorType.id) return;
        dispatch(removeBehaviorType(behaviorType.id))
            .unwrap()
            .then(() => {
                onSuccess();
                dispatch(fetchBehaviorTypes({ page: 0, size: 10, force: true }));
            })
            .catch((error: Error) => {
                console.log('Failed to delete behavior types', error);
            });
    };

    return (
        <BaseModal
            title='Delete Behavior Type'
            show={show}
            onConfirm={handleConfirmDelete}
            onCancel={onCancel}
            confirmText='Delete'
            cancelText='Cancel'
            confirmVariant='danger'
            isLoading={loading}
            disableConfirm={loading}
        >
            {error && <Alert variant='danger'>{error}</Alert>}
            <p>Are you sure you want to delete {behaviorType ? behaviorType.name : 'this behavior type'}?</p>
            <p className='text-muted'>This action cannot be undone.</p>
            {behaviorType && (
                <div className='mt-3 p-3 bg-light rounded'>
                    <h6>Behavior Type Details:</h6>
                    <p><strong>Name:</strong> {behaviorType.name}</p>
                    <p><strong>Point Value:</strong> {behaviorType.pointValue}</p>
                    <p><strong>Status:</strong> {behaviorType.active ? 'Active' : 'Inactive'}</p>
                </div>
            )}
        </BaseModal>
    );
}
