import { Form } from 'react-bootstrap';

interface StatusFilterProps {
    value: string;
    onChange: (value: string) => void;
    label?: string;
    disabled?: boolean;
    showAllOption?: boolean;
    allOptionLabel?: string;
}

export function StatusFilter({ value, onChange, label = 'Status', disabled = false,
                                 showAllOption = true, allOptionLabel = 'All Statuses'}: StatusFilterProps) {
    return (
        <Form.Group>
            <Form.Label>{label}</Form.Label>
            <Form.Select
                value={value}
                onChange={(e) => onChange(e.target.value)}
                disabled={disabled}
            >
                {showAllOption && <option value=''>{allOptionLabel}</option>}
                <option value='true'>Active</option>
                <option value='false'>Inactive</option>
            </Form.Select>
        </Form.Group>
    );
}
