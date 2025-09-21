import { fetchStudents, RootState } from '../../store';
import { Student } from '../../services';
import { useTable } from '../index';

export function useStudentTable() {
    return useTable<Student, { nameSearch: string, teacherFilter: string; gradeFilter: string }>({
        fetchAction: fetchStudents,
        selector: (state: RootState) => state.students,
        initialFilters: {
            nameSearch: '',
            teacherFilter: '',
            gradeFilter: ''
        }
    });
}
