import { Form } from 'react-bootstrap';

interface SelectFilterProps {
    value: string;
    onChange: (value: string) => void;
    label: string;
    options: Array<{ value: string; label: string }>;
    disabled?: boolean;
    showAllOption?: boolean;
    allOptionLabel?: string;
}

export function SelectFilter({ value, onChange, label, options, disabled = false, showAllOption = true,
                                 allOptionLabel = 'All' }: SelectFilterProps) {
    return (
        <Form.Group>
            <Form.Label>{label}</Form.Label>
            <Form.Select value={value}
                         onChange={(e) => onChange(e.target.value)}
                         disabled={disabled}
            >
                {showAllOption && <option value=''>{allOptionLabel}</option>}
                {options.map(option => (
                    <option key={option.value} value={option.value}>
                        {option.label}
                    </option>
                ))}
            </Form.Select>
        </Form.Group>
    );
}
