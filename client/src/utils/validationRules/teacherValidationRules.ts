import { personValidationRules } from './index';
import { ValidationRule } from '../../hooks';

export const teacherValidationRules: ValidationRule[] = [
    ...personValidationRules,
    {
        field: 'grade',
        validator: (value) => {
            if (value === null || value === '') return 'Grade is required';
            return null;
        }
    },
];
