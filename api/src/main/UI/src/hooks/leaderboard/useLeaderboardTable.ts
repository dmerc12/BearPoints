import { useAppDispatch, useAppSelector, fetchLeaderboard, setTimeframe, fetchTeachers } from '../../store';
import { LeaderboardEntryDTO, Timeframe, GradeLevel } from '../../services';
import { fullName, formatGrade, formatName } from '../../utils';
import { useMemo, useCallback, useEffect } from 'react';
import { TableColumn, useTable } from '../../hooks';

export interface UseLeaderboardTableProps {
    itemsPerPage?: number;
}

export function useLeaderboardTable({ itemsPerPage = 20 }: UseLeaderboardTableProps) {
    const dispatch = useAppDispatch();
    const { data: teachers, loading: teachersLoading } = useAppSelector(state => state.teachers);
    const { currentTimeframe } = useAppSelector(
        state => state.leaderboard);

    const initialFilters = { teacherId: '', grade: '' };

    useEffect(() => {
        if (teachers.length === 0 && !teachersLoading) {
            dispatch(fetchTeachers({ page: 0, size: 100, force: true }));
        }
    }, [teachers.length, teachersLoading, dispatch]);

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
            render: (entry: LeaderboardEntryDTO) => entry.rank,
            sortable: true,
        },
        {
            key: 'points',
            header: 'Points',
            render: (entry: LeaderboardEntryDTO) => entry.points,
            sortable: true,
        },
        {
            key: 'studentName',
            header: 'Student',
            render: (entry: LeaderboardEntryDTO) => fullName(entry.student),
            sortable: true,
        },
        {
            key: 'teacherName',
            header: 'Teacher',
            render: (entry: LeaderboardEntryDTO) => formatName(entry.teacher),
            sortable: true,
        },
        {
            key: 'grade',
            header: 'Grade',
            render: (entry: LeaderboardEntryDTO) => formatGrade(entry.grade),
            sortable: true,
        },
    ] as TableColumn<LeaderboardEntryDTO>[], []);

    const fetchAction = useCallback((params: {
        page: number;
        size: number;
        sort?: string;
        force?: boolean;
        teacherId?: number;
        grade?: GradeLevel;
    }) => {
        dispatch(fetchLeaderboard({
            timeframe: currentTimeframe,
            page: params.page,
            size: params.size,
            sort: params.sort,
            teacherId: params.teacherId,
            grade: params.grade,
            force: params.force || false,
        }));
    }, [dispatch, currentTimeframe]);

    const getFetchParams = useCallback((
        filters: typeof initialFilters,
        page: number,
        size: number,
        sort?: string,
    ) => {
        const teacherId = filters.teacherId ? parseInt(filters.teacherId, 10) : undefined;
        const grade = filters.grade ? (filters.grade as GradeLevel) : undefined;
        return { page, size, sort, teacherId, grade };
    }, []);

    const teacherOptions = useMemo(() => {
        return teachers.map(teacher => ({
            value: teacher.id!.toString(),
            label: fullName(teacher),
        }));
    }, [teachers]);

    const gradeOptions = useMemo(() => {
        return Object.values(GradeLevel).map(grade => ({
            value: grade,
            label: formatGrade(grade),
        }));
    }, []);

    const filtersConfig = [
        {
            key: 'teacherId',
            type: 'select' as const,
            label: 'Teacher',
            options: teacherOptions,
        },
        {
            key: 'grade',
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

    const table = useTable({
        selector: (state) => state.leaderboard,
        initialFilters,
        columnsBuilder,
        fetchAction,
        getFetchParams,
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

    const handleTimeframeChange = useCallback((timeframe: Timeframe) => {
        dispatch(setTimeframe(timeframe));
        table.setCurrentPage(1);
    }, [dispatch, table]);

    return { ...table, currentTimeframe, filtersConfig, headerConfig, handleTimeframeChange };
}
