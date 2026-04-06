import { withHealthAwareRetry, api } from './index';

export const fetchPaginated = async <T>(
    url: string,
    resourceName: keyof T,
    signal?: AbortSignal
): Promise<T> => {
    return withHealthAwareRetry(async () => {
        interface HalResponse {
            _embedded: Record<string, unknown[]>;
            page: { totalPages: number, totalElements: number }
        }
        const response = await api.get<HalResponse>(url, { signal });
        const embeddedKey = Object.keys(response.data._embedded).find(
            key => key.toLowerCase().includes(resourceName as string)
        ) || resourceName as string;
        const resources = response.data._embedded[embeddedKey] || [];
        const totalKey = `total${String(resourceName).charAt(0).toUpperCase() +
        String(resourceName).slice(1)}` as keyof T;
        return {
            [resourceName]: resources,
            totalPages: response.data.page.totalPages,
            [totalKey]: response.data.page.totalElements
        } as unknown as T;
    });
};
