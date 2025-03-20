import { createApp } from '.';

describe('createApp (Unit)', () => {
    it('should return an express instance', () => {
        const app = createApp();
        expect(app).toBeInstanceOf(Function);
        expect(app).toHaveProperty('use');
        expect(app).toHaveProperty('listen');
    });
});
