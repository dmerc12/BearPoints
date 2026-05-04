import { ValidationRule } from '../../hooks';

export const studentRewardValidationRules: ValidationRule[] = [
    {
        field: 'studentId',
        validator: (value) => {
            if (typeof value !== 'number' && typeof value !== 'string') return 'Student is required';
            if (String(value).trim() === '') return 'Student is required';
            return null;
        }
    },
    {
      field: 'itemId',
      validator: (value) => {
          if (typeof value !== 'number' && typeof value !== 'string') return 'Reward item is required';
          if (String(value).trim() === '') return 'Reward item is required';
          return null;
      }
    },
];
