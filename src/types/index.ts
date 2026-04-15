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
  url?: string; // 播放 URL
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
  plainLyrics: string | null;
  syncedLyrics: string | null;
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

// 搜索结果类型
export interface SearchResult {
  songs: Song[];
  hasMore: boolean;
  total: number;
}

// 网易云搜索响应
export interface NeteaseSearchResponse {
  result: {
    songs?: Array<{
      id: number;
      name: string;
      artists: Array<{ id: number; name: string }>;
      album: { id: number; name: string; picUrl: string };
      duration: number;
    }>;
    hasMore: boolean;
    songCount: number;
  };
  code: number;
}

// 网易云歌曲详情响应
export interface NeteaseSongDetailResponse {
  songs: Array<{
    id: number;
    name: string;
    artists: Array<{ id: number; name: string }>;
    album: { id: number; name: string; picUrl: string };
    duration: number;
    djId?: number;
    reason?: string;
   Privilege?: {
      cs: boolean;
      maxbr: number;
      fl: number;
    };
  }>;
  code: number;
}

// 网易云歌词响应
export interface NeteaseLyricResponse {
  netease?: {
    lyric: string;
    tlyric?: {
      lyric: string;
    };
  };
  code: number;
}

// LRCLIB 歌词响应
export interface LrclibResponse {
  id: number;
  name: string;
  trackName: string;
  artistName: string;
  albumName: string;
  duration: number;
  instrumental: boolean;
  plainLyrics: string | null;
  syncedLyrics: string | null;
}
