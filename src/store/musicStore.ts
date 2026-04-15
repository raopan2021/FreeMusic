import {create} from 'zustand';
import {persist, createJSONStorage} from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {Song, Playlist, PlayerState, RepeatMode, Settings} from '../types';

interface MusicStore {
  // 播放状态
  player: PlayerState;
  setCurrentSong: (song: Song | null) => void;
  setIsPlaying: (isPlaying: boolean) => void;
  setPosition: (position: number) => void;
  setDuration: (duration: number) => void;
  setQueue: (queue: Song[], index?: number) => void;
  setRepeatMode: (mode: RepeatMode) => void;
  toggleShuffle: () => void;
  playNext: () => void;
  playPrevious: () => void;

  // 歌单
  playlists: Playlist[];
  favoritesPlaylist: Playlist;
  addPlaylist: (name: string) => void;
  deletePlaylist: (id: string) => void;
  addToPlaylist: (playlistId: string, song: Song) => void;
  removeFromPlaylist: (playlistId: string, songId: string) => void;

  // 收藏
  addToFavorites: (song: Song) => void;
  removeFromFavorites: (songId: string) => void;
  isFavorite: (songId: string) => boolean;

  // 本地歌曲
  localSongs: Song[];
  setLocalSongs: (songs: Song[]) => void;

  // 设置
  settings: Settings;
  updateSettings: (settings: Partial<Settings>) => void;
}

export const useMusicStore = create<MusicStore>()(
  persist(
    (set, get) => ({
      // 播放状态
      player: {
        currentSong: null,
        isPlaying: false,
        position: 0,
        duration: 0,
        queue: [],
        queueIndex: 0,
        repeatMode: 'off',
        shuffleEnabled: false,
      },
      setCurrentSong: (song) =>
        set((state) => ({player: {...state.player, currentSong: song}})),
      setIsPlaying: (isPlaying) =>
        set((state) => ({player: {...state.player, isPlaying}})),
      setPosition: (position) =>
        set((state) => ({player: {...state.player, position}})),
      setDuration: (duration) =>
        set((state) => ({player: {...state.player, duration}})),
      setQueue: (queue, index = 0) =>
        set((state) => ({
          player: {...state.player, queue, queueIndex: index, currentSong: queue[index]},
        })),
      setRepeatMode: (mode) =>
        set((state) => ({player: {...state.player, repeatMode: mode}})),
      toggleShuffle: () =>
        set((state) => ({
          player: {...state.player, shuffleEnabled: !state.player.shuffleEnabled},
        })),
      playNext: () => {
        const {player} = get();
        const {queue, queueIndex, repeatMode, shuffleEnabled} = player;
        if (queue.length === 0) return;

        let nextIndex: number;
        if (shuffleEnabled) {
          nextIndex = Math.floor(Math.random() * queue.length);
        } else if (queueIndex >= queue.length - 1) {
          nextIndex = repeatMode === 'all' ? 0 : queueIndex;
        } else {
          nextIndex = queueIndex + 1;
        }

        set({
          player: {
            ...player,
            queueIndex: nextIndex,
            currentSong: queue[nextIndex],
            position: 0,
          },
        });
      },
      playPrevious: () => {
        const {player} = get();
        const {queue, queueIndex, position} = player;
        if (queue.length === 0) return;

        // 如果当前位置超过3秒，则回到当前歌曲开头
        if (position > 3000) {
          set({player: {...player, position: 0}});
          return;
        }

        const prevIndex = queueIndex > 0 ? queueIndex - 1 : queue.length - 1;
        set({
          player: {
            ...player,
            queueIndex: prevIndex,
            currentSong: queue[prevIndex],
            position: 0,
          },
        });
      },

      // 歌单
      playlists: [],
      favoritesPlaylist: {
        id: 'favorites',
        name: '我喜欢的音乐',
        coverUrl: null,
        songs: [],
      },
      addPlaylist: (name) => {
        const newPlaylist: Playlist = {
          id: `local_${Date.now()}`,
          name,
          coverUrl: null,
          songs: [],
        };
        set((state) => ({playlists: [...state.playlists, newPlaylist]}));
      },
      deletePlaylist: (id) => {
        if (id === 'favorites') return;
        set((state) => ({
          playlists: state.playlists.filter((p) => p.id !== id),
        }));
      },
      addToPlaylist: (playlistId, song) => {
        set((state) => {
          if (playlistId === 'favorites') {
            const exists = state.favoritesPlaylist.songs.some((s) => s.id === song.id);
            if (exists) return state;
            return {
              favoritesPlaylist: {
                ...state.favoritesPlaylist,
                songs: [...state.favoritesPlaylist.songs, song],
              },
            };
          }
          return {
            playlists: state.playlists.map((p) => {
              if (p.id !== playlistId) return p;
              const exists = p.songs.some((s) => s.id === song.id);
              if (exists) return p;
              return {...p, songs: [...p.songs, song]};
            }),
          };
        });
      },
      removeFromPlaylist: (playlistId, songId) => {
        set((state) => {
          if (playlistId === 'favorites') {
            return {
              favoritesPlaylist: {
                ...state.favoritesPlaylist,
                songs: state.favoritesPlaylist.songs.filter((s) => s.id !== songId),
              },
            };
          }
          return {
            playlists: state.playlists.map((p) => {
              if (p.id !== playlistId) return p;
              return {...p, songs: p.songs.filter((s) => s.id !== songId)};
            }),
          };
        });
      },

      // 收藏
      addToFavorites: (song) => {
        set((state) => {
          const exists = state.favoritesPlaylist.songs.some((s) => s.id === song.id);
          if (exists) return state;
          return {
            favoritesPlaylist: {
              ...state.favoritesPlaylist,
              songs: [...state.favoritesPlaylist.songs, song],
            },
          };
        });
      },
      removeFromFavorites: (songId) => {
        set((state) => ({
          favoritesPlaylist: {
            ...state.favoritesPlaylist,
            songs: state.favoritesPlaylist.songs.filter((s) => s.id !== songId),
          },
        }));
      },
      isFavorite: (songId) => {
        return get().favoritesPlaylist.songs.some((s) => s.id === songId);
      },

      // 本地歌曲
      localSongs: [],
      setLocalSongs: (songs) => set({localSongs: songs}),

      // 设置
      settings: {
        theme: 'dark',
        primaryColor: '#6366F1',
        lyricsFontSize: 16,
        autoPlay: true,
        playbackSpeed: 1.0,
        skipSilence: false,
      },
      updateSettings: (newSettings) => {
        set((state) => ({
          settings: {...state.settings, ...newSettings},
        }));
      },
    }),
    {
      name: 'music-storage',
      storage: createJSONStorage(() => AsyncStorage),
      partialize: (state) => ({
        playlists: state.playlists,
        favoritesPlaylist: state.favoritesPlaylist,
        localSongs: state.localSongs,
        settings: state.settings,
        player: {
          repeatMode: state.player.repeatMode,
          shuffleEnabled: state.player.shuffleEnabled,
        },
      }),
    },
  ),
);
