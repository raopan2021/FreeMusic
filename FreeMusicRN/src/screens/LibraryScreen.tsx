/**
 * 音乐库页面
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
import {NativeStackNavigationProp} from '@react-navigation/native-stack';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {MiniPlayer} from '../components';
import {RootStackParamList} from '../navigation/AppNavigator';

type NavigationProp = NativeStackNavigationProp<RootStackParamList>;

export default function LibraryScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const {favoritesPlaylist, playlists, localSongs, playHistory} = useMusicStore();

  // 渲染库项目
  const renderLibraryItem = ({
    icon,
    title,
    count,
    onPress,
  }: {
    icon: string;
    title: string;
    count: number;
    onPress: () => void;
  }) => (
    <TouchableOpacity style={styles.libraryItem} onPress={onPress}>
      <View style={[styles.libraryIcon, {backgroundColor: '#6366F1'}]}>
        <MaterialIcons name={icon} size={24} color="#fff" />
      </View>
      <View style={styles.libraryInfo}>
        <Text style={styles.libraryTitle}>{title}</Text>
        <Text style={styles.libraryCount}>{count}</Text>
      </View>
      <MaterialIcons name="chevron-right" size={24} color="#444" />
    </TouchableOpacity>
  );

  // 渲染快捷入口
  const renderQuickAccess = () => (
    <View style={styles.quickAccess}>
      <TouchableOpacity
        style={styles.quickItem}
        onPress={() => navigation.navigate('Search')}>
        <View style={[styles.quickIcon, {backgroundColor: '#6366F1'}]}>
          <MaterialIcons name="search" size={24} color="#fff" />
        </View>
        <Text style={styles.quickText}>搜索歌曲</Text>
      </TouchableOpacity>

      <TouchableOpacity
        style={styles.quickItem}
        onPress={() => navigation.navigate('Playlist')}>
        <View style={[styles.quickIcon, {backgroundColor: '#EC4899'}]}>
          <MaterialIcons name="favorite" size={24} color="#fff" />
        </View>
        <Text style={styles.quickText}>我的歌单</Text>
      </TouchableOpacity>
    </View>
  );

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" />

      {/* 头部 */}
      <View style={styles.header}>
        <Text style={styles.title}>音乐库</Text>
      </View>

      <FlatList
        data={[]}
        ListHeaderComponent={
          <>
            {renderQuickAccess()}

            {/* 我喜欢的音乐 */}
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>收藏</Text>
              {renderLibraryItem({
                icon: 'favorite',
                title: '我喜欢的音乐',
                count: favoritesPlaylist.songs.length,
                onPress: () =>
                  navigation.navigate('PlaylistDetail', {playlistId: 'favorites'}),
              })}
            </View>

            {/* 歌单 */}
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>歌单</Text>
              {renderLibraryItem({
                icon: 'queue-music',
                title: '创建的歌单',
                count: playlists.length,
                onPress: () => navigation.navigate('Playlist'),
              })}
            </View>

            {/* 本地音乐 */}
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>本地</Text>
              {renderLibraryItem({
                icon: 'folder',
                title: '本地音乐',
                count: localSongs.length,
                onPress: () => navigation.navigate('LocalMusic'),
              })}
            </View>

            {/* 播放历史 */}
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>最近</Text>
              {renderLibraryItem({
                icon: 'history',
                title: '播放历史',
                count: playHistory.length,
                onPress: () => {
                  // TODO: 播放历史页面
                },
              })}
            </View>
          </>
        }
        renderItem={null}
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
  header: {
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 16,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
  },
  listContent: {
    paddingBottom: 100,
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
    paddingHorizontal: 20,
  },
  sectionTitle: {
    fontSize: 14,
    color: '#888',
    marginBottom: 12,
  },
  libraryItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#252525',
    borderRadius: 8,
    padding: 12,
    marginBottom: 8,
  },
  libraryIcon: {
    width: 48,
    height: 48,
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  libraryInfo: {
    flex: 1,
  },
  libraryTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: '#fff',
  },
  libraryCount: {
    fontSize: 12,
    color: '#888',
    marginTop: 4,
  },
});
