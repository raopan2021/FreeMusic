/**
 * 迷你播放器组件
 * 显示在屏幕底部的当前播放信息
 */

import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import {NativeStackNavigationProp} from '@react-navigation/native-stack';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {RootStackParamList} from '../navigation/AppNavigator';

type NavigationProp = NativeStackNavigationProp<RootStackParamList>;

export default function MiniPlayer(): React.JSX.Element | null {
  const navigation = useNavigation<NavigationProp>();
  const {player, setIsPlaying, playNext} = useMusicStore();
  const {currentSong, isPlaying, position, duration} = player;

  if (!currentSong) {
    return null;
  }

  const progress = duration > 0 ? position / duration : 0;

  const togglePlay = () => {
    setIsPlaying(!isPlaying);
  };

  return (
    <TouchableOpacity
      style={styles.container}
      onPress={() => navigation.navigate('Player')}
      activeOpacity={0.9}>
      {/* 进度条 */}
      <View style={styles.progressBar}>
        <View style={[styles.progress, {width: `${progress * 100}%`}]} />
      </View>

      <View style={styles.content}>
        {/* 封面 */}
        <View style={styles.coverContainer}>
          {currentSong.coverUrl ? (
            <Image source={{uri: currentSong.coverUrl}} style={styles.cover} />
          ) : (
            <View style={styles.coverPlaceholder}>
              <MaterialIcons name="music-note" size={20} color="#666" />
            </View>
          )}
        </View>

        {/* 歌曲信息 */}
        <View style={styles.info}>
          <Text style={styles.title} numberOfLines={1}>
            {currentSong.title}
          </Text>
          <Text style={styles.artist} numberOfLines={1}>
            {currentSong.artist}
          </Text>
        </View>

        {/* 控制按钮 */}
        <View style={styles.controls}>
          <TouchableOpacity
            style={styles.controlButton}
            onPress={togglePlay}
            hitSlop={{top: 10, bottom: 10, left: 10, right: 10}}>
            <MaterialIcons
              name={isPlaying ? 'pause' : 'play-arrow'}
              size={32}
              color="#fff"
            />
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.controlButton}
            onPress={playNext}
            hitSlop={{top: 10, bottom: 10, left: 10, right: 10}}>
            <MaterialIcons name="skip-next" size={28} color="#fff" />
          </TouchableOpacity>
        </View>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: '#2a2a2a',
    borderTopWidth: 1,
    borderTopColor: '#333',
  },
  progressBar: {
    height: 2,
    backgroundColor: '#333',
  },
  progress: {
    height: '100%',
    backgroundColor: '#6366F1',
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
    paddingHorizontal: 12,
  },
  coverContainer: {
    marginRight: 12,
  },
  cover: {
    width: 48,
    height: 48,
    borderRadius: 4,
    backgroundColor: '#333',
  },
  coverPlaceholder: {
    width: 48,
    height: 48,
    borderRadius: 4,
    backgroundColor: '#333',
    justifyContent: 'center',
    alignItems: 'center',
  },
  info: {
    flex: 1,
    justifyContent: 'center',
  },
  title: {
    fontSize: 14,
    fontWeight: '600',
    color: '#fff',
  },
  artist: {
    fontSize: 12,
    color: '#888',
    marginTop: 2,
  },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  controlButton: {
    padding: 8,
  },
});
