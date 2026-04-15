import React, {useState} from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  FlatList,
  TouchableOpacity,
} from 'react-native';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {Song} from '../types';

export default function SearchScreen() {
  const [searchQuery, setSearchQuery] = useState('');
  const {localSongs, setQueue} = useMusicStore();

  const filteredSongs = searchQuery
    ? localSongs.filter(
        (song) =>
          song.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
          song.artist.toLowerCase().includes(searchQuery.toLowerCase()) ||
          song.album.toLowerCase().includes(searchQuery.toLowerCase()),
      )
    : [];

  const handleSongPress = (song: Song, index: number) => {
    setQueue(filteredSongs, index);
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
      <MaterialIcons name="music-note" size={24} color="#666" />
      <View style={styles.songInfo}>
        <Text style={styles.songTitle} numberOfLines={1}>
          {item.title}
        </Text>
        <Text style={styles.songArtist} numberOfLines={1}>
          {item.artist} • {item.album}
        </Text>
      </View>
      <Text style={styles.songDuration}>{formatDuration(item.duration)}</Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      {/* 搜索框 */}
      <View style={styles.searchContainer}>
        <MaterialIcons name="search" size={20} color="#666" />
        <TextInput
          style={styles.searchInput}
          placeholder="搜索音乐、歌手、专辑..."
          placeholderTextColor="#666"
          value={searchQuery}
          onChangeText={setSearchQuery}
        />
        {searchQuery.length > 0 && (
          <TouchableOpacity onPress={() => setSearchQuery('')}>
            <MaterialIcons name="close" size={20} color="#666" />
          </TouchableOpacity>
        )}
      </View>

      {/* 搜索结果 */}
      {searchQuery.length > 0 ? (
        <FlatList
          data={filteredSongs}
          keyExtractor={(item) => item.id}
          renderItem={renderSongItem}
          style={styles.resultsList}
          ListEmptyComponent={
            <View style={styles.emptyState}>
              <MaterialIcons name="search-off" size={64} color="#333" />
              <Text style={styles.emptyText}>未找到相关音乐</Text>
            </View>
          }
        />
      ) : (
        <View style={styles.hintState}>
          <MaterialIcons name="search" size={64} color="#333" />
          <Text style={styles.hintText}>输入关键词搜索音乐</Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a1a',
    padding: 16,
  },
  searchContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#2a2a2a',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginBottom: 16,
  },
  searchInput: {
    flex: 1,
    fontSize: 14,
    color: '#fff',
    marginLeft: 8,
  },
  resultsList: {
    flex: 1,
  },
  songItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#333',
  },
  songInfo: {
    flex: 1,
    marginLeft: 12,
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
  songDuration: {
    fontSize: 12,
    color: '#666',
  },
  emptyState: {
    alignItems: 'center',
    marginTop: 60,
  },
  emptyText: {
    fontSize: 14,
    color: '#666',
    marginTop: 16,
  },
  hintState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  hintText: {
    fontSize: 14,
    color: '#666',
    marginTop: 16,
  },
});
