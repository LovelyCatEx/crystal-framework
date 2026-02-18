export type DateTimeFormat = 'YYYY-MM-DD HH:mm:ss' | string;

export function formatTimestamp(
    timestamp: number,
    format: DateTimeFormat = 'YYYY-MM-DD HH:mm:ss'
): string {
    if (timestamp === undefined) return '';

    const date = new Date(timestamp);

    if (isNaN(date.getTime())) {
        console.error('Invalid timestamp:', timestamp);
        return 'NaN';
    }

    const formatMap: Record<string, string> = {
        'YYYY': date.getFullYear().toString(),
        'YY': date.getFullYear().toString().slice(-2),

        'MM': (date.getMonth() + 1).toString().padStart(2, '0'),
        'M': (date.getMonth() + 1).toString(),

        'DD': date.getDate().toString().padStart(2, '0'),
        'D': date.getDate().toString(),

        'HH': date.getHours().toString().padStart(2, '0'),
        'H': date.getHours().toString(),
        'hh': (date.getHours() % 12 || 12).toString().padStart(2, '0'),
        'h': (date.getHours() % 12 || 12).toString(),

        'mm': date.getMinutes().toString().padStart(2, '0'),
        'm': date.getMinutes().toString(),

        'ss': date.getSeconds().toString().padStart(2, '0'),
        's': date.getSeconds().toString(),

        'ms': date.getMilliseconds().toString().padStart(3, '0'),

        'Q': Math.floor(date.getMonth() / 3 + 1).toString(),

        'WW': ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'][date.getDay()],
        'W': date.getDay().toString(),
    };

    const patterns = Object.keys(formatMap).sort((a, b) => b.length - a.length);

    let result = format;
    patterns.forEach(pattern => {
        const regex = new RegExp(pattern, 'g');
        result = result.replace(regex, formatMap[pattern]);
    });

    return result;
}

/**
 * 根据当前时间返回合适的问候语
 * @returns 问候语字符串，如"早上好"、"下午好"、"晚上好"
 */
export function getGreeting(): string {
    const now = new Date();
    const hour = now.getHours();

    if (hour >= 5 && hour < 12) {
        return '早上好';
    } else if (hour >= 12 && hour < 18) {
        return '下午好';
    } else {
        return '晚上好';
    }
}