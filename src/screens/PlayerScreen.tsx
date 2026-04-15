import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  StatusBar,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';

export default function PlayerScreen() {
  const navigation = useNavigation();
  const {player, setIsPlaying, playNext, playPrevious, setPosition} =
    useMusicStore();
  const {currentSong, isPlaying, position, duration, repeatMode, shuffleEnabled} =
    player;

  const formatTime = (ms: number) => {
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  const progress = duration > 0 ? position / duration : 0;

  const togglePlay = () => {
    setIsPlaying(!isPlaying);
  };

  const cycleRepeatMode = () => {
    const modes: ('off' | 'all' | 'one')[] = ['off', 'all', 'one'];
    const currentIndex = modes.indexOf(repeatMode);
    const nextIndex = (currentIndex + 1) % modes.length;
    useMusicStore.getState().setRepeatMode(modes[nextIndex]);
  };

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

      {/* 封面 */}
      <View style={styles.coverContainer}>
        <View style={styles.cover}>
          <MaterialIcons name="album" size={120} color="#333" />
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
        <View style={styles.progressBar}>
          <View style={[styles.progressFill, {width: `${progress * 100}%`}]} />
        </View>
        <View style={styles.timeContainer}>
          <Text style={styles.time}>{formatTime(position)}</Text>
          <Text style={styles.time}>{formatTime(duration)}</Text>
        </View>
      </View>

      {/* 控制按钮 */}
      <View style={styles.controls}>
        <TouchableOpacity
          style={styles.controlButton}
          onPress={() => useMusicStore.getState().toggleShuffle()}>
          <MaterialIcons
            name="shuffle"
            size={28}
            color={shuffleEnabled ? '#6366F1' : '#666'}
          />
        </TouchableOpacity>

        <TouchableOpacity style={styles.controlButton} onPress={playPrevious}>
          <MaterialIcons name="skip-previous" size={40} color="#fff" />
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.playButton}
          onPress={togglePlay}>
          <MaterialIcons
            name={isPlaying ? 'pause' : 'play-arrow'}
            size={48}
            color="#fff"
          />
        </TouchableOpacity>

        <TouchableOpacity style={styles.controlButton} onPress={playNext}>
          <MaterialIcons name="skip-next" size={40} color="#fff" />
        </TouchableOpacity>

        <TouchableOpacity style={styles.controlButton} onPress={cycleRepeatMode}>
          <MaterialIcons
            name={getRepeatIcon()}
            size={28}
            color={repeatMode !== 'off' ? '#6366F1' : '#666'}
          />
        </TouchableOpacity>
      </View>

      {/* 底部功能栏 */}
      <View style={styles.bottomBar}>
        <TouchableOpacity style={styles.bottomButton}>
          <MaterialIcons name="favorite-border" size={24} color="#fff" />
        </TouchableOpacity>
        <TouchableOpacity style={styles.bottomButton}>
          <MaterialIcons name="queue-music" size={24} color="#fff" />
        </TouchableOpacity>
        <TouchableOpacity style={styles.bottomButton}>
          <MaterialIcons name="lyrics" size={24} color="#fff" />
        </TouchableOpacity>
      </View>
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
  },
  headerButton: {
    padding: 8,
  },
  headerTitle: {
    fontSize: 14,
    color: '#888',
  },
  coverContainer: {
    alignItems: 'center',
    paddingHorizontal: 40,
    paddingTop: 40,
  },
  cover: {
    width: 280,
    height: 280,
    backgroundColor: '#2a2a2a',
    borderRadius: 8,
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
  progressBar: {
    height: 4,
    backgroundColor: '#333',
    borderRadius: 2,
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#6366F1',
    borderRadius: 2,
  },
  timeContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 8,
  },
  time: {
    fontSize: 12,
    color: '#666',
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
