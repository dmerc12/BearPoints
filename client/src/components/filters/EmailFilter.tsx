import { Form } from 'react-bootstrap';

interface EmailFilterProps {
    value: string;
    onChange: (value: string) => void;
    label?: string;
    placeholder?: string;
    disabled?: boolean;
    showHelpText?: boolean;
    helpText?: string;
}

export function EmailFilter({ value, onChange, label = 'Email', placeholder = 'Search by email', disabled = false,
                                showHelpText = true, helpText = 'Partial email matches accepted' }: EmailFilterProps) {
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
