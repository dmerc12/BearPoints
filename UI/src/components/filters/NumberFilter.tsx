import { useDebouncedInput } from '../../hooks';
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
    debounceDelay?: number;
    instanceId?: string;
}

export function NumberFilter({ value, onChange, label = 'Value', placeholder = '0', disabled = false,
                                 min, max, step = 1, debounceDelay = 500, instanceId }: NumberFilterProps) {
    const { localValue, setLocalValue } = useDebouncedInput({ value: value, onChange, debounceDelay, instanceId });

    return (
        <Form.Group>
            <Form.Label>{label}</Form.Label>
            <Form.Control
                type="number"
                placeholder={placeholder}
                value={localValue}
                onChange={(e) => setLocalValue(e.target.value)}
                disabled={disabled}
                min={min}
                max={max}
                step={step}
            />
        </Form.Group>
    );
}
