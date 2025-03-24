import { ServiceAccount } from 'firebase-admin';
import * as admin from 'firebase-admin';
import path from 'path';
import 'dotenv/config';

export function createFirebase () {
    // Create and setup firebase application
    const serviceAccount: ServiceAccount = require(
        path.resolve(__dirname, '../../../firebase-service-account.json')
    )
    const firebase = admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
    });
    return firebase;
}
