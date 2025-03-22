import { Request, Response } from 'express-serve-static-core';
import { SheetsHelper } from '../../helpers/sheets';
import { getLeaderboard } from '.';

jest.mock('../../helpers/sheets');

const mockRequest = (query: any = {}) => ({ query }) as Request;

const mockResponse = () => {
    const response = {} as Response;
    response.status = jest.fn().mockReturnValue(response);
    response.json = jest.fn().mockReturnValue(response);
    return response;
};

describe('getLeaderboard (Unit)', () => {
    let mockDoc: any;
    let mockBehaviorLogSheet: any;
    let mockStudentsSheet: any;

    beforeEach(() => {
        mockBehaviorLogSheet = {
            title: 'BehaviorLog',
            getRows: jest.fn().mockResolvedValue([]),
            headerValues: [ 'timestamp', 'studentID', 'respectful', 'responsible', 'safe', 'points', 'notes' ]
        };
        mockStudentsSheet = {
            title: 'Students',
            getRows: jest.fn().mockResolvedValue([]),
            headerValues: [ 'studentID', 'name', 'grade', 'teacher' ]
        };
        mockDoc = {
            sheetsByTitle: {
                'BehaviorLog': mockBehaviorLogSheet,
                'Students': mockStudentsSheet
            },
            loadInfo: jest.fn()
        };
        (SheetsHelper as jest.Mock).mockReturnValue(mockDoc);
    });

    it('should return leaderboard data with valid timeframe', async () => {
        mockBehaviorLogSheet.getRows.mockResolvedValue([
            {
                get: (field: string) => {
                    switch (field) {
                        case 'timestamp': return new Date().toISOString();
                        case'studentID': return '123';
                        case'respectful': return 'TRUE';
                        case 'safe': return 'TRUE';
                        case 'points': return '2';
                    }
                }
            }
        ]);
        mockStudentsSheet.getRows.mockResolvedValue([
            {
                get: (field: string) => {
                    switch (field) {
                        case 'studentID': return '123';
                        case 'name': return 'John Doe';
                        case 'grade': return '5th';
                        case 'teacher': return 'Mrs. Smith';
                    }
                }
            }
        ]);
        const request = mockRequest({ timeframe: 'week' });
        const response = mockResponse();
        await getLeaderboard(request, response, jest.fn());
        expect(response.json).toHaveBeenCalledWith(expect.arrayContaining([
            expect.objectContaining({
                studentID: 123,
                name: 'John Doe',
                points: 2
            })
        ]));
    });

    it('should handle invalid timeframe parameter', async () => {
        const request = mockRequest({ timeframe: 'invalid' });
        const response = mockResponse();
        await getLeaderboard(request, response, jest.fn());
        expect(response.status).toHaveBeenCalledWith(400);
        expect(response.json).toHaveBeenCalledWith({
            error: 'Invalid timeframe parameter'
        });
        expect(mockBehaviorLogSheet.getRows).not.toHaveBeenCalled();
    });

    it('should filter by teacher when specified', async () => {
        mockStudentsSheet.getRows.mockResolvedValue([
            {
                get: (field: string) => {
                    switch (field) {
                        case 'studentID': return '123';
                        case 'teacher': return 'Mrs. Smith';
                        case 'name': return 'John Doe';
                        case 'grade': return '5th';
                        default: return '';
                    }
                }
            },
            {
                get: (field: string) => {
                    switch (field) {
                        case 'studentID': return '456';
                        case 'teacher': return 'Mr. Johnson';
                        case 'name': return 'Jane Smith';
                        case 'grade': return '4th';
                        default: return '';
                    }
                }
            }
        ]);
        mockBehaviorLogSheet.getRows.mockResolvedValue([
            {
                get: (field: string) => {
                    switch (field) {
                        case 'studentID': return '123';
                        case 'points': return '2';
                        case 'respectful': return 'FALSE';
                        case 'responsible': return 'TRUE';
                        case 'safe': return 'TRUE';
                        case 'timestamp': return new Date().toISOString();
                        default: return '';
                    }
                }
            },
            {
                get: (field: string) => {
                    switch (field) {
                        case 'studentID': return '456';
                        case 'points': return '3';
                        case 'respectful': return 'TRUE';
                        case 'responsible': return 'TRUE';
                        case 'safe': return 'TRUE';
                        case 'timestamp': return new Date().toISOString();
                        default: return '';
                    }
                }
            }
        ]);
        const request = mockRequest({ teacher: 'Mrs. Smith' });
        const response = mockResponse();
        await getLeaderboard(request, response, jest.fn());
        expect(response.json).toHaveBeenCalledWith([
            expect.objectContaining({
                teacher: 'Mrs. Smith',
                points: 2
            })
        ]);
    });

    it('should handle empty datasets', async () => {
        mockBehaviorLogSheet.getRows.mockResolvedValue([]);
        mockStudentsSheet.getRows.mockResolvedValue([]);
        const request = mockRequest();
        const response = mockResponse();
        await getLeaderboard(request, response, jest.fn());
        expect(response.json).toHaveBeenCalledWith([]);
    });

    it('should handle missing sheets', async () => {
        delete mockDoc.sheetsByTitle.BehaviorLog;
        delete mockDoc.sheetsByTitle.Students;
        const request = mockRequest();
        const response = mockResponse();
        await getLeaderboard(request, response, jest.fn());
        expect(response.status).toHaveBeenCalledWith(500);
        expect(response.json).toHaveBeenCalledWith({
            error: 'Required sheets not found'
        });
    });
});
