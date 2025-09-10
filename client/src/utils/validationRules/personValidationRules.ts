import { ValidationRule } from '../../hooks';

export const personValidationRules: ValidationRule[] = [
    {
        field: 'firstName',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'First name is required';
            return null;
        }
    },
    {
        field: 'lastName',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Last name is required';
            return null;
        }
    },
    {
        field: 'email',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Email name is required';
            if (!value.endsWith('@okcps.org')) return 'Email must be from @okcps.org';
            return null;
        }
    },
];
