import { SheetsHelper } from '.';
import path from 'path';
import fs from 'fs';

describe('Test Setup Verification', () => {
    it('should find service account file', () => {
        const serviceAccountPath = path.resolve(__dirname, '../../../service-account.json');
        expect(fs.existsSync(serviceAccountPath)).toBe(true);
    });

    it('should have SPREADSHEET_ID set', () => {
        expect(process.env.SPREADSHEET_ID).toBeDefined();
    });
});

describe('SheetsHelper Integration', () => {
    const TEST_SHEET = 'TestSheet';
    const CLEANUP_RANGE = 'A1:Z1000';
    const INVALID_SHEET = 'NonExistentSheet';

    beforeAll(async () => {
        try {
            await SheetsHelper.write(TEST_SHEET, CLEANUP_RANGE, []);
        } catch (error: any) {
            console.error(`Setup cleanup failed - proceeding anyway\nError: ${error.message}`);
        }
    });

    it('should authenticate successfully', async () => {
        await expect(SheetsHelper.read(TEST_SHEET, 'A1'))
            .resolves.not.toThrow();
    });

    it('should write, read, and append data', async () => {
        // Write
        await SheetsHelper.write(TEST_SHEET, 'A1', [ 'Test1' ]);
        // Read
        let data = await SheetsHelper.read(TEST_SHEET, 'A1');
        expect(data).toEqual([ 'Test1' ]);
        // Append
        await SheetsHelper.append(TEST_SHEET, [ 'Test2' ]);
        //Verify
        data = await SheetsHelper.read(TEST_SHEET, 'A1:A2');
        expect(data).toEqual([ [ 'Test1' ], [ 'Test2' ] ]);
    });

    describe('SheetsHelper Error Handling', () => {
        it('should handle writing to invalid sheet', async () => {
            await expect(SheetsHelper.write(INVALID_SHEET, 'A1', [ 'Test' ]))
                .rejects.toThrow(/Failed to write data to sheet/);
        });

        it('should handle reading from invalid range', async () => {
            await expect(SheetsHelper.read(TEST_SHEET, 'InvalidRange!A1'))
                .rejects.toThrow(/Failed to read data from sheet/);
        });

        it('should handle appending invalid data format', async () => {
            await expect(SheetsHelper.append(TEST_SHEET, 'NotAnArray' as any))
                .rejects.toThrow(/Failed to append data to sheet/);
        });
    });

    describe('Value Formatting', () => {
        it('should wrap 1D array for write', async () => {
            await SheetsHelper.write(TEST_SHEET, 'B1', [ 'SingleValue' ]);
            const result = await SheetsHelper.read(TEST_SHEET, 'B1');
            expect(result).toEqual([ 'SingleValue' ]);
        });

        it('should preserve 2D array for write', async () => {
            await SheetsHelper.write(TEST_SHEET, 'C1', [ [ 'Row1' ], [ 'Row2' ] ]);
            const result = await SheetsHelper.read(TEST_SHEET, 'C1:C2');
            expect(result).toEqual([ [ 'Row1' ], [ 'Row2' ] ]);
        });

        it('should wrap single value for append', async () => {
            await SheetsHelper.append(TEST_SHEET, [ 'SingleAppend' ]);
            const result = await SheetsHelper.read(TEST_SHEET, 'A:Z');
            expect(result).toContainEqual([ 'SingleAppend' ]);
        });

        it('should preserve 2D array for append', async () => {
            await SheetsHelper.append(TEST_SHEET, [ [ '1', '2' ], [ '3', '4' ] ]);
            const result = await SheetsHelper.read(TEST_SHEET, 'A:Z');
            expect(result).toContainEqual([ '1', '2' ]);
            expect(result).toContainEqual([ '3', '4' ]);
        });

        it('should reject invalid append format', async () => {
            await expect(SheetsHelper.append(TEST_SHEET, null as any))
                .rejects.toThrow('Failed to append data');
        });
    });

    describe('Empty Value Handling', () => {
        it('should handle empty array for write', async () => {
            await SheetsHelper.write(TEST_SHEET, 'D1', []);
            const result = await SheetsHelper.read(TEST_SHEET, 'D1');
            expect(result).toEqual([]);
        });

        it('should handle empty read response', async () => {
            await SheetsHelper.write(TEST_SHEET, 'E1', []);
            const result = await SheetsHelper.read(TEST_SHEET, 'E1');
            expect(result).toEqual([]);
        });
    });

    afterAll(async () => {
        try {
            await SheetsHelper.write(TEST_SHEET, CLEANUP_RANGE, []);
        } catch (error: any) {
            console.error(`Teardown cleanup failed: ${error.message}`);
        }
    });
});
