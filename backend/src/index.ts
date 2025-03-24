import { createApp } from './helpers/createApp';
import dotenv from 'dotenv';

dotenv.config();

// Create express application
const app = createApp();

// Start app on specified port
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
