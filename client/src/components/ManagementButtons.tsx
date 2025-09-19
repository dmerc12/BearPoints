import { Button, ButtonGroup } from 'react-bootstrap';

interface ManagementButtonsProps {
    onEdit: () => void;
    onDelete: () => void;
    size?: 'sm' | 'lg';
}

export function ManagementButtons({ onEdit, onDelete, size = 'sm' }: ManagementButtonsProps) {
    return (
        <ButtonGroup size={size}>
            <Button variant='outline-primary'
                    onClick={onEdit}
            >
                Edit
            </Button>
            <Button variant='danger'
                    onClick={onDelete}
            >
                Delete
            </Button>
        </ButtonGroup>
    );
}
