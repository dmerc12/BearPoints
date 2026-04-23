import { Form } from 'react-bootstrap';

interface NumberFilterProps {
    value: string;
    onChange: (value: string) => void;
    label?: string;
    placeholder?: string;
    disabled?: boolean;
    min?: number;
    max?: number;
    step?: number;
}

export function NumberFilter({ value, onChange, label = 'Value', placeholder = '0', disabled = false,
                                 min, max, step = 1}: NumberFilterProps) {
    return (
        <Form.Group>
            <Form.Label>{label}</Form.Label>
            <Form.Control
                type="number"
                placeholder={placeholder}
                value={value}
                onChange={(e) => onChange(e.target.value)}
                disabled={disabled}
                min={min}
                max={max}
                step={step}
            />
        </Form.Group>
    );
}
