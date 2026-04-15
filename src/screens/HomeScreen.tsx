import React, {useEffect, useState} from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  StatusBar,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import {NativeStackNavigationProp} from '@react-navigation/native-stack';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {RootStackParamList} from '../navigation/AppNavigator';
import {Song} from '../types';

type NavigationProp = NativeStackNavigationProp<RootStackParamList>;

export default function HomeScreen() {
  const navigation = useNavigation<NavigationProp>();
  const {player, setQueue, localSongs} = useMusicStore();
  const [recentSongs, setRecentSongs] = useState<Song[]>([]);

  useEffect(() => {
    // 获取最近播放的歌曲
    const recent = localSongs.slice(0, 10);
    setRecentSongs(recent);
  }, [localSongs]);

  const handleSongPress = (song: Song, index: number) => {
    setQueue(localSongs, index);
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
      <View style={styles.songCover}>
        <MaterialIcons name="music-note" size={24} color="#666" />
      </View>
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
      <StatusBar barStyle="light-content" backgroundColor="#1a1a1a" />

      {/* 正在播放提示 */}
      {player.currentSong && (
        <TouchableOpacity
          style={styles.nowPlayingBar}
          onPress={() => navigation.navigate('Player')}>
          <View style={styles.nowPlayingInfo}>
            <MaterialIcons name="play-circle-filled" size={40} color="#6366F1" />
            <View style={styles.nowPlayingText}>
              <Text style={styles.nowPlayingLabel}>正在播放</Text>
              <Text style={styles.nowPlayingTitle} numberOfLines={1}>
                {player.currentSong.title}
              </Text>
            </View>
          </View>
          <MaterialIcons name="chevron-right" size={24} color="#666" />
        </TouchableOpacity>
      )}

      {/* 本地音乐列表 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>本地音乐 ({localSongs.length})</Text>
        {localSongs.length === 0 ? (
          <View style={styles.emptyState}>
            <MaterialIcons name="library-music" size={64} color="#333" />
            <Text style={styles.emptyText}>暂无本地音乐</Text>
            <Text style={styles.emptyHint}>请在设置中扫描本地音乐</Text>
          </View>
        ) : (
          <FlatList
            data={localSongs}
            keyExtractor={(item) => item.id}
            renderItem={renderSongItem}
            style={styles.songList}
          />
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a1a',
  },
  nowPlayingBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#2a2a2a',
    padding: 12,
    borderRadius: 8,
    margin: 16,
  },
  nowPlayingInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  nowPlayingText: {
    marginLeft: 12,
    flex: 1,
  },
  nowPlayingLabel: {
    fontSize: 12,
    color: '#6366F1',
  },
  nowPlayingTitle: {
    fontSize: 14,
    color: '#fff',
    marginTop: 2,
  },
  section: {
    flex: 1,
    paddingHorizontal: 16,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 12,
  },
  songList: {
    flex: 1,
  },
  songItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#333',
  },
  songCover: {
    width: 48,
    height: 48,
    backgroundColor: '#333',
    borderRadius: 4,
    justifyContent: 'center',
    alignItems: 'center',
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
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 16,
    color: '#666',
    marginTop: 16,
  },
  emptyHint: {
    fontSize: 12,
    color: '#444',
    marginTop: 8,
  },
});
