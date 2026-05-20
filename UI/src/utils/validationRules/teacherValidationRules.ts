import { userValidationRules } from './userValidationRules';
import { ValidationRule } from '../../hooks';

export const teacherValidationRules: ValidationRule[] = [
    ...userValidationRules,
    {
        field: 'grade',
        validator: (value) => {
            if (value === null || value === '') return 'Grade is required';
            return null;
        }
    },
];
