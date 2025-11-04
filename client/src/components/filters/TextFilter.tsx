import { Form } from 'react-bootstrap';

interface TextFilterProps {
    value: string;
    onChange: (value: string) => void;
    label?: string;
    placeholder?: string;
    disabled?: boolean;
    showHelpText?: boolean;
    helpText?: string;
}

export function TextFilter({ value, onChange, label = 'Search', placeholder = 'Search...', disabled = false,
                               showHelpText = true, helpText = 'Partial matches accepted' }: TextFilterProps) {
    return (
        <Form.Group>
            <Form.Label>{label}</Form.Label>
            <Form.Control
                type='text'
                placeholder={placeholder}
                value={value}
                onChange={(e) => onChange(e.target.value)}
                disabled={disabled}
            />
            {showHelpText && <Form.Text className='text-muted'>{helpText}</Form.Text>}
        </Form.Group>
    );

}
