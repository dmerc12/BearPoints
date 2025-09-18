import { useAppDispatch, useAppSelector, fetchLeaderboard, setTimeframe } from '../../store';
import { fullName, formatGrade, formatName } from '../../utils';
import { LeaderboardEntry, Timeframe } from '../../services';
import { TableColumn, useTable } from '../../hooks';
import { useMemo, useCallback } from 'react';

export interface UseLeaderboardTableProps {
    itemsPerPage?: number;
}

export function useLeaderboardTable({ itemsPerPage = 10 }: UseLeaderboardTableProps) {
    const dispatch = useAppDispatch();
    const { entries, currentTimeframe } = useAppSelector(
        state => state.leaderboard);

    const initialFilters = { teacherFilter: '', gradeFilter: '' }

    const rankingFunction = useCallback((data: LeaderboardEntry[]) => {
        return data.map((entry, index) => ({
            ...entry,
            rank: index + 1,
        }));
    }, []);

    const filterFunction = useCallback((data: LeaderboardEntry[],
                                        filters: { teacherFilter: string; gradeFilter: string }) => {
        return data.filter(entry => {
            const teacherMatch = filters.teacherFilter === ''
                || fullName(entry.teacher).toLowerCase().includes(filters.teacherFilter.toLowerCase());
            const gradeMatch = filters.gradeFilter === ''
                || formatGrade(entry.grade) === filters.gradeFilter;
            return teacherMatch && gradeMatch;
        });
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

    const fetchAction = useCallback(({ page, size, force }
                                     : { page?: number, size?: number, force?: boolean }
                                     = { page: 0, size: 1000, force: false }) => {
        console.log(`Page (${page}) and size (${size}) are ignored here`)
        dispatch(fetchLeaderboard({ timeframe: currentTimeframe, force: force || false }) as never);
    }, [dispatch, currentTimeframe]);

    const table = useTable({
        selector: (state) => ({
            data: state.leaderboard.entries,
            loading: state.leaderboard.loading,
            error: state.leaderboard.error
        }),
        initialFilters: initialFilters,
        filterFunction,
        rankingFunction,
        columnsBuilder,
        fetchAction: useCallback(() =>
            { fetchAction({ force: false }) },
            [fetchAction]
        ),
        itemsPerPage: itemsPerPage,
    });

    const teacherOptions = useMemo(() => {
        const teachers = [...new Set(entries.map(e => fullName(e.teacher)))];
        return teachers.map(teacher => ({ value: teacher, label: teacher }));
    }, [entries]);

    const gradeOptions = useMemo(() => {
        const grades = [...new Set(entries.map(e => formatGrade(e.grade)))];
        return grades.map(grade => ({ value: grade, label: grade }));
    }, [entries]);

    const retry = useCallback(() => {
        fetchAction({ force: true });
    }, [fetchAction]);

    const handleTimeframeChange = useCallback((timeframe: Timeframe) => {
        dispatch(setTimeframe(timeframe));
        retry();
    }, [dispatch, retry]);

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

    return { ...table, currentTimeframe, filtersConfig, handleTimeframeChange, retry };
}
