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
            let num: number;
            if (typeof value === 'string') {
                num = parseInt(value, 10);
                if (isNaN(num)) return 'Point value is required';
            } else if (typeof value === 'number') {
                num = value;
            } else {
                return 'Point value is required';
            }
            if (num < 1) return 'Minimum point value is 1';
            if (num > 5) return 'Maximum point value is 5';
            return null;
        }
    },
];
