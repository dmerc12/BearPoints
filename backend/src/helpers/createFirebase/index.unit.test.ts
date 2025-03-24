import { createFirebase } from '.';

jest.mock('firebase-admin', () => ({
    apps: [],
    initializeApp: jest.fn(() => ({
        name: '[DEFAULT]',
        options: {},
        delete: jest.fn()
    })),
    credential: {
        cert: jest.fn()
    }
}));

describe('createFirebase (Unit)', () => {
    it('should return a firebase instance', () => {
        const firebase = createFirebase();
        expect(firebase).toEqual(expect.objectContaining({
            name: expect.any(String),
            options: expect.any(Object),
            delete: expect.any(Function)
        }));
    });
});
