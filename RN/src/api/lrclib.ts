/**
 * LRCLIB API - 免费开源歌词 API
 * 文档: https://lrclib.net
 */

import {Lyrics, LrclibResponse} from '../types';

const LRCLIB_API = 'https://lrclib.net/api';

/**
 * 搜索歌词
 * @param artist 艺术家名
 * @param track 歌曲名
 * @param duration 歌曲时长（秒），可选用于更精确匹配
 */
export async function searchLyrics(
  artist: string,
  track: string,
  duration?: number,
): Promise<Lyrics | null> {
  try {
    let url = `${LRCLIB_API}/search?artist_name=${encodeURIComponent(artist)}&track_name=${encodeURIComponent(track)}`;
    if (duration) {
      url += `&duration=${duration}`;
    }

    const response = await fetch(url);

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data: LrclibResponse[] = await response.json();

    if (!data || data.length === 0) {
      return null;
    }

    // 返回第一个匹配结果
    const result = data[0];
    return {
      plainLyrics: result.plainLyrics || null,
      syncedLyrics: result.syncedLyrics || null,
    };
  } catch (error) {
    console.error('Search lyrics error:', error);
    return null;
  }
}

/**
 * 获取精确歌词
 * @param artist 艺术家名
 * @param track 歌曲名
 * @param duration 歌曲时长（秒）
 */
export async function getLyrics(
  artist: string,
  track: string,
  duration: number,
): Promise<Lyrics | null> {
  try {
    const url = `${LRCLIB_API}/get?artist_name=${encodeURIComponent(artist)}&track_name=${encodeURIComponent(track)}&duration=${duration}`;

    const response = await fetch(url);

    if (!response.ok) {
      if (response.status === 404) {
        // 精确匹配没找到，尝试搜索
        return searchLyrics(artist, track);
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data: LrclibResponse = await response.json();

    return {
      plainLyrics: data.plainLyrics || null,
      syncedLyrics: data.syncedLyrics || null,
    };
  } catch (error) {
    console.error('Get lyrics error:', error);
    return null;
  }
}

/**
 * 解析 LRC 格式歌词，提取时间线和文本
 * @param lrcText LRC 格式歌词文本
 * @returns 解析后的歌词数组 [{time: number, text: string}]
 */
export function parseLRC(lrcText: string): Array<{time: number; text: string}> {
  if (!lrcText) {
    return [];
  }

  const lines = lrcText.split('\n');
  const result: Array<{time: number; text: string}> = [];

  // LRC 时间格式: [mm:ss.xx] 或 [mm:ss:xx]
  const timeRegex = /\[(\d{2}):(\d{2})[.:](\d{2,3})\]/;
  // 跳过元数据行 [ti:xxx], [ar:xxx], [al:xxx], [by:xxx], [offset:xxx]
  const metaRegex = /\[(ti|ar|al|by|offset|la|length):/;

  for (const line of lines) {
    const trimmedLine = line.trim();
    if (!trimmedLine) {
      continue;
    }

    // 跳过元数据
    if (metaRegex.test(trimmedLine)) {
      continue;
    }

    const match = timeRegex.exec(trimmedLine);
    if (match) {
      const minutes = parseInt(match[1], 10);
      const seconds = parseInt(match[2], 10);
      const milliseconds = parseInt(match[3].padEnd(3, '0'), 10);
      const time = minutes * 60 + seconds + milliseconds / 1000;

      // 提取歌词文本（去掉所有时间标签）
      const text = trimmedLine
        .replace(/\[(\d{2}):(\d{2})[.:]\d{2,3}\]/g, '')
        .trim();

      if (text) {
        result.push({time, text});
      }
    }
  }

  // 按时间排序
  result.sort((a, b) => a.time - b.time);

  return result;
}

/**
 * 获取当前时间对应的歌词行索引
 * @param lyrics 解析后的歌词数组
 * @param currentTime 当前播放时间（秒）
 * @returns 当前歌词行索引，-1 表示无匹配
 */
export function getCurrentLyricIndex(
  lyrics: Array<{time: number; text: string}>,
  currentTime: number,
): number {
  if (!lyrics || lyrics.length === 0) {
    return -1;
  }

  let index = -1;
  for (let i = 0; i < lyrics.length; i++) {
    if (lyrics[i].time <= currentTime) {
      index = i;
    } else {
      break;
    }
  }

  return index;
}
