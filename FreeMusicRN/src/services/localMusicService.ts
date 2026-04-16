/**
 * 本地音乐服务
 * 使用 react-native-track-player 扫描本地音乐
 */

import {Platform, PermissionsAndroid} from 'react-native';
import {Song} from '../types';

// Android MediaStore 扫描
// 注意: react-native-track-player 主要用于播放，MediaStore 扫描需要原生模块
// 这里提供一个基础实现框架，实际项目可能需要:
// - react-native-fs 文件系统访问
// - react-native-media-library 媒体库访问
// - 或自定义原生模块

/**
 * 请求存储权限 (Android)
 */
export async function requestStoragePermission(): Promise<boolean> {
  if (Platform.OS !== 'android') {
    return true;
  }

  try {
    // Android 13 (API 33) 及以上
    if (Platform.Version >= 33) {
      const result = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.READ_MEDIA_AUDIO,
        {
          title: '音频文件权限',
          message: '需要访问您的音频文件以播放本地音乐',
          buttonNeutral: '稍后询问',
          buttonNegative: '取消',
          buttonPositive: '确定',
        },
      );
      return result === PermissionsAndroid.RESULTS.GRANTED;
    }

    // Android 12 及以下
    const sdkVersion = Platform.Version;
    const permissions: string[] = [];

    if (sdkVersion >= 30) {
      // Android 11+ 需要 MANAGE_EXTERNAL_STORAGE 或 READ_EXTERNAL_STORAGE
      permissions.push(PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE);
    } else {
      // Android 10 及以下
      permissions.push(PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE);
    }

    const results = await PermissionsAndroid.requestMultiple(permissions);

    return Object.values(results).every(
      result => result === PermissionsAndroid.RESULTS.GRANTED,
    );
  } catch (error) {
    console.error('Request storage permission error:', error);
    return false;
  }
}

/**
 * 检查存储权限状态
 */
export async function checkStoragePermission(): Promise<boolean> {
  if (Platform.OS !== 'android') {
    return true;
  }

  try {
    if (Platform.Version >= 33) {
      return await PermissionsAndroid.check(
        PermissionsAndroid.PERMISSIONS.READ_MEDIA_AUDIO,
      );
    }

    return await PermissionsAndroid.check(
      PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
    );
  } catch (error) {
    console.error('Check storage permission error:', error);
    return false;
  }
}

/**
 * 扫描本地音频文件
 * 
 * 注意: 这是一个简化实现
 * 实际项目中建议使用以下方案之一:
 * 1. react-native-fs + 自定义解析
 * 2. @react-native-community/media-library
 * 3. 自定义原生模块调用 MediaStore
 * 
 * 这里返回示例数据用于开发测试
 */
export async function scanLocalMusic(): Promise<Song[]> {
  const hasPermission = await checkStoragePermission();
  if (!hasPermission) {
    const granted = await requestStoragePermission();
    if (!granted) {
      console.warn('Storage permission not granted');
      return [];
    }
  }

  try {
    // TODO: 实现实际的 MediaStore 扫描
    // 实际实现需要:
    // 1. 使用 ContentResolver 查询 MediaStore.Audio.Media
    // 2. 读取音频文件的 metadata (title, artist, album, duration, albumArt)
    // 3. 转换为 Song 类型

    // 示例: 返回空数组，实际使用时替换为真实扫描逻辑
    console.log('Scanning local music...');
    
    // 模拟扫描延迟
    await new Promise(resolve => setTimeout(resolve, 500));

    // 这里应该实现真正的 MediaStore 查询
    // 临时返回空数组，需要时可从 Android 原生模块获取
    return [];

  } catch (error) {
    console.error('Scan local music error:', error);
    return [];
  }
}

/**
 * 获取音频文件路径
 * 用于 react-native-track-player 播放本地文件
 */
export function getLocalAudioPath(song: Song): string | null {
  if (!song.filePath) {
    return null;
  }

  // 确保路径格式正确
  let path = song.filePath;

  // Android: 确保路径以 file:// 开头或使用绝对路径
  if (Platform.OS === 'android') {
    if (path.startsWith('content://')) {
      // Content URI，直接使用
      return path;
    }
    if (!path.startsWith('/')) {
      path = '/' + path;
    }
    // 确保有读取权限或使用 MediaStore URI
    return `file://${path}`;
  }

  return path;
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
 * 从文件路径提取歌曲信息（基础实现）
 * 实际项目需要解析音频文件的 metadata
 */
export function extractSongInfoFromPath(
  filePath: string,
): Partial<Song> {
  const fileName = filePath.split('/').pop() || '';
  const nameWithoutExt = fileName.replace(/\.[^/.]+$/, '');

  // 常见格式: "歌手 - 歌曲名" 或 "歌曲名"
  const parts = nameWithoutExt.split(' - ');
  if (parts.length >= 2) {
    return {
      title: parts[1].trim(),
      artist: parts[0].trim(),
      album: '未知专辑',
    };
  }

  return {
    title: nameWithoutExt.trim(),
    artist: '未知艺术家',
    album: '未知专辑',
  };
}
