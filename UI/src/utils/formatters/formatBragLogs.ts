export function formatBragLogDate(timestamp: string): string {
    return new Date(timestamp).toLocaleDateString();
}

export function getBragLogPointsVariant(points: number): string {
    if (points >= 5) return 'success';
    if (points >= 3) return 'warning';
    return 'danger';
}
