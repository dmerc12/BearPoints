import { createFirebase } from './helpers/createFirebase';
import { createApp } from './helpers/createApp';
import dotenv from 'dotenv';

dotenv.config();

// Create firebase application
const firebase = createFirebase();

// Create express application
const app = createApp();

// Start app on specified port
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
    console.log(`Firebase project: ${firebase.options.projectId}`);
});
