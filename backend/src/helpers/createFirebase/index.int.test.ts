import * as admin from 'firebase-admin';
import { createFirebase } from '.';

describe('createFirebase (Integration)', () => {
    var testApp: admin.app.App;

    afterEach(async () => {
        if (testApp && testApp.name !== '[DEFAULT]') {
            await testApp.delete();
        }
    });
    
    it('should allow Firebase service operations', async () => {
        testApp = createFirebase();
        const auth = testApp.auth();
        const users = await auth.listUsers(1);
        expect(users).toHaveProperty('users');
    });
});
