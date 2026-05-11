import { parseISO, format } from 'date-fns';
import DatePicker from 'react-datepicker';
import { Form } from 'react-bootstrap';

interface DateFilterProps {
    value: string;
    onChange: (value: string) => void;
    label?: string;
    placeholder?: string;
    disabled?: boolean;
}

export function DateFilter({ value, onChange, label = 'Date', placeholder = 'Select a date', disabled = false }
                           : DateFilterProps) {
    const selectedDate = value ? parseISO(value) : null;
    const handleDateChange = (date: Date | null) => {
        onChange(date ? format(date, "yyyy-MM-dd'T'00:00:00") : '');
    };

    return (
        <Form.Group>
            <Form.Label>{label}</Form.Label>
            <DatePicker
                selected={selectedDate}
                onChange={handleDateChange}
                dateFormat="MMM dd, yyyy"
                placeholderText={placeholder}
                disabled={disabled}
                className="form-control"
                wrapperClassName="w-100"
            />
        </Form.Group>
    );
}
