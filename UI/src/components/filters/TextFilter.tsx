import { useState, useEffect, useRef } from 'react';
import { Form } from 'react-bootstrap';

const inputCache = new Map<string, string>();

interface TextFilterProps {
    value: string;
    onChange: (value: string) => void;
    label?: string;
    placeholder?: string;
    disabled?: boolean;
    showHelpText?: boolean;
    helpText?: string;
    instanceId?: string;
}

export function TextFilter({ value, onChange, label = 'Search', placeholder = 'Search...', disabled = false,
                               showHelpText = true, helpText = 'Partial matches accepted', instanceId }
                           : TextFilterProps) {
    const stableId = useRef(instanceId || `${label}-${placeholder}`);

    const [localValue, setLocalValue] = useState(() => {
        const cached = inputCache.get(stableId.current);
        return cached !== undefined ? cached : value;
    });

    useEffect(() => {
        const timer = setTimeout(() => {
            onChange(localValue);
            inputCache.set(stableId.current, localValue);
        }, 1000);
        return () => clearTimeout(timer);
    }, [localValue, onChange]);

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
