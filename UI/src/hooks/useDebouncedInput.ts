import { useState, useEffect, useRef } from 'react';

const inputCache = new Map<string, string>();

interface UseDebouncedInputOptions {
    value: string;
    onChange: (value: string) => void;
    debounceDelay?: number;
    instanceId?: string;
}

export function useDebouncedInput({ value, onChange, debounceDelay = 500, instanceId }: UseDebouncedInputOptions) {
    const stableId = useRef(instanceId ||  `${Math.random()}`);

    const [localValue, setLocalValue] = useState(() => {
        const cached = inputCache.get(stableId.current);
        return cached !== undefined ? cached : value;
    });

    useEffect(() => {
        const timer = setTimeout(() => {
            onChange(localValue);
            inputCache.set(stableId.current, localValue);
        }, debounceDelay);
        return () => clearTimeout(timer);
    }, [localValue, onChange, debounceDelay]);

    return { localValue, setLocalValue };
}
