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
            if (typeof value !== 'number') return 'Point cost is required';
            if (value < 0) return 'Point cost cannot be negative';
            return null;
        }
    },
    {
        field: 'stock',
        validator: (value) => {
            if (typeof value !== 'number') return 'Stock quantity is required';
            if (value < 0) return 'Stock quantity cannot be negative';
            return null;
        }
    },
];
