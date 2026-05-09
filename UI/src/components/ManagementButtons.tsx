import { Button, ButtonGroup } from 'react-bootstrap';

export interface ManagementButtonsProps {
    onEdit: () => void;
    onDelete: () => void;
    size?: 'sm' | 'lg';
    showEdit?: boolean;
    showDelete?: boolean;
}

export function ManagementButtons(props: ManagementButtonsProps) {
    const { onEdit, onDelete, size = 'sm', showEdit = true, showDelete = true } = props;

    if (!showEdit && !showDelete) return null;

    return (
        <ButtonGroup size={size}>
            <div className="d-flex gap-2 my-auto">
                {showEdit && (
                    <Button variant='outline-primary' onClick={onEdit}>
                        Edit
                    </Button>
                )}
                {showDelete && (
                    <Button variant='danger' onClick={onDelete}>
                        Delete
                    </Button>
                )}
            </div>
        </ButtonGroup>
    );
}
