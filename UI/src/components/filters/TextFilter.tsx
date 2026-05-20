import { useDebouncedInput } from '../../hooks';
import { Form } from 'react-bootstrap';

interface TextFilterProps {
    value: string;
    onChange: (value: string) => void;
    label?: string;
    placeholder?: string;
    disabled?: boolean;
    showHelpText?: boolean;
    helpText?: string;
    debounceDelay?: number;
    instanceId?: string;
}

export function TextFilter({ value, onChange, label = 'Search', placeholder = 'Search...', disabled = false,
                               showHelpText = true, helpText = 'Partial matches accepted', debounceDelay = 500, instanceId }
                           : TextFilterProps) {

    const { localValue, setLocalValue } = useDebouncedInput({ value, onChange, debounceDelay, instanceId });

    return (
        <Form.Group>
            <Form.Label>{label}</Form.Label>
            <Form.Control
                type='text'
                placeholder={placeholder}
                value={localValue}
                onChange={(e) => setLocalValue(e.target.value)}
                disabled={disabled}
            />
            {showHelpText && <Form.Text className='text-muted'>{helpText}</Form.Text>}
        </Form.Group>
    );

}
