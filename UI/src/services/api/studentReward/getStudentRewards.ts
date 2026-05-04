import { PagedResponseDTO, StudentRewardDTO } from '../../types';
import { withHealthAwareRetry } from '../withHealthAwareRetry';
import { api } from '../api';

export const getStudentRewards = async (page = 0, size = 20, sort?: string,
                                        signal?: AbortSignal): Promise<PagedResponseDTO<StudentRewardDTO>> => {
    let url = `api/rewards?page=${page}&size=${size}`;
    if (sort) url += `&sort=${sort}`;
    return withHealthAwareRetry(() =>
        api.get<PagedResponseDTO<StudentRewardDTO>>(url, { signal })
            .then(r => r.data));
};
