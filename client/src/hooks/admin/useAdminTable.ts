import { fetchAdmins, RootState } from '../../store';
import { UserDTO } from '../../services';
import { useTable } from '../index';

export function useAdminTable() {
    return useTable<UserDTO, { nameSearch: string; emailSearch: string }>({
        fetchAction: fetchAdmins,
        selector: (state: RootState) => state.admins,
        initialFilters: {
            nameSearch: '',
            emailSearch: '',
        }
    });
}