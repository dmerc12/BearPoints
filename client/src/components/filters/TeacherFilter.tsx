import { Form } from 'react-bootstrap';

interface TeacherFilterProps {
    value: string;
    onChange: (value: string) => void;
    teachers: string[];
    label?: string;
    disabled?: boolean;
    showAllOption?: boolean;
    allOptionLabel?: string;
}

export function TeacherFilter({ value, onChange, teachers, label = 'Teacher', disabled = false,
                               showAllOption = true, allOptionLabel = 'All Teachers' }: TeacherFilterProps)  {
    return (
        <Form.Group>
            <Form.Label>{label}</Form.Label>
            <Form.Select
                value={value}
                onChange={(e) => onChange(e.target.value)}
                disabled={disabled}
            >
                {showAllOption && <option value=''>{allOptionLabel}</option>}
                {teachers.map(teacher => (
                    <option key={teacher} value={teacher}>{teacher}</option>
                ))}
            </Form.Select>
        </Form.Group>
    );
}