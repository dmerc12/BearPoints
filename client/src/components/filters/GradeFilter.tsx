import { formatGrade } from '../../utils/formatGrades';
import { sortGrades } from '../../utils/sortGrades';
import { GradeLevel } from '../../services/types';
import { Form } from 'react-bootstrap';

interface GradeFilterProps {
    value: string;
    onChange: (value: string) => void;
    items: (GradeLevel | string)[];
    label?: string;
    disabled?: boolean;
    showAllOption?: boolean;
    allOptionLabel?: string;
}

export function GradeFilter({ value, onChange, items, label = 'Grade', disabled = false, showAllOption = true,
                                allOptionLabel = 'All Grades' }: GradeFilterProps) {
    const sortedGrades = sortGrades(items);
    return (
        <Form.Group>
            <Form.Label>{label}</Form.Label>
            <Form.Select
                value={value}
                onChange={(e) => onChange(e.target.value)}
                disabled={disabled}
            >
                {showAllOption && <option value=''>{allOptionLabel}</option>}
                {sortedGrades.map(grade => (
                    <option key={grade} value={grade}>
                        {formatGrade(grade)}
                    </option>
                ))}
            </Form.Select>
        </Form.Group>
    );
}
