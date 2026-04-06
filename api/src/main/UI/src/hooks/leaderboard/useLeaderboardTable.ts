import { useAppDispatch, useAppSelector, fetchLeaderboard, setTimeframe } from '../../store';
import { fullName, formatGrade, formatName } from '../../utils';
import { LeaderboardEntry, Timeframe } from '../../services';
import { useMemo, useCallback, useEffect } from 'react';
import { TableColumn, useTable } from '../../hooks';

export interface UseLeaderboardTableProps {
    itemsPerPage?: number;
}

export function useLeaderboardTable({ itemsPerPage = 20 }: UseLeaderboardTableProps) {
    const dispatch = useAppDispatch();
    const { data, currentTimeframe } = useAppSelector(
        state => state.leaderboard);

    const initialFilters = { teacherFilter: '', gradeFilter: '' }

    const getServerSortField = useCallback((clientField: string): string => {
        const fieldMap: Record<string, string> = {
            'rank': 'rank',
            'points': 'points',
            'studentName': 'student.lastName',
            'teacherName': 'teacher.lastName',
            'grade': 'grade'
        };
        return fieldMap[clientField] || clientField
    }, []);

    const columnsBuilder = useCallback(() => [
        {
            key: 'rank',
            header: 'Rank',
            render: (entry: LeaderboardEntry) => entry.rank,
            sortable: true,
        },
        {
            key: 'points',
            header: 'Points',
            render: (entry: LeaderboardEntry) => entry.points,
            sortable: true,
        },
        {
            key: 'studentName',
            header: 'Student Name',
            render: (entry: LeaderboardEntry) => fullName(entry.student),
            sortable: true,
        },
        {
            key: 'teacherName',
            header: 'Teacher',
            render: (entry: LeaderboardEntry) => formatName(entry.teacher),
            sortable: true,
        },
        {
            key: 'grade',
            header: 'Grade',
            render: (entry: LeaderboardEntry) => formatGrade(entry.grade),
            sortable: true,
        },
    ] as TableColumn<LeaderboardEntry>[], []);

    const fetchAction = useCallback(({ page, size, sort, force }
                                     : { page?: number, size?: number, sort?: string, force?: boolean }
                                     = {}) => {
        const pageToUse = page !== undefined ? page : 0;
        const sizeToUse = size !== undefined ? size : itemsPerPage
        dispatch(fetchLeaderboard({
            timeframe: currentTimeframe,
            page: pageToUse,
            size: sizeToUse,
            sort: sort,
            force: force || false
        }));
    }, [dispatch, currentTimeframe, itemsPerPage]);

    const table = useTable({
        selector: (state) => state.leaderboard,
        initialFilters,
        columnsBuilder,
        fetchAction,
        itemsPerPage,
        mode: 'read-only',
        getServerSortField
    });

    useEffect(() => {
        return () => {
            table.resetFilters();
            table.resetSorting();
        };
    }, [table]);

    const teacherOptions = useMemo(() => {
        const teachers = [...new Set(data.map(e => fullName(e.teacher)))];
        return teachers.map(teacher => ({ value: teacher, label: teacher }));
    }, [data]);

    const gradeOptions = useMemo(() => {
        const grades = [...new Set(data.map(e => formatGrade(e.grade)))];
        return grades.map(grade => ({ value: grade, label: grade }));
    }, [data]);

    const retry = useCallback(() => {
        fetchAction({ force: true });
    }, [fetchAction]);

    const handleTimeframeChange = useCallback((timeframe: Timeframe) => {
        dispatch(setTimeframe(timeframe));
        table.setCurrentPage(1);
    }, [dispatch, table]);

    const filtersConfig = [
        {
            key: 'teacherFilter',
            type: 'select' as const,
            label: 'Teacher',
            options: teacherOptions,
        },
        {
            key: 'gradeFilter',
            type: 'select' as const,
            label: 'Grade',
            options: gradeOptions,
        },
    ];

    const headerConfig = {
        title: 'Leaderboard',
        itemName: 'entries',
        showCreateButton: false,
    };

    return { ...table, currentTimeframe, filtersConfig, headerConfig, handleTimeframeChange, retry };
}
