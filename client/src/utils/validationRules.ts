import { ValidationRule } from '../hooks/useForm';

export interface CommonPersonFormData {
    firstName: string;
    lastName: string;
    email: string;
}

export interface TeacherFormData extends CommonPersonFormData {
    grade: string | null;
}

export interface StudentFormData extends CommonPersonFormData {
    teacherId: string;
}

export const commonPersonValidationRules: ValidationRule[] = [
    {
        field: 'firstName',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'First name is required';
            return null;
        }
    },
    {
        field: 'lastName',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Last name is required';
            return null;
        }
    },
    {
        field: 'email',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Email name is required';
            if (!value.endsWith('@okcps.org')) return 'Email must be from @okcps.org';
            return null;
        }
    },
];

export const teacherValidationRules: ValidationRule[] = [
    ...commonPersonValidationRules,
    {
        field: 'grade',
        validator: (value) => {
            if (value === null || value === '') return 'Grade is required';
            return null;
        }
    },
];

export const studentValidationRules: ValidationRule[] = [
    ...commonPersonValidationRules,
    {
        field: 'teacherId',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Teacher is required';
            return null;
        }
    },
];
