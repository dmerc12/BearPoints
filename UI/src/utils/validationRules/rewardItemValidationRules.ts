import { ValidationRule } from '../../hooks';

export const rewardItemValidationRules: ValidationRule[] = [
    {
        field: 'name',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Name is required';
            if (value.length > 50) return 'Name must be less than 50 characters';
            return null;
        }
    },
    {
        field: 'pointCost',
        validator: (value) => {
            let num: number;
            if (typeof value === 'string') {
                num = parseInt(value, 10);
                if (isNaN(num)) return 'Point cost is required';
            } else if (typeof value === 'number') {
                num = value;
            } else {
                return 'Point cost is required';
            }
            if (num < 0) return 'Point cost cannot be negative';
            return null;
        }
    },
    {
        field: 'stock',
        validator: (value) => {
            let num: number;
            if (typeof value === 'string') {
                num = parseInt(value, 10);
                if (isNaN(num)) return 'Stock quantity is required';
            } else if (typeof value === 'number') {
                num = value;
            } else {
                return 'Stock quantity is required';
            }
            if (num < 0) return 'Stock quantity cannot be negative';
            return null;
        }
    },
];
