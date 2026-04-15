// 歌曲类型
export interface Song {
  id: string;
  title: string;
  artist: string;
  album: string;
  coverUrl: string | null;
  duration: number; // 毫秒
  neteaseId: string | null;
  isNetease: boolean;
  filePath?: string;
}

// 歌单类型
export interface Playlist {
  id: string;
  name: string;
  coverUrl: string | null;
  songs: Song[];
}

// 歌词类型
export interface Lyrics {
  lrc: string | null;
  yrc: string | null;
  translation: string | null;
  ttml: string | null;
}

// 播放状态
export interface PlayerState {
  currentSong: Song | null;
  isPlaying: boolean;
  position: number;
  duration: number;
  queue: Song[];
  queueIndex: number;
  repeatMode: RepeatMode;
  shuffleEnabled: boolean;
}

// 重复模式
export type RepeatMode = 'off' | 'all' | 'one';

// 设置类型
export interface Settings {
  theme: 'light' | 'dark' | 'system';
  primaryColor: string;
  lyricsFontSize: number;
  autoPlay: boolean;
  playbackSpeed: number;
  skipSilence: boolean;
}

// 均衡器预设
export interface EqualizerPreset {
  name: string;
  bands: number[];
}
