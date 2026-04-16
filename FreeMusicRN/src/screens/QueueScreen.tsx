/**
 * 播放队列页面
 */

import React, {useCallback} from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  StatusBar,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {SongItem, MiniPlayer} from '../components';
import {playSong} from '../services/playerService';
import {getPlayUrl} from '../api/netease';
import {Song} from '../types';

export default function QueueScreen(): React.JSX.Element {
  const navigation = useNavigation();
  const {player, setQueue, playSongAtIndex, clearQueue, removeFromQueue} =
    useMusicStore();

  const {queue, currentSong, queueIndex} = player;

  // 处理歌曲点击
  const handleSongPress = useCallback(
    async (song: Song, index: number) => {
      try {
        if (song.isNetease && song.neteaseId) {
          const url = await getPlayUrl(song.neteaseId);
          if (url) {
            await playSong(song, url);
            playSongAtIndex(index);
          }
        } else if (song.url) {
          await playSong(song, song.url);
          playSongAtIndex(index);
        }
      } catch (error) {
        console.error('Play song error:', error);
      }
    },
    [playSongAtIndex],
  );

  // 处理移除歌曲
  const handleRemoveSong = useCallback(
    (songId: string) => {
      removeFromQueue(songId);
    },
    [removeFromQueue],
  );

  // 清空队列
  const handleClearQueue = useCallback(() => {
    clearQueue();
  }, [clearQueue]);

  // 渲染队列项
  const renderQueueItem = ({item, index}: {item: Song; index: number}) => (
    <SongItem
      song={item}
      index={index}
      onPress={handleSongPress}
      onMorePress={handleRemoveSong}
      showIndex={false}
    />
  );

  // 渲染头部
  const renderHeader = () => (
    <View style={styles.header}>
      <View style={styles.headerInfo}>
        <Text style={styles.headerTitle}>播放队列</Text>
        <Text style={styles.headerSubtitle}>
          {queue.length} 首歌曲
        </Text>
      </View>
      {queue.length > 0 && (
        <TouchableOpacity style={styles.clearButton} onPress={handleClearQueue}>
          <MaterialIcons name="delete-sweep" size={20} color="#666" />
          <Text style={styles.clearText}>清空</Text>
        </TouchableOpacity>
      )}
    </View>
  );

  // 渲染当前播放提示
  const renderCurrentSong = () => {
    if (!currentSong) {
      return null;
    }

    return (
      <View style={styles.currentSongContainer}>
        <View style={styles.currentSongHeader}>
          <MaterialIcons name="equalizer" size={16} color="#6366F1" />
          <Text style={styles.currentSongLabel}>正在播放</Text>
        </View>
        <View style={styles.currentSongInfo}>
          <Text style={styles.currentSongTitle} numberOfLines={1}>
            {currentSong.title}
          </Text>
          <Text style={styles.currentSongArtist} numberOfLines={1}>
            {currentSong.artist}
          </Text>
        </View>
      </View>
    );
  };

  // 渲染空状态
  const renderEmpty = () => (
    <View style={styles.emptyContainer}>
      <MaterialIcons name="queue-music" size={64} color="#333" />
      <Text style={styles.emptyText}>播放队列为空</Text>
      <Text style={styles.emptyHint}>去搜索歌曲添加到队列吧</Text>
    </View>
  );

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" />

      <FlatList
        data={queue}
        keyExtractor={item => item.id}
        renderItem={renderQueueItem}
        ListHeaderComponent={
          <>
            {renderHeader()}
            {renderCurrentSong()}
          </>
        }
        ListEmptyComponent={renderEmpty}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
      />

      <MiniPlayer />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a1a',
  },
  listContent: {
    paddingBottom: 100,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 16,
  },
  headerInfo: {},
  headerTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#fff',
  },
  headerSubtitle: {
    fontSize: 13,
    color: '#888',
    marginTop: 4,
  },
  clearButton: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 8,
  },
  clearText: {
    fontSize: 13,
    color: '#666',
    marginLeft: 4,
  },
  currentSongContainer: {
    backgroundColor: '#252525',
    marginHorizontal: 20,
    borderRadius: 8,
    padding: 12,
    marginBottom: 16,
  },
  currentSongHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  currentSongLabel: {
    fontSize: 12,
    color: '#6366F1',
    marginLeft: 6,
  },
  currentSongInfo: {},
  currentSongTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: '#fff',
  },
  currentSongArtist: {
    fontSize: 13,
    color: '#888',
    marginTop: 2,
  },
  emptyContainer: {
    alignItems: 'center',
    paddingVertical: 60,
  },
  emptyText: {
    fontSize: 16,
    color: '#666',
    marginTop: 16,
  },
  emptyHint: {
    fontSize: 13,
    color: '#444',
    marginTop: 4,
  },
});
