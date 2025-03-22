import { Request, Response } from 'express-serve-static-core';
import { SheetsHelper } from '../../helpers/sheets';
import { submitForm } from '.';

jest.mock('../../helpers/sheets');

const mockRequest = (body: any) => ({ body }) as Request;

const mockResponse = () => {
    const response = {} as Response;
    response.status = jest.fn().mockReturnValue(response);
    response.json = jest.fn().mockReturnValue(response);
    return response;
};

describe('submitForm (Unit)', () => {
    let mockDoc: any;
    let mockSheet: any;

    beforeEach(() => {
        mockSheet = {
            title: 'BehaviorLog',
            addRow: jest.fn().mockResolvedValue(true),
            headerValues: [ 'timestamp', 'studentID', 'respectful', 'responsible', 'safe', 'points', 'notes' ]
        };
        mockDoc = {
            sheetsByTitle: { 'BehaviorLog': mockSheet },
            loadInfo: jest.fn(),
        };
        (SheetsHelper as jest.Mock).mockReturnValue(mockDoc);
    });

    it('should submit valid form data successfully', async () => {
        const request = mockRequest({
            studentID: 123,
            behaviors: { respectful: true, responsible: false, safe: true },
            notes: 'Test note'
        });
        const response = mockResponse();
        await submitForm(request, response, jest.fn());
        expect(mockSheet.addRow).toHaveBeenCalledWith({
            timestamp: expect.any(String),
            studentID: 123,
            respectful: true,
            responsible: false,
            safe: true,
            points: 2,
            notes: 'Test note'
        });
        expect(response.status).toHaveBeenCalledWith(201);
        expect(response.json).toHaveBeenCalledWith({ success: true });
    });

    it('should handle missing required fields', async () => {
        const request = mockRequest({});
        const response = mockResponse();
        await submitForm(request, response, jest.fn());
        expect(response.status).toHaveBeenCalledWith(400);
        expect(response.json).toHaveBeenCalledWith({
            error: expect.stringContaining('Invalid request format')
        });
        expect(mockSheet.addRow).not.toHaveBeenCalled();
    });

    it('should handle missing optional notes field', async () => {
        const request = mockRequest({
            studentID: 123,
            behaviors: { respectful: true, responsible: false, safe: true }
        });
        const response = mockResponse();
        await submitForm(request, response, jest.fn());
        expect(mockSheet.addRow).toHaveBeenCalledWith({
            timestamp: expect.any(String),
            studentID: 123,
            respectful: true,
            responsible: false,
            safe: true,
            points: 2,
            notes: ''
        });
        expect(response.status).toHaveBeenCalledWith(201);
        expect(response.json).toHaveBeenCalledWith({ success: true });
    });

    it('should handle sheet not found', async () => {
        delete mockDoc.sheetsByTitle.BehaviorLog;
        const request = mockRequest({
            studentID: 123,
            behaviors: { respectful: true, responsible: false, safe: true }
        });
        const response = mockResponse();
        await submitForm(request, response, jest.fn());
        expect(response.status).toHaveBeenCalledWith(500);
        expect(response.json).toHaveBeenCalledWith(
            expect.objectContaining({
                error: 'BehaviorLog sheet not found'
            })
        );
    });
});
