import { Student, BragLog, Teacher, StudentToken, BragRow } from 'types';
import { GoogleSpreadsheet } from 'google-spreadsheet';
import { Response } from 'express-serve-static-core';
import { JWT } from 'google-auth-library';

export const SheetsHelper = () => {
    const serviceAccount = process.env.SHEETS_SERVICE_ACCOUNT;
    const spreadsheetId = process.env.SPREADSHEET_ID;
    if (!serviceAccount) {
        throw new Error('SHEETS_SERVICE_ACCOUNT environment variable is not set');
    }
    if (!spreadsheetId) {
        throw new Error('SPREADSHEET_ID environment variable is not set');
    }
    const credentials = JSON.parse(serviceAccount)
    const auth = new JWT({
        email: credentials.client_email,
        key: credentials.private_key.replace(/\\n/g, '\n'),
        scopes: [ 'https://www.googleapis.com/auth/spreadsheets' ]
    });
    return new GoogleSpreadsheet(spreadsheetId, auth);
};

export const fetchSheets = async (response: Response) => {
    const doc = SheetsHelper();
    await doc.loadInfo();
    const [studentsSheet, teachersSheet, bragsSheet] = await Promise.all([
        doc.sheetsByTitle[ 'Students' ],
        doc.sheetsByTitle[ 'Teachers' ],
        doc.sheetsByTitle[ 'BearBragLog' ]
    ]);
    if (!studentsSheet) {
        console.error('Students sheet not found');
        response.status(404).json({ error: 'Students sheet not found' });
    }
    if (!teachersSheet) {
        console.error('Teachers sheet not found');
        response.status(404).json({ error: 'Teachers sheet not found' });
    }
    if (!bragsSheet) {
        console.error('Brags sheet not found');
        response.status(404).json({ error: 'Brags sheet not found' });
    }
    const [studentRows, teacherRows, bragRows] = await Promise.all([
        studentsSheet.getRows(),
        teachersSheet.getRows(),
        bragsSheet.getRows()
    ]);
    return { studentRows, teacherRows, bragRows };
};

export const mapSheets = async (response: Response) => {
    // Get all behavior logs and students
    const { studentRows, teacherRows, bragRows } = await fetchSheets(response);
    const teacherMap = new Map<number, Teacher>();
    teacherRows.forEach(row => {
        const teacherID = Number(row.get('teacherID'));
        teacherMap.set(teacherID, {
            teacherID,
            name: String(row.get('name')),
            email: String(row.get('email')),
            grade: String(row.get('grade'))
        });
    });
    const bragLogs: BragLog[] = bragRows.map(row => ({
        timestamp: row.get('timestamp'),
        studentID: Number(row.get('studentID')),
        teacherID: Number(row.get('teacherID')),
        grade: String(row.get('grade')),
        brilliant: row.get('brilliant') === 'TRUE',
        excelled: row.get('excelled') === 'TRUE',
        answered: row.get('answered') === 'TRUE',
        read: row.get('read') === 'TRUE',
        sensationalWriting: row.get('sensationalWriting') === 'TRUE',
        points: Number(row.get('points')),
        notes: row.get('notes') || undefined
    }));
    const pointsMap = new Map<number, number>();
    bragRows.forEach(row => {
        const studentID = Number(row.get('studentID'));
        const points = Number(row.get('points')) || 0;
        pointsMap.set(studentID, (pointsMap.get(studentID) || 0) + points);
    });
    const students: Student[] = studentRows.map(row => {
        const teacherID = Number(row.get('teacherID'));
        const teacher = teacherMap.get(teacherID);
        if (!teacher) {
            console.warn(`Teacher not found for ID: ${teacherID}`);
        }
        return {
            studentID: Number(row.get('studentID')),
            name: String(row.get('name')),
            teacher: teacher?.name || 'Unknown',
            teacherID: Number(row.get('teacherID')),
            grade: String(teacher?.grade || 'Unknown'),
            token: String(row.get('token')),
            points: pointsMap.get(Number(row.get('studentID'))) || 0
        }
    });
    const teachers: Teacher[] = teacherRows.map(row => ({
        teacherID: Number(row.get('teacherID')),
        name: String(row.get('name')),
        email: String(row.get('email')),
        grade: String(row.get('grade')),
    }));
    return { bragLogs, students, teachers };
};

export const fetchStudentByID = async (token: string, response: Response) => {
    const { studentRows, teacherRows } = await fetchSheets(response);
    const studentRow = studentRows
        .find(row => row.get('token') === token)!;
    if (!studentRow) response.status(400).json({ error: 'Student not found' });
    const teacherID = Number(studentRow.get('teacherID'));
    const teacherRow = teacherRows
        .find(row => Number(row.get('teacherID')) === teacherID)
    if (!teacherRow) response.status(400).json({ error: 'Teacher not found' });
    const student: StudentToken = {
        studentID: Number(studentRow.get('studentID')),
        name: String(studentRow.get('name')),
        teacherID: Number(studentRow.get('teacherID')),
        grade: String(teacherRow?.get('grade') || 'Unknown'),
        token: String(studentRow.get('token'))
    }
    return student;
};

export const addBearBrag = async (studentID: number, teacherID: number, grade: string, behaviors: { brilliant: boolean; excelled: boolean; answered: boolean; read: boolean; sensationalWriting: boolean }, notes: string) => {
    const doc = SheetsHelper();
    await doc.loadInfo();
    const sheet = doc.sheetsByTitle[ 'BearBragLog' ];
    if (!sheet) {
        throw new Error('BearBragLog sheet not found');
    }
    const points = Object.values(behaviors).filter(Boolean).length;
    const timestamp = new Date().toISOString();
    const rowData: BragRow = {
        timestamp: timestamp,
        studentID: studentID,
        teacherID: teacherID,
        grade: grade,
        brilliant: behaviors.brilliant ? 'TRUE' : 'FALSE',
        excelled: behaviors.excelled ? 'TRUE' : 'FALSE',
        answered: behaviors.answered ? 'TRUE' : 'FALSE',
        read: behaviors.read ? 'TRUE' : 'FALSE',
        sensationalWriting: behaviors.sensationalWriting ? 'TRUE' : 'FALSE',
        points: points,
        notes: notes || ''
    }
    await sheet.addRow(rowData);
    return true;
};

export const checkHealth = async (response: Response) => {
    // 1. Verify Google Sheets connection
    const doc = SheetsHelper();
    await doc.loadInfo();
    // 2. Verify required sheets exist
    const requiredSheets = ['Students', 'Teachers', 'BearBragLog'];
    const missingSheets = requiredSheets.filter(title => !doc.sheetsByTitle[title])
    if (missingSheets.length > 0) {
        response.status(500).json({
            healthy: false,
            error: `Missing sheets: ${missingSheets.join(', ')}`
        });
    }
    // 3. Verify we can read data
    const [studentsSheet, teachersSheet, bragsSheet] = await Promise.all([
        doc.sheetsByTitle['Students'].getRows(),
        doc.sheetsByTitle['Teachers'].getRows(),
        doc.sheetsByTitle['BearBragLog'].getRows(),
    ]);
    if (studentsSheet.length === 0 || teachersSheet.length === 0) {
        response.status(500).json({
            healthy: false,
            error: 'Sheets contain no data'
        });
    }
    // Return healthy response
    response.status(200).json({
        healthy: true,
        details: {
            spreadsheetId: doc.spreadsheetId,
            sheetTitles: Object.keys(doc.sheetsByTitle),
            studentsCount: studentsSheet.length,
            teachersCount: teachersSheet.length,
            bragsCount: bragsSheet.length
        }
    });
};

