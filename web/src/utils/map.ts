export function sortByMapOrder<T extends string>(arr: T[], map: Map<T, any>): T[] {
    const orderMap = new Map<T, number>();
    Array.from(map.keys()).forEach((key, index) => {
        orderMap.set(key, index);
    });

    return [...arr].sort((a, b) => {
        const orderA = orderMap.get(a) ?? Number.MAX_SAFE_INTEGER;
        const orderB = orderMap.get(b) ?? Number.MAX_SAFE_INTEGER;
        return orderA - orderB;
    });
}
