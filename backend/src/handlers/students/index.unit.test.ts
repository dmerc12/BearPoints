import { Request, Response } from 'express-serve-static-core';
import { GoogleSpreadsheet } from 'google-spreadsheet';
import { SheetsHelper } from '../../helpers/sheets';
import { getStudents } from '.';

jest.mock('../../helpers/sheets');

const mockRequest = () => ({}) as Request;
const mockResponse = () => {
    const response = {} as Response;
    response.status = jest.fn().mockReturnValue(response);
    response.json = jest.fn().mockReturnValue(response);
    return response;
};

describe('getStudents (Unit)', () => {
    let mockDoc: GoogleSpreadsheet;
    let mockSheet: any;

    beforeEach(() => {
        mockSheet = {
            title: 'Students',
            getRows: jest.fn(),
        };
        mockDoc = {
            sheetsByTitle: { 'Students': mockSheet },
            loadInfo: jest.fn()
        } as unknown as GoogleSpreadsheet;
        (SheetsHelper as jest.Mock).mockReturnValue(mockDoc);
    });

    it('should return list of students', async () => {
        const mockRows = [
            {
                get: (field: string) => {
                    switch (field) {
                        case 'studentID': return '123';
                        case 'name': return 'John Doe';
                        case 'grade': return '5th';
                        case 'teacher': return 'Mrs. Smith';
                    }
                }
            },
            {
                get: (field: string) => {
                    switch (field) {
                        case 'studentID': return '456';
                        case 'name': return 'Jane Smith';
                        case 'grade': return '4th';
                        case 'teacher': return 'Mr. Johnson';
                    }
                }
            },
        ];
        mockSheet.getRows.mockResolvedValue(mockRows);
        const response = mockResponse();
        await getStudents(mockRequest(), response, jest.fn());
        expect(response.json).toHaveBeenCalledWith([
            {
                studentID: 123,
                name: 'John Doe',
                grade: '5th',
                teacher: 'Mrs. Smith'
            },
            {
                studentID: 456,
                name: 'Jane Smith',
                grade: '4th',
                teacher: 'Mr. Johnson'
            }
        ]);
    });

    it('should handle sheet not found', async () => {
        delete mockDoc.sheetsByTitle.Students;
        const response = mockResponse();
        await getStudents(mockRequest(), response, jest.fn());
        expect(response.status).toHaveBeenCalledWith(500);
        expect(response.json).toHaveBeenCalledWith({ error: 'Students sheet not found' });
    });
});
