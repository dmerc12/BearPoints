import { userValidationRules } from './userValidationRules';
import { ValidationRule } from '../../hooks';

export const studentValidationRules: ValidationRule[] = [
    ...userValidationRules,
    {
        field: 'teacherId',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Teacher is required';
            return null;
        }
    },
];
