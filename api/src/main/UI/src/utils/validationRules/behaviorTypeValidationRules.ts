import { ValidationRule } from '../../hooks';

export const behaviorTypeValidationRules: ValidationRule[] = [
    {
        field: 'name',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Name is required';
            if (value.length > 50) return 'Name must be less than 50 characters';
            return null;
        }
    },
    {
        field: 'pointValue',
        validator: (value) => {
            if (typeof value !== 'number') return 'Point value is required';
            if (value < 1) return 'Minimum point value is 1';
            if (value > 5) return 'Maximum point value is 5';
            return null;
        }
    },
];
