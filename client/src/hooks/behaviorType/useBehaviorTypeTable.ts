import { fetchBehaviorTypes, RootState } from '../../store';
import { BehaviorType } from '../../services';
import { useTable } from '../index';

export function useBehaviorTypeTable() {
    return useTable<BehaviorType, { nameSearch: string; statusFilter: string; pointValueFilter: string }>({
        fetchAction: fetchBehaviorTypes,
        selector: (state: RootState) => state.behaviorTypes,
        initialFilters: {
            nameSearch: '',
            statusFilter: '',
            pointValueFilter: ''
        }
    });
}
