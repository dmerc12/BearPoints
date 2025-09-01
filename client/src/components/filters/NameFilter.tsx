import { Form } from 'react-bootstrap';

interface NameFilterProps {
    value: string;
    onChange: (value: string) => void;
    label?: string;
    placeholder?: string;
    disabled?: boolean;
    showHelpText?: boolean;
    helpText?: string;
}

export function NameFilter({ value, onChange, label = 'Name', placeholder = 'Search by name', disabled = false,
                               showHelpText = true, helpText = 'Partial name matches accepted' }: NameFilterProps) {
    return (
        <Form.Group>
            <Form.Label>{label}</Form.Label>
            <Form.Control
                placeholder={placeholder}
                value={value}
                onChange={(e) => onChange(e.target.value)}
                disabled={disabled}
            />
            {showHelpText && <Form.Text className='text-muted'>{helpText}</Form.Text>}
        </Form.Group>
    );
}
