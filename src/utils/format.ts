/**
 * 格式化工具函数
 */

/**
 * 格式化时长 (毫秒 -> mm:ss)
 */
export function formatDuration(ms: number): string {
  if (!ms || ms <= 0) {
    return '0:00';
  }

  const totalSeconds = Math.floor(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;

  return `${minutes}:${seconds.toString().padStart(2, '0')}`;
}

/**
 * 格式化时长 (毫秒 -> HH:mm:ss 或 mm:ss)
 */
export function formatDurationLong(ms: number): string {
  if (!ms || ms <= 0) {
    return '0:00';
  }

  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours > 0) {
    return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  return `${minutes}:${seconds.toString().padStart(2, '0')}`;
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) {
    return '0 B';
  }

  const units = ['B', 'KB', 'MB', 'GB'];
  const k = 1024;
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${units[i]}`;
}

/**
 * 格式化播放量
 */
export function formatPlayCount(count: number): string {
  if (count < 10000) {
    return count.toString();
  }

  if (count < 100000000) {
    return `${(count / 10000).toFixed(1)}万`;
  }

  return `${(count / 100000000).toFixed(1)}亿`;
}

/**
 * 格式化日期
 */
export function formatDate(date: Date | string | number): string {
  const d = typeof date === 'string' || typeof date === 'number' ? new Date(date) : date;

  const year = d.getFullYear();
  const month = (d.getMonth() + 1).toString().padStart(2, '0');
  const day = d.getDate().toString().padStart(2, '0');

  return `${year}-${month}-${day}`;
}

/**
 * 格式化相对时间
 */
export function formatRelativeTime(date: Date | string | number): string {
  const d = typeof date === 'string' || typeof date === 'number' ? new Date(date) : date;
  const now = new Date();
  const diff = now.getTime() - d.getTime();

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (days > 30) {
    return formatDate(d);
  }

  if (days > 0) {
    return `${days}天前`;
  }

  if (hours > 0) {
    return `${hours}小时前`;
  }

  if (minutes > 0) {
    return `${minutes}分钟前`;
  }

  return '刚刚';
}

/**
 * 截断文本
 */
export function truncateText(text: string, maxLength: number): string {
  if (!text || text.length <= maxLength) {
    return text;
  }

  return text.slice(0, maxLength - 3) + '...';
}

/**
 * 从 URL 提取域名
 */
export function extractDomain(url: string): string {
  try {
    const urlObj = new URL(url);
    return urlObj.hostname;
  } catch {
    return url;
  }
}

/**
 * 生成随机 ID
 */
export function generateId(prefix: string = ''): string {
  const timestamp = Date.now().toString(36);
  const random = Math.random().toString(36).substring(2, 8);
  return prefix ? `${prefix}_${timestamp}${random}` : `${timestamp}${random}`;
}

/**
 * 防抖函数
 */
export function debounce<T extends (...args: unknown[]) => void>(
  func: T,
  wait: number,
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null;

  return (...args: Parameters<T>) => {
    if (timeout) {
      clearTimeout(timeout);
    }

    timeout = setTimeout(() => {
      func(...args);
    }, wait);
  };
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: unknown[]) => void>(
  func: T,
  limit: number,
): (...args: Parameters<T>) => void {
  let inThrottle = false;

  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => {
        inThrottle = false;
      }, limit);
    }
  };
}
