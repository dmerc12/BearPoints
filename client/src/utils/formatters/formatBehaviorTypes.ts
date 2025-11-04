export function formatBehaviorTypeStatus(active: boolean): string {
    return active ? 'Active' : 'Inactive';
}

export function getBehaviorTypeStatusVariant(active: boolean): string {
    return active ? 'success' : 'secondary';
}
