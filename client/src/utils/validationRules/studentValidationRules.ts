import { personValidationRules } from './index';
import { ValidationRule } from '../../hooks';

export const studentValidationRules: ValidationRule[] = [
    ...personValidationRules,
    {
        field: 'teacherId',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Teacher is required';
            return null;
        }
    },
];
