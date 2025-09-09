import { ValidationRule } from '../hooks';

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

export interface BehaviorTypeFormData {
    name: string;
    pointValue: number;
    active: boolean;
}

export interface BragLogFormData {
    studentId: string;
    teacherId: string;
    behaviorIs: string[];
    notes?: string;
    pointsGenerated?: number;
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
            if (typeof value !== 'number') return 'Point value is required';
            if (value < 1) return 'Minimum point value is 1';
            if (value > 5) return 'Maximum point value is 5';
            return null;
        }
    },
];

export const bragLogValidationRules: ValidationRule[] = [
    {
        field: 'studentId',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Student is required';
            return null;
        }
    },
    {
        field: 'teacherId',
        validator: (value) => {
            if (typeof value !== 'string' || !value.trim()) return 'Teacher is required';
            return null;
        }
    },
    {
        field: 'behaviorIds',
        validator: (value) => {
            if (!Array.isArray(value) || value.length === 0) return 'At least one behavior is required';
            return null;
        }
    },
    {
        field: 'notes',
        validator: (value) => {
            if (typeof value === 'string' && value.length > 500) return 'Notes cannot exceed 500 characters';
            return null;
        }
    },
]
