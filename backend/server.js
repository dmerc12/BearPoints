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
app.get( "/api/students", async ( req, res ) => {
    const response = await sheets.spreadsheets.values.get( {
        spreadsheetId: SPREADSHEET_ID,
        range: "Students!A:C"
    } );
    res.json( response.data.values.map( ( row ) => ( {
        studentId: row[ 0 ],
        name: row[ 2 ]
    } )));
});

// Generate QR code URL for specific student

// Validate auth token

app.listen( port, () => console.log( `API running on port ${ port }` ) );
