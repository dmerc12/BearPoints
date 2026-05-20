import { ValidationRule } from '../../hooks';

export const bragLogValidationRules: ValidationRule[] = [
    {
        field: 'studentId',
        validator: (value) => {
            if (value === undefined || value === null) return 'Student is required';
            if (typeof value === 'number') {
                return value > 0 ? null : 'Student is required';
            }
            if (typeof value === 'string') {
                return value.trim() ? null : 'Student is required';
            }
            return 'Student is required';
        }
    },
    {
        field: 'submitterName',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Submitter name is required';
            const trimmed = value.trim();
            if (trimmed.length < 2) return 'Submitter name must be at least 2 characters';
            if (trimmed.length > 250) return 'Submitter name cannot exceed 250 characters';
            return null;
        }
    },
    {
        field: 'behaviorIds',
        validator: (value) => {
            if (!Array.isArray(value) || value.length === 0) return 'At least one behavior is required';
            return null;
        }
    },
    {
        field: 'notes',
        validator: (value) => {
            if (typeof value === 'string' && value.length > 500) return 'Notes cannot exceed 500 characters';
            return null;
        }
    },
]
