/**
 * FreeMusic RN - Zustand 状态管理
 * 完整的播放状态、歌单、收藏、设置持久化
 */

import {create} from 'zustand';
import {persist, createJSONStorage} from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {
  Song,
  Playlist,
  PlayerState,
  RepeatMode,
  Settings,
} from '../types';

// Store 接口定义
interface MusicStore {
  // ============ 播放状态 ============
  player: PlayerState;
  setCurrentSong: (song: Song | null) => void;
  setIsPlaying: (isPlaying: boolean) => void;
  setPosition: (position: number) => void;
  setDuration: (duration: number) => void;
  setQueue: (queue: Song[], index?: number) => void;
  addToQueue: (song: Song) => void;
  removeFromQueue: (songId: string) => void;
  clearQueue: () => void;
  setRepeatMode: (mode: RepeatMode) => void;
  toggleShuffle: () => void;
  playNext: () => void;
  playPrevious: () => void;
  playSongAtIndex: (index: number) => void;

  // ============ 歌单 ============
  playlists: Playlist[];
  favoritesPlaylist: Playlist;
  addPlaylist: (name: string) => Playlist;
  deletePlaylist: (id: string) => void;
  renamePlaylist: (id: string, name: string) => void;
  addToPlaylist: (playlistId: string, song: Song) => void;
  removeFromPlaylist: (playlistId: string, songId: string) => void;
  getPlaylistById: (id: string) => Playlist | undefined;

  // ============ 收藏 ============
  addToFavorites: (song: Song) => void;
  removeFromFavorites: (songId: string) => void;
  isFavorite: (songId: string) => boolean;
  toggleFavorite: (song: Song) => void;

  // ============ 本地歌曲 ============
  localSongs: Song[];
  setLocalSongs: (songs: Song[]) => void;
  addLocalSong: (song: Song) => void;
  removeLocalSong: (songId: string) => void;

  // ============ 搜索历史 ============
  searchHistory: string[];
  addSearchHistory: (keyword: string) => void;
  removeSearchHistory: (keyword: string) => void;
  clearSearchHistory: () => void;

  // ============ 设置 ============
  settings: Settings;
  updateSettings: (settings: Partial<Settings>) => void;
  resetSettings: () => void;

  // ============ 播放历史 ============
  playHistory: Song[];
  addToPlayHistory: (song: Song) => void;
  clearPlayHistory: () => void;
}

// 默认设置
const defaultSettings: Settings = {
  theme: 'dark',
  primaryColor: '#6366F1',
  lyricsFontSize: 16,
  autoPlay: true,
  playbackSpeed: 1.0,
  skipSilence: false,
};

// 默认播放状态
const defaultPlayerState: PlayerState = {
  currentSong: null,
  isPlaying: false,
  position: 0,
  duration: 0,
  queue: [],
  queueIndex: 0,
  repeatMode: 'off',
  shuffleEnabled: false,
};

export const useMusicStore = create<MusicStore>()(
  persist(
    (set, get) => ({
      // ============ 播放状态 ============
      player: {...defaultPlayerState},

      setCurrentSong: (song) =>
        set((state) => ({player: {...state.player, currentSong: song}})),

      setIsPlaying: (isPlaying) =>
        set((state) => ({player: {...state.player, isPlaying}})),

      setPosition: (position) =>
        set((state) => ({player: {...state.player, position}})),

      setDuration: (duration) =>
        set((state) => ({player: {...state.player, duration}})),

      setQueue: (queue, index = 0) =>
        set((state) => {
          if (queue.length === 0) {
            return {
              player: {
                ...defaultPlayerState,
                repeatMode: state.player.repeatMode,
                shuffleEnabled: state.player.shuffleEnabled,
              },
            };
          }
          const clampedIndex = Math.max(0, Math.min(index, queue.length - 1));
          return {
            player: {
              ...state.player,
              queue,
              queueIndex: clampedIndex,
              currentSong: queue[clampedIndex],
              position: 0,
            },
          };
        }),

      addToQueue: (song) =>
        set((state) => ({
          player: {
            ...state.player,
            queue: [...state.player.queue, song],
          },
        })),

      removeFromQueue: (songId) =>
        set((state) => {
          const newQueue = state.player.queue.filter((s) => s.id !== songId);
          const newIndex = Math.min(
            state.player.queueIndex,
            Math.max(0, newQueue.length - 1),
          );
          return {
            player: {
              ...state.player,
              queue: newQueue,
              queueIndex: newIndex,
              currentSong: newQueue[newIndex] || null,
            },
          };
        }),

      clearQueue: () =>
        set((state) => ({
          player: {
            ...state.player,
            queue: [],
            queueIndex: 0,
            currentSong: null,
            isPlaying: false,
            position: 0,
            duration: 0,
          },
        })),

      setRepeatMode: (mode) =>
        set((state) => ({player: {...state.player, repeatMode: mode}})),

      toggleShuffle: () =>
        set((state) => ({
          player: {
            ...state.player,
            shuffleEnabled: !state.player.shuffleEnabled,
          },
        })),

      playNext: () =>
        set((state) => {
          const {queue, queueIndex, repeatMode, shuffleEnabled} = state.player;
          if (queue.length === 0) {
            return state;
          }

          let nextIndex: number;
          if (shuffleEnabled) {
            nextIndex = Math.floor(Math.random() * queue.length);
          } else if (queueIndex >= queue.length - 1) {
            nextIndex = repeatMode === 'all' ? 0 : queueIndex;
          } else {
            nextIndex = queueIndex + 1;
          }

          return {
            player: {
              ...state.player,
              queueIndex: nextIndex,
              currentSong: queue[nextIndex],
              position: 0,
            },
          };
        }),

      playPrevious: () =>
        set((state) => {
          const {queue, queueIndex, position} = state.player;
          if (queue.length === 0) {
            return state;
          }

          // 如果当前位置超过 3 秒，则回到当前歌曲开头
          if (position > 3000) {
            return {
              player: {...state.player, position: 0},
            };
          }

          const prevIndex = queueIndex > 0 ? queueIndex - 1 : queue.length - 1;
          return {
            player: {
              ...state.player,
              queueIndex: prevIndex,
              currentSong: queue[prevIndex],
              position: 0,
            },
          };
        }),

      playSongAtIndex: (index) =>
        set((state) => {
          const {queue} = state.player;
          if (index < 0 || index >= queue.length) {
            return state;
          }
          return {
            player: {
              ...state.player,
              queueIndex: index,
              currentSong: queue[index],
              position: 0,
            },
          };
        }),

      // ============ 歌单 ============
      playlists: [],

      favoritesPlaylist: {
        id: 'favorites',
        name: '我喜欢的音乐',
        coverUrl: null,
        songs: [],
      },

      addPlaylist: (name) => {
        const newPlaylist: Playlist = {
          id: `playlist_${Date.now()}`,
          name,
          coverUrl: null,
          songs: [],
        };
        set((state) => ({playlists: [...state.playlists, newPlaylist]}));
        return newPlaylist;
      },

      deletePlaylist: (id) => {
        if (id === 'favorites') {
          return; // 不能删除默认收藏歌单
        }
        set((state) => ({
          playlists: state.playlists.filter((p) => p.id !== id),
        }));
      },

      renamePlaylist: (id, name) =>
        set((state) => ({
          playlists: state.playlists.map((p) =>
            p.id === id ? {...p, name} : p,
          ),
        })),

      addToPlaylist: (playlistId, song) =>
        set((state) => {
          // 检查歌曲是否已存在
          const existsInPlaylist = (playlist: Playlist) =>
            playlist.songs.some((s) => s.id === song.id);

          if (playlistId === 'favorites') {
            if (existsInPlaylist(state.favoritesPlaylist)) {
              return state;
            }
            return {
              favoritesPlaylist: {
                ...state.favoritesPlaylist,
                songs: [...state.favoritesPlaylist.songs, song],
              },
            };
          }

          return {
            playlists: state.playlists.map((p) => {
              if (p.id !== playlistId) {
                return p;
              }
              if (existsInPlaylist(p)) {
                return p;
              }
              return {...p, songs: [...p.songs, song]};
            }),
          };
        }),

      removeFromPlaylist: (playlistId, songId) =>
        set((state) => {
          if (playlistId === 'favorites') {
            return {
              favoritesPlaylist: {
                ...state.favoritesPlaylist,
                songs: state.favoritesPlaylist.songs.filter(
                  (s) => s.id !== songId,
                ),
              },
            };
          }
          return {
            playlists: state.playlists.map((p) => {
              if (p.id !== playlistId) {
                return p;
              }
              return {...p, songs: p.songs.filter((s) => s.id !== songId)};
            }),
          };
        }),

      getPlaylistById: (id) => {
        const state = get();
        if (id === 'favorites') {
          return state.favoritesPlaylist;
        }
        return state.playlists.find((p) => p.id === id);
      },

      // ============ 收藏 ============
      addToFavorites: (song) =>
        set((state) => {
          if (state.favoritesPlaylist.songs.some((s) => s.id === song.id)) {
            return state;
          }
          return {
            favoritesPlaylist: {
              ...state.favoritesPlaylist,
              songs: [...state.favoritesPlaylist.songs, song],
            },
          };
        }),

      removeFromFavorites: (songId) =>
        set((state) => ({
          favoritesPlaylist: {
            ...state.favoritesPlaylist,
            songs: state.favoritesPlaylist.songs.filter((s) => s.id !== songId),
          },
        })),

      isFavorite: (songId) => {
        return get().favoritesPlaylist.songs.some((s) => s.id === songId);
      },

      toggleFavorite: (song) => {
        const state = get();
        if (state.favoritesPlaylist.songs.some((s) => s.id === song.id)) {
          get().removeFromFavorites(song.id);
        } else {
          get().addToFavorites(song);
        }
      },

      // ============ 本地歌曲 ============
      localSongs: [],

      setLocalSongs: (songs) => set({localSongs: songs}),

      addLocalSong: (song) =>
        set((state) => {
          if (state.localSongs.some((s) => s.id === song.id)) {
            return state;
          }
          return {localSongs: [...state.localSongs, song]};
        }),

      removeLocalSong: (songId) =>
        set((state) => ({
          localSongs: state.localSongs.filter((s) => s.id !== songId),
        })),

      // ============ 搜索历史 ============
      searchHistory: [],

      addSearchHistory: (keyword) =>
        set((state) => {
          const filtered = state.searchHistory.filter(
            (k) => k !== keyword,
          );
          return {
            searchHistory: [keyword, ...filtered].slice(0, 20), // 最多保存 20 条
          };
        }),

      removeSearchHistory: (keyword) =>
        set((state) => ({
          searchHistory: state.searchHistory.filter((k) => k !== keyword),
        })),

      clearSearchHistory: () => set({searchHistory: []}),

      // ============ 设置 ============
      settings: {...defaultSettings},

      updateSettings: (newSettings) =>
        set((state) => ({
          settings: {...state.settings, ...newSettings},
        })),

      resetSettings: () => set({settings: {...defaultSettings}}),

      // ============ 播放历史 ============
      playHistory: [],

      addToPlayHistory: (song) =>
        set((state) => {
          const filtered = state.playHistory.filter((s) => s.id !== song.id);
          return {
            playHistory: [song, ...filtered].slice(0, 100), // 最多保存 100 首
          };
        }),

      clearPlayHistory: () => set({playHistory: []}),
    }),
    {
      name: 'freemusic-storage',
      storage: createJSONStorage(() => AsyncStorage),
      // 只持久化需要持久化的部分
      partialize: (state) => ({
        playlists: state.playlists,
        favoritesPlaylist: state.favoritesPlaylist,
        localSongs: state.localSongs,
        settings: state.settings,
        searchHistory: state.searchHistory,
        playHistory: state.playHistory,
        // 播放状态中的持久化部分
        player: {
          repeatMode: state.player.repeatMode,
          shuffleEnabled: state.player.shuffleEnabled,
        },
      }),
    },
  ),
);

// 辅助 hook：获取当前播放信息
export const useCurrentSong = () =>
  useMusicStore((state) => state.player.currentSong);

// 辅助 hook：获取播放状态
export const useIsPlaying = () =>
  useMusicStore((state) => state.player.isPlaying);

// 辅助 hook：获取收藏状态
export const useIsFavorite = (songId: string) =>
  useMusicStore((state) =>
    state.favoritesPlaylist.songs.some((s) => s.id === songId),
  );

// 辅助 hook：获取设置
export const useSettings = () => useMusicStore((state) => state.settings);
