/**
 * 网易云音乐 API
 * 与 Android 版本保持一致
 */

import {Song, NeteaseSearchResponse, NeteaseLyricResponse} from '../types';

// API 基础 URL
const BASE_URL = 'https://zm.wwoyun.cn';
const PLAY_URL = 'https://api.qijieya.cn/meting/';

/**
 * 搜索歌曲
 * @param keywords 搜索关键词
 * @param limit 返回数量
 * @param offset 偏移量
 */
export async function searchSongs(
  keywords: string,
  limit: number = 20,
  offset: number = 0,
): Promise<{songs: Song[]; hasMore: boolean; total: number}> {
  try {
    const response = await fetch(
      `${BASE_URL}/cloudsearch?keywords=${encodeURIComponent(keywords)}&limit=${limit}&offset=${offset}`,
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data: NeteaseSearchResponse = await response.json();

    if (data.code !== 200 || !data.result?.songs) {
      return {songs: [], hasMore: false, total: 0};
    }

    const songs: Song[] = data.result.songs.map(song => ({
      id: `netease_${song.id}`,
      title: song.name,
      artist: song.artists.map(a => a.name).join(', '),
      album: song.album.name,
      coverUrl: song.album.picUrl || null,
      duration: song.duration,
      neteaseId: String(song.id),
      isNetease: true,
    }));

    return {
      songs,
      hasMore: data.result.hasMore || false,
      total: data.result.songCount || songs.length,
    };
  } catch (error) {
    console.error('Search songs error:', error);
    return {songs: [], hasMore: false, total: 0};
  }
}

/**
 * 获取歌曲播放 URL
 * @param neteaseId 网易云歌曲 ID
 */
export async function getPlayUrl(neteaseId: string): Promise<string | null> {
  try {
    const response = await fetch(
      `${PLAY_URL}?id=${neteaseId}&type=song`,
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const text = await response.text();
    // Meting API 返回的可能是 JSON 或直接文本 URL
    try {
      const json = JSON.parse(text);
      return json.url || null;
    } catch {
      // 直接返回文本内容（可能是 URL）
      return text.trim() || null;
    }
  } catch (error) {
    console.error('Get play URL error:', error);
    return null;
  }
}

/**
 * 获取歌词
 * @param neteaseId 网易云歌曲 ID
 */
export async function getLyric(
  neteaseId: string,
): Promise<{lrc: string | null; translation: string | null}> {
  try {
    const response = await fetch(
      `${BASE_URL}/lyric/new?id=${neteaseId}`,
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data: NeteaseLyricResponse = await response.json();

    if (data.code !== 200) {
      return {lrc: null, translation: null};
    }

    return {
      lrc: data.netease?.lyric || null,
      translation: data.netease?.tlyric?.lyric || null,
    };
  } catch (error) {
    console.error('Get lyric error:', error);
    return {lrc: null, translation: null};
  }
}

/**
 * 获取歌曲详情（用于补充信息）
 * @param neteaseId 网易云歌曲 ID
 */
export async function getSongDetail(
  neteaseId: string,
): Promise<Song | null> {
  try {
    const response = await fetch(
      `${BASE_URL}/song/detail?ids=${neteaseId}`,
    );

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();

    if (data.code !== 200 || !data.songs?.[0]) {
      return null;
    }

    const song = data.songs[0];
    return {
      id: `netease_${song.id}`,
      title: song.name,
      artist: song.artists.map((a: {name: string}) => a.name).join(', '),
      album: song.album?.name || '未知专辑',
      coverUrl: song.album?.picUrl || null,
      duration: song.duration,
      neteaseId: String(song.id),
      isNetease: true,
    };
  } catch (error) {
    console.error('Get song detail error:', error);
    return null;
  }
}

/**
 * 批量获取歌曲播放 URL
 * @param songs 歌曲列表
 */
export async function batchGetPlayUrls(
  songs: Song[],
): Promise<Map<string, string>> {
  const results = new Map<string, string>();

  // 并发请求，但限制并发数
  const batchSize = 5;
  for (let i = 0; i < songs.length; i += batchSize) {
    const batch = songs.slice(i, i + batchSize);
    const promises = batch.map(async song => {
      if (song.neteaseId) {
        const url = await getPlayUrl(song.neteaseId);
        if (url) {
          results.set(song.id, url);
        }
      }
    });
    await Promise.all(promises);
  }

  return results;
}
