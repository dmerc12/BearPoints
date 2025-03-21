import { Request, Response } from 'express-serve-static-core';
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

const mockNext = jest.fn();

describe('getStudents (Unit)', () => {
    let request: Request;
    let response: Response;
    let next: jest.Mock;

    beforeEach(() => {
        jest.clearAllMocks();
        request = mockRequest();
        response = mockResponse();
        next = mockNext;
    });

    it('should return list of students', async () => {
        const mockData = [
            [ '123', 'John Doe', '5th', 'Mrs. Smith' ],
            [ '456', 'Jane Smith', '4th', 'Mr. Johnson' ]
        ];
        (SheetsHelper.read as jest.Mock).mockResolvedValue(mockData);
        await getStudents(request, response, next);
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

    it('should handle empty response', async () => {
        (SheetsHelper.read as jest.Mock).mockResolvedValue([]);
        await getStudents(request, response, next);
        expect(response.json).toHaveBeenCalledWith([]);
    });

    it('should handle API errors', async () => {
        const error = new Error('Sheet not found');
        (SheetsHelper.read as jest.Mock).mockRejectedValue(error);
        await getStudents(request, response, next);
        expect(response.status).toHaveBeenCalledWith(500);
        expect(response.json).toHaveBeenCalledWith({ error: 'Sheet not found' });
    });
});
