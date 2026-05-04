import { ValidationRule } from '../../hooks';

export const bragLogValidationRules: ValidationRule[] = [
    {
        field: 'studentId',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Student is required';
            return null;
        }
    },
    {
        field: 'teacherId',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Teacher is required';
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
