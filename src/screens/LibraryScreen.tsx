import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import {NativeStackNavigationProp} from '@react-navigation/native-stack';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {RootStackParamList} from '../navigation/AppNavigator';
import {Playlist as PlaylistType} from '../types';

type NavigationProp = NativeStackNavigationProp<RootStackParamList>;

export default function LibraryScreen() {
  const navigation = useNavigation<NavigationProp>();
  const {playlists, favoritesPlaylist, setQueue} = useMusicStore();

  const allPlaylists = [favoritesPlaylist, ...playlists];

  const handlePlaylistPress = (playlist: PlaylistType) => {
    navigation.navigate('Playlist', {playlistId: playlist.id});
  };

  const handlePlayAll = (playlist: PlaylistType) => {
    if (playlist.songs.length > 0) {
      setQueue(playlist.songs, 0);
    }
  };

  const renderPlaylistItem = ({item}: {item: PlaylistType}) => (
    <TouchableOpacity
      style={styles.playlistItem}
      onPress={() => handlePlaylistPress(item)}>
      <View style={styles.playlistCover}>
        <MaterialIcons name="library-music" size={32} color="#666" />
      </View>
      <View style={styles.playlistInfo}>
        <Text style={styles.playlistName}>{item.name}</Text>
        <Text style={styles.playlistCount}>{item.songs.length} 首歌曲</Text>
      </View>
      <TouchableOpacity
        style={styles.playButton}
        onPress={() => handlePlayAll(item)}>
        <MaterialIcons name="play-circle" size={36} color="#6366F1" />
      </TouchableOpacity>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <Text style={styles.title}>我的音乐库</Text>

      {/* 创建歌单按钮 */}
      <TouchableOpacity style={styles.createButton}>
        <MaterialIcons name="add" size={24} color="#6366F1" />
        <Text style={styles.createText}>创建歌单</Text>
      </TouchableOpacity>

      {/* 收藏 */}
      <TouchableOpacity
        style={styles.favoritesItem}
        onPress={() => handlePlaylistPress(favoritesPlaylist)}>
        <View style={[styles.playlistCover, styles.favoritesCover]}>
          <MaterialIcons name="favorite" size={32} color="#e91e63" />
        </View>
        <View style={styles.playlistInfo}>
          <Text style={styles.playlistName}>{favoritesPlaylist.name}</Text>
          <Text style={styles.playlistCount}>
            {favoritesPlaylist.songs.length} 首歌曲
          </Text>
        </View>
      </TouchableOpacity>

      {/* 歌单列表 */}
      <FlatList
        data={playlists}
        keyExtractor={(item) => item.id}
        renderItem={renderPlaylistItem}
        style={styles.list}
        contentContainerStyle={styles.listContent}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a1a',
    padding: 16,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 20,
  },
  createButton: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#2a2a2a',
    borderRadius: 8,
    marginBottom: 16,
  },
  createText: {
    fontSize: 14,
    color: '#6366F1',
    marginLeft: 12,
  },
  favoritesItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    backgroundColor: '#2a2a2a',
    borderRadius: 8,
    marginBottom: 16,
  },
  playlistItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    backgroundColor: '#2a2a2a',
    borderRadius: 8,
    marginBottom: 8,
  },
  playlistCover: {
    width: 56,
    height: 56,
    backgroundColor: '#333',
    borderRadius: 4,
    justifyContent: 'center',
    alignItems: 'center',
  },
  favoritesCover: {
    backgroundColor: '#2a2a2a',
  },
  playlistInfo: {
    flex: 1,
    marginLeft: 12,
  },
  playlistName: {
    fontSize: 14,
    color: '#fff',
    fontWeight: '600',
  },
  playlistCount: {
    fontSize: 12,
    color: '#888',
    marginTop: 4,
  },
  playButton: {
    padding: 4,
  },
  list: {
    flex: 1,
  },
  listContent: {
    paddingBottom: 100,
  },
});
