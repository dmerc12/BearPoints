import { Row, Col } from 'react-bootstrap';
import { SelectFilter } from '../index';

interface LeaderboardFiltersProps {
    teacherFilter: string;
    gradeFilter: string;
    teacherOptions: Array<{ value: string; label: string }>;
    gradeOptions: Array<{ value: string; label: string }>;
    onFilterChange: (filterName: string, value: string) => void;
}

export function LeaderboardFilters({ teacherFilter, gradeFilter, teacherOptions, gradeOptions, onFilterChange }: LeaderboardFiltersProps) {
    return (
        <Row className='mb-3 justify-content-center'>
            <Col md={4} className='mb-2'>
                <SelectFilter
                    value={teacherFilter}
                    label='Teacher'
                    onChange={(value) => onFilterChange('teacherFilter', value)}
                    options={teacherOptions}
                />
            </Col>
            <Col md={4} className='mb-2'>
                <SelectFilter
                    value={gradeFilter}
                    label='Grade'
                    onChange={(value) => onFilterChange('gradeFilter', value)}
                    options={gradeOptions}
                />
            </Col>
        </Row>
    );
}
