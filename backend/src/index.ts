import { createApp } from './helpers/createApp';
import cors from 'cors';
import 'dotenv/config';

// Create express application
const app = createApp();

// Enable application to use cors
app.use(cors());

// Create and setup firebase application
require('dotenv').config();
var admin = require('firebase-admin');
var serviceAccount = require('../../../firebase-service-account.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

// Start app on specified port
const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
