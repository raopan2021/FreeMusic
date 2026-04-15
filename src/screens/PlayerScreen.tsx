/**
 * 全屏播放器页面
 */

import React, {useEffect, useCallback} from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  StatusBar,
  Image,
  ScrollView,
} from 'react-native';
import {useNavigation, useRoute, RouteProp} from '@react-navigation/native';
import {NativeStackNavigationProp} from '@react-navigation/native-stack';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {ProgressBar} from '../components';
import {
  pause,
  resume,
  skipToNext,
  skipToPrevious,
  seekTo,
} from '../services/playerService';
import {RootStackParamList} from '../navigation/AppNavigator';

type PlayerScreenRouteProp = RouteProp<RootStackParamList, 'Player'>;
type NavigationProp = NativeStackNavigationProp<RootStackParamList>;

export default function PlayerScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const route = useRoute<PlayerScreenRouteProp>();

  const {
    player,
    setIsPlaying,
    toggleShuffle,
    setRepeatMode,
    toggleFavorite,
    isFavorite,
  } = useMusicStore();

  const {currentSong, isPlaying, position, duration, repeatMode, shuffleEnabled} =
    player;

  // 处理播放/暂停
  const handleTogglePlay = useCallback(async () => {
    if (isPlaying) {
      await pause();
    } else {
      await resume();
    }
  }, [isPlaying]);

  // 处理上一首
  const handlePrevious = useCallback(async () => {
    await skipToPrevious();
  }, []);

  // 处理下一首
  const handleNext = useCallback(async () => {
    await skipToNext();
  }, []);

  // 处理进度条拖动
  const handleSeek = useCallback(async (pos: number) => {
    await seekTo(pos / 1000); // 转换为秒
  }, []);

  // 处理循环模式切换
  const handleRepeatMode = useCallback(() => {
    const modes: ('off' | 'all' | 'one')[] = ['off', 'all', 'one'];
    const currentIndex = modes.indexOf(repeatMode);
    const nextIndex = (currentIndex + 1) % modes.length;
    setRepeatMode(modes[nextIndex]);
  }, [repeatMode, setRepeatMode]);

  // 获取循环图标
  const getRepeatIcon = () => {
    switch (repeatMode) {
      case 'one':
        return 'repeat-one';
      case 'all':
        return 'repeat';
      default:
        return 'repeat';
    }
  };

  // 获取收藏状态
  const getFavoriteIcon = () => {
    if (!currentSong) {
      return 'favorite-border';
    }
    return isFavorite(currentSong.id) ? 'favorite' : 'favorite-border';
  };

  // 获取收藏颜色
  const getFavoriteColor = () => {
    if (!currentSong) {
      return '#fff';
    }
    return isFavorite(currentSong.id) ? '#6366F1' : '#fff';
  };

  if (!currentSong) {
    return (
      <View style={styles.container}>
        <StatusBar barStyle="light-content" />
        <View style={styles.emptyState}>
          <MaterialIcons name="music-off" size={64} color="#333" />
          <Text style={styles.emptyText}>暂无播放</Text>
          <TouchableOpacity
            style={styles.closeButton}
            onPress={() => navigation.goBack()}>
            <MaterialIcons name="close" size={24} color="#fff" />
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" />

      {/* 顶部栏 */}
      <View style={styles.header}>
        <TouchableOpacity
          style={styles.headerButton}
          onPress={() => navigation.goBack()}>
          <MaterialIcons name="keyboard-arrow-down" size={32} color="#fff" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>正在播放</Text>
        <TouchableOpacity style={styles.headerButton}>
          <MaterialIcons name="more-vert" size={24} color="#fff" />
        </TouchableOpacity>
      </View>

      <ScrollView
        style={styles.scrollView}
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}>
        {/* 封面 */}
        <View style={styles.coverContainer}>
          <View style={styles.cover}>
            {currentSong.coverUrl ? (
              <Image
                source={{uri: currentSong.coverUrl}}
                style={styles.coverImage}
                resizeMode="cover"
              />
            ) : (
              <View style={styles.coverPlaceholder}>
                <MaterialIcons name="album" size={120} color="#333" />
              </View>
            )}
          </View>
        </View>

        {/* 歌曲信息 */}
        <View style={styles.infoContainer}>
          <Text style={styles.title} numberOfLines={1}>
            {currentSong.title}
          </Text>
          <Text style={styles.artist} numberOfLines={1}>
            {currentSong.artist} • {currentSong.album}
          </Text>
        </View>

        {/* 进度条 */}
        <View style={styles.progressContainer}>
          <ProgressBar
            position={position}
            duration={duration}
            onSeek={handleSeek}
          />
        </View>

        {/* 播放控制 */}
        <View style={styles.controls}>
          <TouchableOpacity
            style={styles.controlButton}
            onPress={toggleShuffle}>
            <MaterialIcons
              name="shuffle"
              size={28}
              color={shuffleEnabled ? '#6366F1' : '#666'}
            />
          </TouchableOpacity>

          <TouchableOpacity style={styles.controlButton} onPress={handlePrevious}>
            <MaterialIcons name="skip-previous" size={40} color="#fff" />
          </TouchableOpacity>

          <TouchableOpacity style={styles.playButton} onPress={handleTogglePlay}>
            <MaterialIcons
              name={isPlaying ? 'pause' : 'play-arrow'}
              size={48}
              color="#fff"
            />
          </TouchableOpacity>

          <TouchableOpacity style={styles.controlButton} onPress={handleNext}>
            <MaterialIcons name="skip-next" size={40} color="#fff" />
          </TouchableOpacity>

          <TouchableOpacity style={styles.controlButton} onPress={handleRepeatMode}>
            <MaterialIcons
              name={getRepeatIcon()}
              size={28}
              color={repeatMode !== 'off' ? '#6366F1' : '#666'}
            />
          </TouchableOpacity>
        </View>

        {/* 底部功能栏 */}
        <View style={styles.bottomBar}>
          <TouchableOpacity
            style={styles.bottomButton}
            onPress={() => toggleFavorite(currentSong)}>
            <MaterialIcons
              name={getFavoriteIcon()}
              size={24}
              color={getFavoriteColor()}
            />
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.bottomButton}
            onPress={() => navigation.navigate('Queue')}>
            <MaterialIcons name="queue-music" size={24} color="#fff" />
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.bottomButton}
            onPress={() => navigation.navigate('Lyrics')}>
            <MaterialIcons name="lyrics" size={24} color="#fff" />
          </TouchableOpacity>
        </View>
      </ScrollView>
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
    paddingHorizontal: 8,
    paddingTop: 8,
    paddingBottom: 16,
  },
  headerButton: {
    padding: 8,
  },
  headerTitle: {
    fontSize: 14,
    color: '#888',
  },
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    paddingBottom: 40,
  },
  coverContainer: {
    alignItems: 'center',
    paddingHorizontal: 40,
  },
  cover: {
    width: 280,
    height: 280,
    backgroundColor: '#2a2a2a',
    borderRadius: 8,
    overflow: 'hidden',
    elevation: 8,
    shadowColor: '#000',
    shadowOffset: {width: 0, height: 4},
    shadowOpacity: 0.3,
    shadowRadius: 8,
  },
  coverImage: {
    width: '100%',
    height: '100%',
  },
  coverPlaceholder: {
    width: '100%',
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
  },
  infoContainer: {
    paddingHorizontal: 24,
    paddingTop: 32,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#fff',
    textAlign: 'center',
  },
  artist: {
    fontSize: 14,
    color: '#888',
    textAlign: 'center',
    marginTop: 8,
  },
  progressContainer: {
    paddingHorizontal: 24,
    paddingTop: 32,
  },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    paddingHorizontal: 16,
    paddingTop: 24,
  },
  controlButton: {
    padding: 8,
  },
  playButton: {
    width: 72,
    height: 72,
    backgroundColor: '#6366F1',
    borderRadius: 36,
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 4,
    shadowColor: '#6366F1',
    shadowOffset: {width: 0, height: 2},
    shadowOpacity: 0.3,
    shadowRadius: 4,
  },
  bottomBar: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingHorizontal: 40,
    paddingTop: 32,
  },
  bottomButton: {
    padding: 8,
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
  closeButton: {
    position: 'absolute',
    top: 48,
    left: 16,
    padding: 8,
  },
});
