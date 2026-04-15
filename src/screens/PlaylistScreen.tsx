import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
} from 'react-native';
import {RouteProp, useRoute} from '@react-navigation/native';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {RootStackParamList} from '../navigation/AppNavigator';
import {Song} from '../types';

type PlaylistRouteProp = RouteProp<RootStackParamList, 'Playlist'>;

export default function PlaylistScreen() {
  const route = useRoute<PlaylistRouteProp>();
  const {playlistId} = route.params;
  const {playlists, favoritesPlaylist, setQueue, removeFromPlaylist} =
    useMusicStore();

  const playlist =
    playlistId === 'favorites'
      ? favoritesPlaylist
      : playlists.find((p) => p.id === playlistId);

  if (!playlist) {
    return (
      <View style={styles.container}>
        <Text style={styles.errorText}>歌单不存在</Text>
      </View>
    );
  }

  const handlePlayAll = () => {
    if (playlist.songs.length > 0) {
      setQueue(playlist.songs, 0);
    }
  };

  const handleSongPress = (song: Song, index: number) => {
    setQueue(playlist.songs, index);
  };

  const handleRemove = (songId: string) => {
    removeFromPlaylist(playlistId, songId);
  };

  const formatDuration = (ms: number) => {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  const renderSongItem = ({item, index}: {item: Song; index: number}) => (
    <TouchableOpacity
      style={styles.songItem}
      onPress={() => handleSongPress(item, index)}>
      <View style={styles.songIndex}>
        <Text style={styles.indexText}>{index + 1}</Text>
      </View>
      <View style={styles.songInfo}>
        <Text style={styles.songTitle} numberOfLines={1}>
          {item.title}
        </Text>
        <Text style={styles.songArtist} numberOfLines={1}>
          {item.artist} • {item.album}
        </Text>
      </View>
      <TouchableOpacity
        style={styles.moreButton}
        onPress={() => handleRemove(item.id)}>
        <MaterialIcons name="more-vert" size={20} color="#666" />
      </TouchableOpacity>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      {/* 歌单信息 */}
      <View style={styles.header}>
        <View style={styles.cover}>
          <MaterialIcons name="library-music" size={64} color="#333" />
        </View>
        <View style={styles.headerInfo}>
          <Text style={styles.playlistName}>{playlist.name}</Text>
          <Text style={styles.songCount}>{playlist.songs.length} 首歌曲</Text>
        </View>
      </View>

      {/* 操作按钮 */}
      <View style={styles.actions}>
        <TouchableOpacity style={styles.playAllButton} onPress={handlePlayAll}>
          <MaterialIcons name="play-circle-filled" size={32} color="#6366F1" />
          <Text style={styles.playAllText}>播放全部</Text>
        </TouchableOpacity>
      </View>

      {/* 歌曲列表 */}
      {playlist.songs.length === 0 ? (
        <View style={styles.emptyState}>
          <MaterialIcons name="music-off" size={64} color="#333" />
          <Text style={styles.emptyText}>歌单为空</Text>
        </View>
      ) : (
        <FlatList
          data={playlist.songs}
          keyExtractor={(item) => item.id}
          renderItem={renderSongItem}
          style={styles.songList}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a1a',
  },
  header: {
    flexDirection: 'row',
    padding: 16,
    alignItems: 'center',
  },
  cover: {
    width: 120,
    height: 120,
    backgroundColor: '#2a2a2a',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
  headerInfo: {
    flex: 1,
    marginLeft: 16,
  },
  playlistName: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#fff',
  },
  songCount: {
    fontSize: 14,
    color: '#888',
    marginTop: 8,
  },
  actions: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    paddingBottom: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#333',
  },
  playAllButton: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 8,
  },
  playAllText: {
    fontSize: 14,
    color: '#6366F1',
    marginLeft: 8,
  },
  songList: {
    flex: 1,
  },
  songItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#333',
  },
  songIndex: {
    width: 32,
    alignItems: 'center',
  },
  indexText: {
    fontSize: 14,
    color: '#666',
  },
  songInfo: {
    flex: 1,
    marginLeft: 8,
  },
  songTitle: {
    fontSize: 14,
    color: '#fff',
  },
  songArtist: {
    fontSize: 12,
    color: '#888',
    marginTop: 4,
  },
  moreButton: {
    padding: 4,
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 14,
    color: '#666',
    marginTop: 16,
  },
  errorText: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    marginTop: 100,
  },
});
