import { userValidationRules } from './userValidationRules';
import { ValidationRule } from '../../hooks';

export const studentValidationRules: ValidationRule[] = [
    ...userValidationRules,
    {
        field: 'teacherId',
        validator: (value) => {
            const numValue = typeof value === 'string' ? parseInt(value, 10) : value;
            if (numValue === -1 || numValue === null || isNaN(<number>numValue)) return 'Teacher is required';
            return null;
        }
    },
];
