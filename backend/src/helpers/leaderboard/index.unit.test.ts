import { filterLogsByTimeframe, calculateLeaderboard } from '.';
import { BehaviorLog, Student } from '../../types';

const daysAgo = (days: number): string => {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date.toISOString();
};

// Mock data
const mockStudents: Student[] = [
    { studentID: 1, name: 'Student A', teacher: 'Teacher 1', grade: '1' },
    { studentID: 2, name: 'Student B', teacher: 'Teacher 2', grade: '3' },
];

const mockLogs: BehaviorLog[] = [
    { timestamp: daysAgo(50), studentID: 1, respectful: true, responsible: true, safe: true, points: 10, notes: 'test log 1' },
    { timestamp: daysAgo(8), studentID: 1, respectful: false, responsible: true, safe: true, points: 5, notes: 'test log 2' },
    { timestamp: daysAgo(5), studentID: 2, respectful: true, responsible: true, safe: true, points: 8, notes: 'test log 3' },
    { timestamp: daysAgo(2), studentID: 2, respectful: true, responsible: true, safe: true, points: 12, notes: 'test log 4' },
];

describe('filterLogsByTimeframe', () => {
    test('filters logs for week timeframe', () => {
        const result = filterLogsByTimeframe(mockLogs, 'week');
        expect(result).toEqual([
            mockLogs[2],
            mockLogs[3]
        ]);
    });

    test('filters logs for month timeframe', () => {
        const result = filterLogsByTimeframe(mockLogs, 'month');
        expect(result).toEqual([
            mockLogs[ 1 ],
            mockLogs[ 2 ],
            mockLogs[ 3 ]
        ]);
    });

    test('filters logs for semester timeframe', () => {
        const result = filterLogsByTimeframe(mockLogs, 'semester');
        expect(result).toEqual(mockLogs);
    });

    test('filters logs for year timeframe', () => {
        const result = filterLogsByTimeframe(mockLogs, 'year');
        expect(result).toEqual(mockLogs);
    });

    test('handles custom timeframe', () => {
        const result = filterLogsByTimeframe(mockLogs, 'custom');
        expect(result).toEqual([]);
    });

    describe('semester timeframe', () => {
        test('covers all semester calculation branches', () => {
            const mockDate = new Date(daysAgo(0));
            mockDate.setMonth(7); // August
            const realDate = Date;
            global.Date = class extends Date {
                constructor () {
                    super(mockDate);
                }
            } as any;
            const logs = [
                { timestamp: daysAgo(5), studentID: 1, respectful: true, responsible: true, safe: true, points: 10, notes: 'Current semester log' },
                { timestamp: daysAgo(35), studentID: 2, respectful: true, responsible: true, safe: true, points: 5, notes: 'Previous semester log' }
            ];
            const result = filterLogsByTimeframe(logs, 'semester');
            expect(result).toEqual(logs);
            global.Date = realDate;
        });
    });
});

describe('calculateLeaderboard', () => {
    test('calculates leaderboard with correct points', () => {
        const result = calculateLeaderboard(mockLogs, mockStudents);
        expect(result).toEqual([
            { studentID: 2, name: 'Student B', teacher: 'Teacher 2', points: 20 },
            { studentID: 1, name: 'Student A', teacher: 'Teacher 1', points: 15 },
        ]);
    });

    test('filters by teacher', () => {
        const result = calculateLeaderboard(mockLogs, mockStudents, 'Teacher 1');
        expect(result).toEqual([
            { studentID: 1, name: 'Student A', teacher: 'Teacher 1', points: 15 },
        ]);
    });

    test('ignores students not in the list', () => {
        const result = calculateLeaderboard(mockLogs, mockStudents);
        expect(result).not.toContainEqual(expect.objectContaining({ studentID: 3 }));
    });

    test('handles empty logs', () => {
        const result = calculateLeaderboard([], mockStudents);
        expect(result).toEqual([]);
    });

    test('handles empty students', () => {
        const result = calculateLeaderboard(mockLogs, []);
        expect(result).toEqual([]);
    });

    test('sorts by points descending', () => {
        const unsortedLogs = [
            { timestamp: '2024-02-20T00:00:00Z', studentID: 1, respectful: false, responsible: true, safe: true, points: 5, notes: 'test log 2' },
            { timestamp: '2024-01-25T00:00:00Z', studentID: 2, respectful: true, responsible: true, safe: true, points: 10, notes: 'test log 1' },
        ];
        const result = calculateLeaderboard(unsortedLogs, mockStudents);
        expect(result[ 0 ].points).toBe(10);
        expect(result[ 1 ].points).toBe(5);
    });
});
