/**
 * 歌曲列表项组件
 */

import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
} from 'react-native';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {Song} from '../types';
import {useMusicStore} from '../store/musicStore';

interface SongItemProps {
  song: Song;
  index?: number;
  onPress: (song: Song, index: number) => void;
  onMorePress?: (song: Song) => void;
  showIndex?: boolean;
  showCover?: boolean;
}

export default function SongItem({
  song,
  index,
  onPress,
  onMorePress,
  showIndex = false,
  showCover = true,
}: SongItemProps): React.JSX.Element {
  const isPlaying = useMusicStore(
    state =>
      state.player.currentSong?.id === song.id && state.player.isPlaying,
  );
  const isFavorite = useMusicStore(state =>
    state.favoritesPlaylist.songs.some(s => s.id === song.id),
  );

  const formatDuration = (ms: number) => {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  return (
    <TouchableOpacity
      style={[styles.container, isPlaying && styles.playing]}
      onPress={() => onPress(song, index ?? 0)}
      activeOpacity={0.7}>
      {showIndex && (
        <View style={styles.indexContainer}>
          {isPlaying ? (
            <MaterialIcons name="equalizer" size={20} color="#6366F1" />
          ) : (
            <Text style={styles.indexText}>{index! + 1}</Text>
          )}
        </View>
      )}

      {showCover ? (
        <View style={styles.coverContainer}>
          {song.coverUrl ? (
            <Image source={{uri: song.coverUrl}} style={styles.cover} />
          ) : (
            <View style={styles.coverPlaceholder}>
              <MaterialIcons name="music-note" size={24} color="#666" />
            </View>
          )}
          {isPlaying && (
            <View style={styles.playingOverlay}>
              <MaterialIcons name="play-arrow" size={16} color="#fff" />
            </View>
          )}
        </View>
      ) : isPlaying ? (
        <View style={styles.playingIndicator}>
          <MaterialIcons name="equalizer" size={20} color="#6366F1" />
        </View>
      ) : null}

      <View style={styles.info}>
        <Text
          style={[styles.title, isPlaying && styles.playingText]}
          numberOfLines={1}>
          {song.title}
        </Text>
        <View style={styles.subInfo}>
          {isFavorite && (
            <MaterialIcons name="favorite" size={12} color="#6366F1" style={styles.favoriteIcon} />
          )}
          <Text style={styles.artist} numberOfLines={1}>
            {song.artist}
          </Text>
          {song.album && (
            <Text style={styles.album} numberOfLines={1}>
              {' '}• {song.album}
            </Text>
          )}
        </View>
      </View>

      <View style={styles.right}>
        {song.duration > 0 && (
          <Text style={styles.duration}>{formatDuration(song.duration)}</Text>
        )}
        {onMorePress && (
          <TouchableOpacity
            style={styles.moreButton}
            onPress={() => onMorePress(song)}
            hitSlop={{top: 10, bottom: 10, left: 10, right: 10}}>
            <MaterialIcons name="more-vert" size={20} color="#666" />
          </TouchableOpacity>
        )}
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    paddingHorizontal: 16,
    backgroundColor: '#1a1a1a',
  },
  playing: {
    backgroundColor: '#252525',
  },
  indexContainer: {
    width: 32,
    alignItems: 'center',
    marginRight: 8,
  },
  indexText: {
    fontSize: 14,
    color: '#666',
  },
  coverContainer: {
    position: 'relative',
    marginRight: 12,
  },
  cover: {
    width: 48,
    height: 48,
    borderRadius: 4,
    backgroundColor: '#333',
  },
  coverPlaceholder: {
    width: 48,
    height: 48,
    borderRadius: 4,
    backgroundColor: '#2a2a2a',
    justifyContent: 'center',
    alignItems: 'center',
  },
  playingOverlay: {
    position: 'absolute',
    bottom: 2,
    right: 2,
    backgroundColor: 'rgba(99, 102, 241, 0.8)',
    borderRadius: 10,
    width: 20,
    height: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  playingIndicator: {
    width: 32,
    marginRight: 8,
    alignItems: 'center',
  },
  info: {
    flex: 1,
    justifyContent: 'center',
  },
  title: {
    fontSize: 15,
    color: '#fff',
    fontWeight: '500',
  },
  playingText: {
    color: '#6366F1',
  },
  subInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 4,
  },
  favoriteIcon: {
    marginRight: 4,
  },
  artist: {
    fontSize: 12,
    color: '#888',
  },
  album: {
    fontSize: 12,
    color: '#666',
  },
  right: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  duration: {
    fontSize: 12,
    color: '#666',
    marginRight: 8,
  },
  moreButton: {
    padding: 4,
  },
});
