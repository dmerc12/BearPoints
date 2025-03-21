import { Request, Response } from 'express-serve-static-core';
import { SheetsHelper } from '../../helpers/sheets';
import { submitForm } from '.';

jest.mock('../../helpers/sheets');

const mockRequest = (body: any) => ({
    body
}) as Request;

const mockResponse = () => {
    const response = {} as Response;
    response.status = jest.fn().mockReturnValue(response);
    response.json = jest.fn().mockReturnValue(response);
    return response;
};

const mockNext = jest.fn();

describe('submitForm (Unit)', () => {
    let request: Request;
    let response: Response;
    let next: jest.Mock;

    beforeEach(() => {
        jest.clearAllMocks();
        request = mockRequest({
            studentId: 123,
            behaviors: { respectful: true, responsible: false, safe: true },
            notes: 'Test note'
        });
        response = mockResponse();
        next = mockNext;
    });

    it('should submit valid form data successfully', async () => {
        (SheetsHelper.append as jest.Mock).mockResolvedValue(true);
        await submitForm(request, response, next);
        expect(SheetsHelper.append).toHaveBeenCalledWith('BehaviorLog', [[
            expect.any(String),
            123,
            true,
            false,
            true,
            2,
            'Test note'
        ]]);
        expect(response.status).toHaveBeenCalledWith(201);
        expect(response.json).toHaveBeenCalledWith({ success: true });
    });

    it('should handle missing required fields', async () => {
        request.body = {};
        await submitForm(request, response, next);
        expect(response.status).toHaveBeenCalledWith(400);
        expect(response.json).toHaveBeenCalledWith({
            error: expect.stringContaining('Invalid request format')
        });
    });

    it('should handle missing optional notes field', async () => {
        request.body = {
            studentId: 123,
            behaviors: { respectful: true, responsible: false, safe: true }
        };
        (SheetsHelper.append as jest.Mock).mockResolvedValue(true);
        await submitForm(request, response, next);
        expect(SheetsHelper.append).toHaveBeenCalledWith('BehaviorLog', [[
            expect.any(String),
            123,
            true,
            false,
            true,
            2,
            ''
        ]]);
        expect(response.status).toHaveBeenCalledWith(201);
        expect(response.json).toHaveBeenCalledWith({ success: true });
    });

    it('should handle sheets API errors', async () => {
        const error = new Error('API limit exceeded');
        (SheetsHelper.append as jest.Mock).mockRejectedValue(error);
        await submitForm(request, response, next);
        expect(response.status).toHaveBeenCalledWith(500);
        expect(response.json).toHaveBeenCalledWith({
            error: 'API limit exceeded'
        });
    });
});
