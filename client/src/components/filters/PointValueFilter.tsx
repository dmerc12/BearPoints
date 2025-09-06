import { Form } from 'react-bootstrap';

interface PointValueFilterProps {
    value: string;
    onChange: (value: string) => void;
    label?: string;
    disabled?: boolean;
    showAllOption?: boolean;
    allOptionLabel?: string;
}

export function PointValueFilter({ value, onChange, label = 'Point Value', disabled = false,
                                     showAllOption = true, allOptionLabel = 'All Point Values'}: PointValueFilterProps) {
    return (
        <Form.Group>
            <Form.Label>{label}</Form.Label>
            <Form.Select
                value={value}
                onChange={(e) => onChange(e.target.value)}
                disabled={disabled}
            >
                {showAllOption && <option value=''>{allOptionLabel}</option>}
                <option value='1'>1 Point</option>
                <option value='2'>2 Points</option>
                <option value='3'>3 Points</option>
                <option value='4'>4 Points</option>
                <option value='5'>5 Points</option>
            </Form.Select>
        </Form.Group>
    );
}
