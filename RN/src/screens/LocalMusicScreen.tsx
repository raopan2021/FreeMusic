/**
 * 本地音乐页面
 */

import React, {useState, useEffect, useCallback} from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  StatusBar,
  Alert,
  PermissionsAndroid,
  Platform,
  ActivityIndicator,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {MiniPlayer, SongItem} from '../components';
import {scanLocalMusic, requestStoragePermission, checkStoragePermission} from '../services/localMusicService';
import {Song} from '../types';
import TrackPlayer, {State} from 'react-native-track-player';

export default function LocalMusicScreen(): React.JSX.Element {
  const navigation = useNavigation();
  const {localSongs, setLocalSongs, setQueue, player} = useMusicStore();

  const [isScanning, setIsScanning] = useState(false);
  const [hasPermission, setHasPermission] = useState<boolean | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // 检查权限
  const checkPermission = useCallback(async () => {
    const granted = await checkStoragePermission();
    setHasPermission(granted);
  }, []);

  // 扫描本地音乐
  const handleScan = useCallback(async () => {
    if (Platform.OS === 'android') {
      const granted = await requestStoragePermission();
      if (!granted) {
        Alert.alert('权限不足', '需要音频文件访问权限才能扫描本地音乐');
        setHasPermission(false);
        return;
      }
      setHasPermission(true);
    }

    setIsScanning(true);
    try {
      const songs = await scanLocalMusic();
      setLocalSongs(songs);
      if (songs.length === 0) {
        Alert.alert('提示', '未找到本地音频文件');
      } else {
        Alert.alert('扫描完成', `找到 ${songs.length} 首歌曲`);
      }
    } catch (error) {
      console.error('Scan error:', error);
      Alert.alert('错误', '扫描本地音乐失败');
    } finally {
      setIsScanning(false);
    }
  }, [setLocalSongs]);

  // 处理歌曲点击
  const handleSongPress = useCallback(
    async (song: Song, index: number) => {
      try {
        // 设置队列
        setQueue(localSongs, index);

        // 使用 TrackPlayer 播放
        const queue = await TrackPlayer.getQueue();
        if (queue.length > 0) {
          await TrackPlayer.skip(index);
          await TrackPlayer.play();
        }
      } catch (error) {
        console.error('Play song error:', error);
      }
    },
    [localSongs, setQueue],
  );

  // 初始化加载
  useEffect(() => {
    checkPermission();
  }, [checkPermission]);

  // 权限被拒绝时的渲染
  if (hasPermission === false) {
    return (
      <View style={styles.container}>
        <StatusBar barStyle="light-content" />
        <View style={styles.permissionContainer}>
          <MaterialIcons name="folder-off" size={64} color="#333" />
          <Text style={styles.permissionTitle}>需要音频文件权限</Text>
          <Text style={styles.permissionHint}>
            请授予音频文件访问权限以扫描本地音乐
          </Text>
          <TouchableOpacity style={styles.permissionButton} onPress={handleScan}>
            <Text style={styles.permissionButtonText}>授予权限</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  // 扫描状态
  if (localSongs.length === 0 && !isScanning) {
    return (
      <View style={styles.container}>
        <StatusBar barStyle="light-content" />
        <View style={styles.header}>
          <Text style={styles.title}>本地音乐</Text>
        </View>
        <View style={styles.emptyContainer}>
          <MaterialIcons name="library-music" size={64} color="#333" />
          <Text style={styles.emptyText}>暂无本地音乐</Text>
          <Text style={styles.emptyHint}>
            扫描您的设备以发现本地音频文件
          </Text>
          <TouchableOpacity style={styles.scanButton} onPress={handleScan}>
            <MaterialIcons name="refresh" size={20} color="#fff" />
            <Text style={styles.scanButtonText}>扫描本地音乐</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  // 扫描中状态
  if (isScanning) {
    return (
      <View style={styles.container}>
        <StatusBar barStyle="light-content" />
        <View style={styles.header}>
          <Text style={styles.title}>本地音乐</Text>
        </View>
        <View style={styles.scanningContainer}>
          <ActivityIndicator size="large" color="#6366F1" />
          <Text style={styles.scanningText}>正在扫描...</Text>
          <Text style={styles.scanningHint}>这可能需要一些时间</Text>
        </View>
      </View>
    );
  }

  // 渲染歌曲项
  const renderSongItem = ({item, index}: {item: Song; index: number}) => (
    <SongItem
      song={item}
      index={index}
      onPress={handleSongPress}
      showIndex
    />
  );

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" />

      {/* 头部 */}
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>本地音乐</Text>
          <Text style={styles.subtitle}>{localSongs.length} 首歌曲</Text>
        </View>
        <TouchableOpacity style={styles.rescanButton} onPress={handleScan}>
          <MaterialIcons name="refresh" size={20} color="#6366F1" />
        </TouchableOpacity>
      </View>

      <FlatList
        data={localSongs}
        keyExtractor={item => item.id}
        renderItem={renderSongItem}
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
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 16,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
  },
  subtitle: {
    fontSize: 13,
    color: '#888',
    marginTop: 4,
  },
  rescanButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#252525',
    justifyContent: 'center',
    alignItems: 'center',
  },
  listContent: {
    paddingBottom: 100,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 40,
  },
  emptyText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#666',
    marginTop: 20,
  },
  emptyHint: {
    fontSize: 14,
    color: '#444',
    marginTop: 8,
    textAlign: 'center',
  },
  scanButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#6366F1',
    borderRadius: 20,
    paddingVertical: 12,
    paddingHorizontal: 24,
    marginTop: 24,
  },
  scanButtonText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#fff',
    marginLeft: 8,
  },
  scanningContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  scanningText: {
    fontSize: 16,
    color: '#fff',
    marginTop: 16,
  },
  scanningHint: {
    fontSize: 13,
    color: '#666',
    marginTop: 8,
  },
  permissionContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 40,
  },
  permissionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#fff',
    marginTop: 20,
  },
  permissionHint: {
    fontSize: 14,
    color: '#666',
    marginTop: 8,
    textAlign: 'center',
  },
  permissionButton: {
    backgroundColor: '#6366F1',
    borderRadius: 20,
    paddingVertical: 12,
    paddingHorizontal: 24,
    marginTop: 24,
  },
  permissionButtonText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#fff',
  },
});
