/**
 * 歌单页面
 */

import React, {useState, useCallback} from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  TextInput,
  Alert,
  StatusBar,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import {NativeStackNavigationProp} from '@react-navigation/native-stack';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {MiniPlayer} from '../components';
import {Playlist} from '../types';
import {RootStackParamList} from '../navigation/AppNavigator';

type NavigationProp = NativeStackNavigationProp<RootStackParamList>;

export default function PlaylistScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const {playlists, favoritesPlaylist, addPlaylist, deletePlaylist} =
    useMusicStore();

  const [isCreating, setIsCreating] = useState(false);
  const [newPlaylistName, setNewPlaylistName] = useState('');

  // 创建歌单
  const handleCreatePlaylist = useCallback(() => {
    if (!newPlaylistName.trim()) {
      Alert.alert('提示', '请输入歌单名称');
      return;
    }
    addPlaylist(newPlaylistName.trim());
    setNewPlaylistName('');
    setIsCreating(false);
  }, [newPlaylistName, addPlaylist]);

  // 删除歌单
  const handleDeletePlaylist = useCallback(
    (playlist: Playlist) => {
      Alert.alert('删除歌单', `确定删除「${playlist.name}」吗？`, [
        {text: '取消', style: 'cancel'},
        {
          text: '删除',
          style: 'destructive',
          onPress: () => deletePlaylist(playlist.id),
        },
      ]);
    },
    [deletePlaylist],
  );

  // 渲染创建歌单输入框
  const renderCreateInput = () => {
    if (!isCreating) {
      return (
        <TouchableOpacity
          style={styles.createButton}
          onPress={() => setIsCreating(true)}>
          <MaterialIcons name="add" size={24} color="#6366F1" />
          <Text style={styles.createText}>创建歌单</Text>
        </TouchableOpacity>
      );
    }

    return (
      <View style={styles.createInputContainer}>
        <TextInput
          style={styles.createInput}
          placeholder="输入歌单名称"
          placeholderTextColor="#666"
          value={newPlaylistName}
          onChangeText={setNewPlaylistName}
          onSubmitEditing={handleCreatePlaylist}
          autoFocus
        />
        <TouchableOpacity
          style={styles.createConfirm}
          onPress={handleCreatePlaylist}>
          <MaterialIcons name="check" size={20} color="#6366F1" />
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.createCancel}
          onPress={() => {
            setIsCreating(false);
            setNewPlaylistName('');
          }}>
          <MaterialIcons name="close" size={20} color="#666" />
        </TouchableOpacity>
      </View>
    );
  };

  // 渲染歌单项
  const renderPlaylistItem = ({item}: {item: Playlist}) => (
    <TouchableOpacity
      style={styles.playlistItem}
      onPress={() => navigation.navigate('PlaylistDetail', {playlistId: item.id})}
      onLongPress={() => handleDeletePlaylist(item)}>
      <View style={styles.playlistCover}>
        {item.coverUrl ? (
          <MaterialIcons name="album" size={32} color="#666" />
        ) : (
          <MaterialIcons name="queue-music" size={32} color="#6366F1" />
        )}
      </View>
      <View style={styles.playlistInfo}>
        <Text style={styles.playlistName}>{item.name}</Text>
        <Text style={styles.playlistCount}>{item.songs.length} 首歌曲</Text>
      </View>
      <MaterialIcons name="chevron-right" size={24} color="#444" />
    </TouchableOpacity>
  );

  // 渲染收藏歌单
  const renderFavoritesPlaylist = () => (
    <TouchableOpacity
      style={styles.playlistItem}
      onPress={() => navigation.navigate('PlaylistDetail', {playlistId: 'favorites'})}>
      <View style={[styles.playlistCover, {backgroundColor: '#6366F1'}]}>
        <MaterialIcons name="favorite" size={32} color="#fff" />
      </View>
      <View style={styles.playlistInfo}>
        <Text style={styles.playlistName}>{favoritesPlaylist.name}</Text>
        <Text style={styles.playlistCount}>
          {favoritesPlaylist.songs.length} 首歌曲
        </Text>
      </View>
      <MaterialIcons name="chevron-right" size={24} color="#444" />
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" />

      {/* 头部 */}
      <View style={styles.header}>
        <Text style={styles.title}>我的歌单</Text>
      </View>

      <FlatList
        data={playlists}
        keyExtractor={item => item.id}
        renderItem={renderPlaylistItem}
        ListHeaderComponent={
          <>
            {renderFavoritesPlaylist()}
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>创建的歌单</Text>
              <Text style={styles.sectionCount}>{playlists.length}</Text>
            </View>
          </>
        }
        ListFooterComponent={renderCreateInput}
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
  playlistItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 20,
  },
  playlistCover: {
    width: 56,
    height: 56,
    backgroundColor: '#252525',
    borderRadius: 4,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  playlistInfo: {
    flex: 1,
  },
  playlistName: {
    fontSize: 15,
    fontWeight: '600',
    color: '#fff',
  },
  playlistCount: {
    fontSize: 12,
    color: '#888',
    marginTop: 4,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 24,
    paddingBottom: 8,
  },
  sectionTitle: {
    fontSize: 14,
    color: '#888',
  },
  sectionCount: {
    fontSize: 14,
    color: '#666',
    marginLeft: 8,
  },
  createButton: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 16,
    paddingHorizontal: 20,
    marginTop: 8,
  },
  createText: {
    fontSize: 15,
    color: '#6366F1',
    marginLeft: 8,
  },
  createInputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  createInput: {
    flex: 1,
    backgroundColor: '#252525',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 15,
    color: '#fff',
  },
  createConfirm: {
    marginLeft: 12,
    padding: 8,
  },
  createCancel: {
    marginLeft: 4,
    padding: 8,
  },
});
