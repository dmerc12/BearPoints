const { google } = require( 'googleapis' );
const express = require( 'express' );

require( 'dotenv' ).config();

const app = express();
const port = process.env.PORT || 3000;

// Load credentials from the JSON file
const auth = new google.auth.GoogleAuth( {
    keyFile: 'credentials.json',
    scopes: 'https://www.googleapis.com/auth/spreadsheets',
} );

const sheets = google.sheets( { version: 'v4', auth } );

const SPREADSHEET_ID = process.env.SPREADSHEET_ID;

// Submit form

// Get leaderboard

// Get all students

// Generate QR code URL for specific student

// Validate auth token

app.listen( port, () => console.log( `API running on port ${ port }` ) );
