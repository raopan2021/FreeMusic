/**
 * 歌单详情页面
 */

import React, {useCallback, useMemo} from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  StatusBar,
  Image,
} from 'react-native';
import {useNavigation, useRoute, RouteProp} from '@react-navigation/native';
import {NativeStackNavigationProp} from '@react-navigation/native-stack';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {MiniPlayer, SongItem} from '../components';
import {playSong} from '../services/playerService';
import {getPlayUrl} from '../api/netease';
import {Song} from '../types';
import {RootStackParamList} from '../navigation/AppNavigator';

type PlaylistDetailRouteProp = RouteProp<RootStackParamList, 'PlaylistDetail'>;
type NavigationProp = NativeStackNavigationProp<RootStackParamList>;

export default function PlaylistDetailScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const route = useRoute<PlaylistDetailRouteProp>();
  const {playlistId} = route.params;

  const {
    getPlaylistById,
    favoritesPlaylist,
    setQueue,
    removeFromPlaylist,
    playHistory,
  } = useMusicStore();

  // 获取歌单
  const playlist = useMemo(() => {
    if (playlistId === 'favorites') {
      return favoritesPlaylist;
    }
    return getPlaylistById(playlistId);
  }, [playlistId, favoritesPlaylist, getPlaylistById]);

  // 播放全部
  const handlePlayAll = useCallback(async () => {
    if (!playlist || playlist.songs.length === 0) {
      return;
    }

    try {
      // 获取所有歌曲的播放 URL
      const songsWithUrls = await Promise.all(
        playlist.songs.map(async song => {
          if (song.isNetease && song.neteaseId) {
            const url = await getPlayUrl(song.neteaseId);
            return {...song, url: url || undefined};
          }
          return song;
        }),
      );

      // 设置队列并播放第一首
      setQueue(songsWithUrls, 0);
      const firstSong = songsWithUrls[0];
      if (firstSong.url) {
        await playSong(firstSong, firstSong.url);
      }
    } catch (error) {
      console.error('Play all error:', error);
    }
  }, [playlist, setQueue]);

  // 处理歌曲点击
  const handleSongPress = useCallback(
    async (song: Song, index: number) => {
      try {
        if (song.isNetease && song.neteaseId) {
          const url = await getPlayUrl(song.neteaseId);
          if (url) {
            const songWithUrl = {...song, url};
            setQueue(
              playlist?.songs.map((s, i) =>
                i === index ? songWithUrl : {...s, url: s.url},
              ) || [],
              index,
            );
            await playSong(songWithUrl, url);
          }
        }
      } catch (error) {
        console.error('Play song error:', error);
      }
    },
    [playlist, setQueue],
  );

  // 处理更多按钮
  const handleMorePress = useCallback(
    (song: Song) => {
      // TODO: 显示操作菜单
      console.log('More press:', song.title);
    },
    [],
  );

  // 渲染头部
  const renderHeader = () => {
    if (!playlist) {
      return null;
    }

    return (
      <View style={styles.header}>
        {/* 封面 */}
        <View style={styles.coverContainer}>
          {playlist.coverUrl ? (
            <Image
              source={{uri: playlist.coverUrl}}
              style={styles.cover}
              resizeMode="cover"
            />
          ) : (
            <View style={styles.coverPlaceholder}>
              <MaterialIcons
                name={playlistId === 'favorites' ? 'favorite' : 'queue-music'}
                size={64}
                color={playlistId === 'favorites' ? '#fff' : '#6366F1'}
              />
            </View>
          )}
        </View>

        {/* 歌单信息 */}
        <Text style={styles.playlistName}>{playlist.name}</Text>
        <Text style={styles.playlistInfo}>
          {playlist.songs.length} 首歌曲
        </Text>

        {/* 操作按钮 */}
        <View style={styles.actions}>
          <TouchableOpacity style={styles.playAllButton} onPress={handlePlayAll}>
            <MaterialIcons name="play-arrow" size={24} color="#fff" />
            <Text style={styles.playAllText}>播放全部</Text>
          </TouchableOpacity>

          {playlistId !== 'favorites' && (
            <TouchableOpacity
              style={styles.actionButton}
              onPress={() => {
                // TODO: 编辑歌单
              }}>
              <MaterialIcons name="edit" size={20} color="#fff" />
            </TouchableOpacity>
          )}
        </View>
      </View>
    );
  };

  // 渲染空状态
  const renderEmpty = () => (
    <View style={styles.emptyContainer}>
      <MaterialIcons name="music-off" size={64} color="#333" />
      <Text style={styles.emptyText}>歌单为空</Text>
      <Text style={styles.emptyHint}>去搜索添加歌曲吧</Text>
    </View>
  );

  // 渲染歌曲项
  const renderSongItem = ({item, index}: {item: Song; index: number}) => (
    <SongItem
      song={item}
      index={index}
      onPress={handleSongPress}
      onMorePress={handleMorePress}
      showIndex
    />
  );

  if (!playlist) {
    return (
      <View style={styles.container}>
        <StatusBar barStyle="light-content" />
        <View style={styles.notFound}>
          <Text style={styles.notFoundText}>歌单不存在</Text>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" />

      {/* 顶部导航 */}
      <View style={styles.navHeader}>
        <TouchableOpacity
          style={styles.navButton}
          onPress={() => navigation.goBack()}>
          <MaterialIcons name="arrow-back" size={24} color="#fff" />
        </TouchableOpacity>
        <Text style={styles.navTitle}>{playlist.name}</Text>
        <TouchableOpacity style={styles.navButton}>
          <MaterialIcons name="more-vert" size={24} color="#fff" />
        </TouchableOpacity>
      </View>

      <FlatList
        data={playlist.songs}
        keyExtractor={item => item.id}
        renderItem={renderSongItem}
        ListHeaderComponent={renderHeader}
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
  navHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 8,
    paddingVertical: 12,
    backgroundColor: '#1a1a1a',
  },
  navButton: {
    padding: 8,
  },
  navTitle: {
    flex: 1,
    fontSize: 16,
    fontWeight: '600',
    color: '#fff',
    textAlign: 'center',
  },
  header: {
    alignItems: 'center',
    paddingVertical: 24,
    paddingHorizontal: 20,
  },
  coverContainer: {
    marginBottom: 20,
  },
  cover: {
    width: 180,
    height: 180,
    borderRadius: 8,
    backgroundColor: '#252525',
  },
  coverPlaceholder: {
    width: 180,
    height: 180,
    borderRadius: 8,
    backgroundColor: '#252525',
    justifyContent: 'center',
    alignItems: 'center',
  },
  playlistName: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#fff',
    textAlign: 'center',
  },
  playlistInfo: {
    fontSize: 13,
    color: '#888',
    marginTop: 8,
  },
  actions: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 20,
    gap: 16,
  },
  playAllButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#6366F1',
    borderRadius: 20,
    paddingVertical: 10,
    paddingHorizontal: 24,
  },
  playAllText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#fff',
    marginLeft: 4,
  },
  actionButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#333',
    justifyContent: 'center',
    alignItems: 'center',
  },
  listContent: {
    paddingBottom: 100,
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
  notFound: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  notFoundText: {
    fontSize: 16,
    color: '#666',
  },
});
