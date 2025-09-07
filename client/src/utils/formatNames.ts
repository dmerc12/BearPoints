import { Teacher, Student } from '../services';
import { memoize } from 'lodash';

interface NameSource {
    firstName: string;
    lastName: string;
}

const getNameSource = (source: Teacher | Student | NameSource): NameSource => {
    if ('user' in source) {
        if (!source.user) {
            return { firstName: '', lastName: '' };
        }
        return {
            firstName: source.user.firstName,
            lastName: source.user.lastName,
        };
    }
    return source;
};

const resolver = (source: Teacher | Student | NameSource) => {
    if ('id' in source) return source.id;
    const names = getNameSource(source);
    return `${names.firstName || ''}-${names.lastName || ''}`;
};

export const formatName = memoize((source: Teacher | Student | NameSource) => {
    const { firstName, lastName } = getNameSource(source);
    if (!firstName && !lastName) return '';
    if (!firstName) return lastName || '';
    if (!lastName) return `${firstName[0]}.`;
    return `${firstName?.[0] || ''}. ${lastName}`;
}, resolver);

export const fullName = memoize((source: Teacher | Student | NameSource) => {
    const { firstName, lastName } = getNameSource(source);
    return `${firstName} ${lastName}`;
}, resolver);

export const clearNameCaches = () => {
    formatName.cache.clear?.();
    fullName.cache.clear?.();
};
