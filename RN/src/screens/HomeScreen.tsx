/**
 * 首页
 */

import React, {useEffect, useCallback} from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  StatusBar,
  RefreshControl,
} from 'react-native';
import {useNavigation, useFocusEffect} from '@react-navigation/native';
import {NativeStackNavigationProp} from '@react-navigation/native-stack';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {MiniPlayer, SongItem} from '../components';
import {playSong} from '../services/playerService';
import {searchSongs, getPlayUrl} from '../api/netease';
import {Song} from '../types';
import {RootStackParamList} from '../navigation/AppNavigator';

type NavigationProp = NativeStackNavigationProp<RootStackParamList>;

export default function HomeScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const {
    player,
    setQueue,
    playHistory,
    favoritesPlaylist,
    playSongAtIndex,
  } = useMusicStore();

  const [refreshing, setRefreshing] = React.useState(false);

  // 加载最近播放的歌曲
  const loadRecentSongs = useCallback(() => {
    // 使用播放历史作为最近播放
    return playHistory.slice(0, 20);
  }, [playHistory]);

  // 下拉刷新
  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    // 模拟刷新
    await new Promise(resolve => setTimeout(resolve, 1000));
    setRefreshing(false);
  }, []);

  // 处理歌曲点击
  const handleSongPress = useCallback(
    async (song: Song, index: number) => {
      try {
        // 如果是网易云歌曲，获取播放 URL
        if (song.isNetease && song.neteaseId) {
          const url = await getPlayUrl(song.neteaseId);
          if (url) {
            await playSong(song, url);
            return;
          }
        }
        // 否则直接播放（本地歌曲应该有 url）
        if (song.url) {
          await playSong(song, song.url);
        }
      } catch (error) {
        console.error('Play song error:', error);
      }
    },
    [],
  );

  // 处理更多按钮点击
  const handleMorePress = useCallback((song: Song) => {
    // TODO: 显示操作菜单
    console.log('More press:', song.title);
  }, []);

  // 渲染歌曲项
  const renderSongItem = useCallback(
    ({item, index}: {item: Song; index: number}) => (
      <SongItem
        song={item}
        index={index}
        onPress={handleSongPress}
        onMorePress={handleMorePress}
        showIndex
      />
    ),
    [handleSongPress, handleMorePress],
  );

  // 渲染头部
  const renderHeader = () => (
    <View style={styles.header}>
      <Text style={styles.greeting}>晚上好</Text>
      <Text style={styles.subtitle}>今天想听什么音乐？</Text>
    </View>
  );

  // 渲染快速入口
  const renderQuickAccess = () => (
    <View style={styles.quickAccess}>
      <TouchableOpacity
        style={styles.quickItem}
        onPress={() => navigation.navigate('Search')}>
        <View style={[styles.quickIcon, {backgroundColor: '#6366F1'}]}>
          <MaterialIcons name="search" size={24} color="#fff" />
        </View>
        <Text style={styles.quickText}>搜索</Text>
      </TouchableOpacity>

      <TouchableOpacity
        style={styles.quickItem}
        onPress={() => navigation.navigate('Library')}>
        <View style={[styles.quickIcon, {backgroundColor: '#EC4899'}]}>
          <MaterialIcons name="favorite" size={24} color="#fff" />
        </View>
        <Text style={styles.quickText}>收藏</Text>
      </TouchableOpacity>

      <TouchableOpacity
        style={styles.quickItem}
        onPress={() => navigation.navigate('Queue')}>
        <View style={[styles.quickIcon, {backgroundColor: '#10B981'}]}>
          <MaterialIcons name="queue-music" size={24} color="#fff" />
        </View>
        <Text style={styles.quickText}>播放队列</Text>
      </TouchableOpacity>
    </View>
  );

  // 渲染最近播放
  const renderRecentSection = () => {
    const recentSongs = loadRecentSongs();

    if (recentSongs.length === 0) {
      return (
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>最近播放</Text>
          <View style={styles.emptyState}>
            <MaterialIcons name="history" size={48} color="#333" />
            <Text style={styles.emptyText}>暂无播放记录</Text>
            <Text style={styles.emptyHint}>去搜索歌曲开始播放吧</Text>
          </View>
        </View>
      );
    }

    return (
      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>最近播放</Text>
          <TouchableOpacity onPress={() => navigation.navigate('Queue')}>
            <Text style={styles.seeAll}>查看全部</Text>
          </TouchableOpacity>
        </View>
        <FlatList
          data={recentSongs}
          keyExtractor={item => item.id}
          renderItem={renderSongItem}
          scrollEnabled={false}
        />
      </View>
    );
  };

  // 渲染收藏入口
  const renderFavoritesSection = () => {
    const songs = favoritesPlaylist.songs;
    if (songs.length === 0) {
      return null;
    }

    return (
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>我喜欢的音乐 ({songs.length})</Text>
        <TouchableOpacity
          style={styles.favoritesCard}
          onPress={() => navigation.navigate('Playlist', {playlistId: 'favorites'})}>
          <View style={styles.favoritesCover}>
            <MaterialIcons name="favorite" size={40} color="#6366F1" />
          </View>
          <View style={styles.favoritesInfo}>
            <Text style={styles.favoritesTitle}>我喜欢的音乐</Text>
            <Text style={styles.favoritesSubtitle}>{songs.length} 首歌曲</Text>
          </View>
          <MaterialIcons name="chevron-right" size={24} color="#666" />
        </TouchableOpacity>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a1a" />

      <FlatList
        data={[]}
        ListHeaderComponent={
          <>
            {renderHeader()}
            {renderQuickAccess()}
            {renderFavoritesSection()}
            {renderRecentSection()}
          </>
        }
        renderItem={null}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor="#6366F1"
            colors={['#6366F1']}
          />
        }
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
      />

      {/* 迷你播放器 */}
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
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 16,
  },
  greeting: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
  },
  subtitle: {
    fontSize: 14,
    color: '#888',
    marginTop: 4,
  },
  quickAccess: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    paddingVertical: 16,
    gap: 12,
  },
  quickItem: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#252525',
    borderRadius: 8,
    padding: 12,
  },
  quickIcon: {
    width: 40,
    height: 40,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 10,
  },
  quickText: {
    fontSize: 13,
    color: '#fff',
    fontWeight: '500',
  },
  section: {
    marginTop: 24,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#fff',
    paddingHorizontal: 20,
    marginBottom: 12,
  },
  seeAll: {
    fontSize: 13,
    color: '#6366F1',
  },
  emptyState: {
    alignItems: 'center',
    paddingVertical: 40,
  },
  emptyText: {
    fontSize: 14,
    color: '#666',
    marginTop: 12,
  },
  emptyHint: {
    fontSize: 12,
    color: '#444',
    marginTop: 4,
  },
  favoritesCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#252525',
    marginHorizontal: 20,
    borderRadius: 8,
    padding: 12,
  },
  favoritesCover: {
    width: 56,
    height: 56,
    backgroundColor: '#1a1a1a',
    borderRadius: 4,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  favoritesInfo: {
    flex: 1,
  },
  favoritesTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: '#fff',
  },
  favoritesSubtitle: {
    fontSize: 12,
    color: '#888',
    marginTop: 4,
  },
});
