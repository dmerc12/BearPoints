import { useAppDispatch, useAppSelector, fetchLeaderboard, setTimeframe } from '../../store';
import { useState, useEffect, useMemo, useCallback } from 'react';
import { fullName, formatGrade, formatName } from '../../utils';
import { LeaderboardEntry, Timeframe } from '../../services';
import { TableColumn } from '../../components';

export function useLeaderboardTable() {
    const dispatch = useAppDispatch();
    const { entries, loading, error, currentTimeframe } = useAppSelector(
        state => state.leaderboard);

    const [filters, setFilters] = useState({
        teacherFilter: '',
        gradeFilter: ''
    });

    useEffect(() => {
        dispatch(fetchLeaderboard({ timeframe: currentTimeframe, force: false }));
    }, [dispatch, currentTimeframe]);

    const rankedEntries = useMemo(() => {
        const safeEntries = Array.isArray(entries) ? entries : [];
        return safeEntries.map((entry, index) => ({
            ...entry,
            rank: index + 1,
        }));
    }, [entries]);

    const filteredEntries = useMemo(() => {
        return rankedEntries.filter(entry => {
            const teacherFilter = filters.teacherFilter;
            const gradeFilter = filters.gradeFilter;
            const teacherMatch = teacherFilter === '' || fullName(entry.teacher).includes(teacherFilter);
            const gradeMatch = gradeFilter === '' || formatGrade(entry.grade) === gradeFilter;
            return teacherMatch && gradeMatch;
        });
    }, [rankedEntries, filters]);

    const handleFilterChange = useCallback((filterName: string, value: string) => {
        setFilters(prev => ({
            ...prev,
            [filterName]: value
        }));
    }, []);

    const handleTimeframeChange = useCallback((timeframe: Timeframe) => {
        dispatch(setTimeframe(timeframe));
    }, [dispatch]);

    const uniqueTeachers = useMemo(() => [
        ...new Set(rankedEntries.map(e => fullName(e.teacher)))
    ], [rankedEntries]);

    const uniqueGrades = useMemo(() => [
        ...new Set(rankedEntries.map(e => formatGrade(e.grade)))
    ], [rankedEntries]);

    const teacherOptions = useMemo(() => uniqueTeachers.map(
        teacher => ({ value: teacher, label: teacher })), [uniqueTeachers]);

    const gradeOptions = useMemo(() => uniqueGrades.map(
        grade => ({ value: grade, label: grade })), [uniqueGrades]);

    const columns: TableColumn<LeaderboardEntry>[] = [
        {
            key: 'rank',
            header: 'Rank',
            render: (entry: LeaderboardEntry) => entry.rank
        },
        {
            key: 'points',
            header: 'Points',
            render: (entry: LeaderboardEntry) => entry.points
        },
        {
            key: 'studentName',
            header: 'Student Name',
            render: (entry: LeaderboardEntry) => fullName(entry.student)
        },
        {
            key: 'teacherName',
            header: 'Teacher',
            render: (entry: LeaderboardEntry) => formatName(entry.teacher)
        },
        {
            key: 'grade',
            header: 'Grade',
            render: (entry: LeaderboardEntry) => formatGrade(entry.grade)
        },
    ];

    const retry = useCallback(() => {
        dispatch(fetchLeaderboard({ timeframe: currentTimeframe, force: true }));
    }, [dispatch, currentTimeframe]);

    return {
        loading, error, currentTimeframe, filters, filteredEntries, teacherOptions, gradeOptions,
        columns, handleFilterChange, handleTimeframeChange, retry
    };
}
