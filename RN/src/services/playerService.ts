/**
 * 播放器服务 - react-native-track-player
 * 后台播放、通知栏控制、蓝牙控制
 */

import TrackPlayer, {
  AppKilledPlaybackBehavior,
  Capability,
  Event,
  RepeatMode,
  State,
  Track,
} from 'react-native-track-player';
import {Song} from '../types';
import {useMusicStore} from '../store/musicStore';

// 将 Song 转换为 TrackPlayer Track 格式
export function songToTrack(song: Song, url?: string): Track {
  return {
    id: song.id,
    url: url || song.url || '', // TrackPlayer 需要有效 URL
    title: song.title,
    artist: song.artist,
    album: song.album,
    artwork: song.coverUrl || undefined,
    duration: song.duration / 1000, // 转换为秒
  };
}

// 将 TrackPlayer Track 转换为 Song
export function trackToSong(track: Track): Song {
  return {
    id: track.id as string,
    title: track.title || '未知歌曲',
    artist: track.artist || '未知艺术家',
    album: track.album || '未知专辑',
    coverUrl: track.artwork as string | null,
    duration: (track.duration || 0) * 1000, // 转换为毫秒
    neteaseId: null,
    isNetease: false,
    url: track.url as string,
  };
}

// 初始化播放器
export async function setupPlayer(): Promise<boolean> {
  try {
    await TrackPlayer.setupPlayer({
      autoHandleInterruptions: true,
    });

    await TrackPlayer.updateOptions({
      android: {
        appKilledPlaybackBehavior:
          AppKilledPlaybackBehavior.StopPlaybackAndRemoveNotification,
      },
      capabilities: [
        Capability.Play,
        Capability.Pause,
        Capability.SkipToNext,
        Capability.SkipToPrevious,
        Capability.SeekTo,
        Capability.Stop,
      ],
      compactCapabilities: [
        Capability.Play,
        Capability.Pause,
        Capability.SkipToNext,
        Capability.SkipToPrevious,
      ],
      progressUpdateEventInterval: 1, // 每秒更新进度
    });

    return true;
  } catch (error) {
    console.error('Setup player error:', error);
    return false;
  }
}

// 播放歌曲
export async function playSong(song: Song, url: string): Promise<void> {
  try {
    const track = songToTrack(song, url);
    await TrackPlayer.reset();
    await TrackPlayer.add(track);
    await TrackPlayer.play();

    // 更新 store 状态
    useMusicStore.getState().setCurrentSong(song);
    useMusicStore.getState().setIsPlaying(true);
    useMusicStore.getState().setDuration(song.duration);
    useMusicStore.getState().setPosition(0);

    // 添加到播放历史
    useMusicStore.getState().addToPlayHistory(song);
  } catch (error) {
    console.error('Play song error:', error);
    throw error;
  }
}

// 播放队列
export async function playQueue(
  songs: Song[],
  urls: Map<string, string>,
  startIndex: number = 0,
): Promise<void> {
  try {
    const tracks = songs.map(song => {
      const url = urls.get(song.id) || song.url || '';
      return songToTrack(song, url);
    });

    await TrackPlayer.reset();
    await TrackPlayer.add(tracks);

    if (startIndex > 0) {
      await TrackPlayer.skip(startIndex);
    }

    await TrackPlayer.play();

    // 更新 store 状态
    useMusicStore.getState().setQueue(songs, startIndex);
    useMusicStore.getState().setIsPlaying(true);
  } catch (error) {
    console.error('Play queue error:', error);
    throw error;
  }
}

// 暂停播放
export async function pause(): Promise<void> {
  try {
    await TrackPlayer.pause();
    useMusicStore.getState().setIsPlaying(false);
  } catch (error) {
    console.error('Pause error:', error);
  }
}

// 继续播放
export async function resume(): Promise<void> {
  try {
    await TrackPlayer.play();
    useMusicStore.getState().setIsPlaying(true);
  } catch (error) {
    console.error('Resume error:', error);
  }
}

// 停止播放
export async function stop(): Promise<void> {
  try {
    await TrackPlayer.stop();
    useMusicStore.getState().setIsPlaying(false);
    useMusicStore.getState().setPosition(0);
  } catch (error) {
    console.error('Stop error:', error);
  }
}

// 跳到下一首
export async function skipToNext(): Promise<void> {
  try {
    await TrackPlayer.skipToNext();
  } catch (error) {
    console.error('Skip to next error:', error);
  }
}

// 跳到上一首
export async function skipToPrevious(): Promise<void> {
  try {
    await TrackPlayer.skipToPrevious();
  } catch (error) {
    console.error('Skip to previous error:', error);
  }
}

// 跳到指定位置
export async function seekTo(position: number): Promise<void> {
  try {
    await TrackPlayer.seekTo(position);
    useMusicStore.getState().setPosition(position * 1000); // 转换为毫秒
  } catch (error) {
    console.error('Seek to error:', error);
  }
}

// 设置播放速度
export async function setRate(rate: number): Promise<void> {
  try {
    await TrackPlayer.setRate(rate);
    useMusicStore.getState().updateSettings({playbackSpeed: rate});
  } catch (error) {
    console.error('Set rate error:', error);
  }
}

// 设置循环模式
export async function setRepeatMode(mode: 'off' | 'all' | 'one'): Promise<void> {
  try {
    const repeatModeMap = {
      off: RepeatMode.Off,
      all: RepeatMode.Queue,
      one: RepeatMode.Track,
    };
    await TrackPlayer.setRepeatMode(repeatModeMap[mode]);
    useMusicStore.getState().setRepeatMode(mode);
  } catch (error) {
    console.error('Set repeat mode error:', error);
  }
}

// 获取当前播放位置（秒）
export async function getPosition(): Promise<number> {
  try {
    const position = await TrackPlayer.getProgress();
    return position.position;
  } catch (error) {
    console.error('Get position error:', error);
    return 0;
  }
}

// 获取当前状态
export async function getState(): Promise<State> {
  try {
    return await TrackPlayer.getPlaybackState().then(state => state.state);
  } catch (error) {
    console.error('Get state error:', error);
    return State.None;
  }
}

// 注册播放服务（必须在 index.js 中调用）
export async function registerPlaybackService(): Promise<void> {
  TrackPlayer.registerPlaybackService(() => async function () {
    // 监听事件
    TrackPlayer.addEventListener(Event.RemotePlay, () => {
      TrackPlayer.play();
      useMusicStore.getState().setIsPlaying(true);
    });

    TrackPlayer.addEventListener(Event.RemotePause, () => {
      TrackPlayer.pause();
      useMusicStore.getState().setIsPlaying(false);
    });

    TrackPlayer.addEventListener(Event.RemoteNext, () => {
      TrackPlayer.skipToNext();
      handleTrackChange();
    });

    TrackPlayer.addEventListener(Event.RemotePrevious, () => {
      TrackPlayer.skipToPrevious();
      handleTrackChange();
    });

    TrackPlayer.addEventListener(Event.RemoteSeek, (event) => {
      TrackPlayer.seekTo(event.position);
      useMusicStore.getState().setPosition(event.position * 1000);
    });

    TrackPlayer.addEventListener(Event.RemoteStop, () => {
      TrackPlayer.stop();
      useMusicStore.getState().setIsPlaying(false);
    });

    // 播放完成事件
    TrackPlayer.addEventListener(Event.PlaybackQueueEnded, () => {
      const {repeatMode} = useMusicStore.getState().player;
      if (repeatMode === 'all') {
        TrackPlayer.seekTo(0);
        TrackPlayer.play();
      } else {
        useMusicStore.getState().setIsPlaying(false);
      }
    });

    // 播放ACTIVE Track切换事件
    TrackPlayer.addEventListener(Event.PlaybackActiveTrackChanged, (event) => {
      if (event.track) {
        const song = trackToSong(event.track);
        useMusicStore.getState().setCurrentSong(song);
      }
    });

    // 进度更新事件
    TrackPlayer.addEventListener(Event.PlaybackProgressUpdated, (event) => {
      useMusicStore.getState().setPosition(event.position * 1000);
    });
  });
}

// 处理 Track 切换（更新 store 状态）
async function handleTrackChange(): Promise<void> {
  try {
    const index = await TrackPlayer.getActiveTrackIndex();
    if (index !== undefined) {
      const queue = useMusicStore.getState().player.queue;
      if (queue[index]) {
        useMusicStore.getState().setCurrentSong(queue[index]);
      }
    }
  } catch (error) {
    console.error('Handle track change error:', error);
  }
}

// 导出服务
export const playerService = {
  setupPlayer,
  playSong,
  playQueue,
  pause,
  resume,
  stop,
  skipToNext,
  skipToPrevious,
  seekTo,
  setRate,
  setRepeatMode,
  getPosition,
  getState,
  registerPlaybackService,
};

export default playerService;
