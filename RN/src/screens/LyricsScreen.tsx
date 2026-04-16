/**
 * 歌词页面
 */

import React, {useState, useEffect, useCallback, useRef} from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  StatusBar,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {getLyrics as getNeteaseLyrics} from '../api/netease';
import {searchLyrics, parseLRC} from '../api/lrclib';

interface ParsedLyricLine {
  time: number;
  text: string;
}

export default function LyricsScreen(): React.JSX.Element {
  const navigation = useNavigation();
  const {player} = useMusicStore();
  const {currentSong, position} = player;

  const [plainLyrics, setPlainLyrics] = useState<string | null>(null);
  const [syncedLyrics, setSyncedLyrics] = useState<ParsedLyricLine[]>([]);
  const [currentLineIndex, setCurrentLineIndex] = useState(-1);
  const [isLoading, setIsLoading] = useState(false);
  const scrollViewRef = useRef<ScrollView>(null);

  // 加载歌词
  const loadLyrics = useCallback(async () => {
    if (!currentSong) {
      return;
    }

    setIsLoading(true);
    try {
      // 首先尝试网易云歌词
      if (currentSong.neteaseId) {
        const neteaseResult = await getNeteaseLyrics(currentSong.neteaseId);
        if (neteaseResult.lrc) {
          const parsed = parseLRC(neteaseResult.lrc);
          if (parsed.length > 0) {
            setSyncedLyrics(parsed);
            setPlainLyrics(neteaseResult.translation || null);
            setIsLoading(false);
            return;
          }
        }
      }

      // 尝试 LRCLIB
      const lrclibResult = await searchLyrics(
        currentSong.artist,
        currentSong.title,
        currentSong.duration / 1000,
      );

      if (lrclibResult?.syncedLyrics) {
        const parsed = parseLRC(lrclibResult.syncedLyrics);
        setSyncedLyrics(parsed);
        setPlainLyrics(lrclibResult.plainLyrics);
      } else {
        setSyncedLyrics([]);
        setPlainLyrics(null);
      }
    } catch (error) {
      console.error('Load lyrics error:', error);
      setSyncedLyrics([]);
      setPlainLyrics(null);
    } finally {
      setIsLoading(false);
    }
  }, [currentSong]);

  // 加载歌词
  useEffect(() => {
    loadLyrics();
  }, [loadLyrics]);

  // 更新当前行
  useEffect(() => {
    if (syncedLyrics.length === 0) {
      return;
    }

    const currentTime = position / 1000; // 转换为秒
    let index = -1;

    for (let i = 0; i < syncedLyrics.length; i++) {
      if (syncedLyrics[i].time <= currentTime) {
        index = i;
      } else {
        break;
      }
    }

    setCurrentLineIndex(index);

    // 滚动到当前行
    if (index >= 0 && scrollViewRef.current) {
      scrollViewRef.current.scrollTo({
        y: Math.max(0, index * 50 - 150),
        animated: true,
      });
    }
  }, [position, syncedLyrics]);

  // 渲染歌词行
  const renderLyricLine = (line: ParsedLyricLine, index: number) => {
    const isCurrentLine = index === currentLineIndex;
    const isPastLine = index < currentLineIndex;

    return (
      <Text
        key={`${line.time}-${index}`}
        style={[
          styles.lyricLine,
          isCurrentLine && styles.currentLyricLine,
          isPastLine && styles.pastLyricLine,
        ]}>
        {line.text || '♪'}
      </Text>
    );
  };

  // 渲染空状态
  const renderEmptyState = () => (
    <View style={styles.emptyContainer}>
      <MaterialIcons name="lyrics" size={64} color="#333" />
      <Text style={styles.emptyText}>暂无歌词</Text>
      <Text style={styles.emptyHint}>
        {currentSong?.title || '播放歌曲后自动加载歌词'}
      </Text>
    </View>
  );

  // 渲染加载状态
  const renderLoading = () => (
    <View style={styles.loadingContainer}>
      <Text style={styles.loadingText}>加载歌词中...</Text>
    </View>
  );

  if (!currentSong) {
    return (
      <View style={styles.container}>
        <StatusBar barStyle="light-content" />
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <MaterialIcons name="close" size={24} color="#fff" />
          </TouchableOpacity>
          <Text style={styles.headerTitle}>歌词</Text>
          <View style={{width: 24}} />
        </View>
        <View style={styles.emptyContainer}>
          <MaterialIcons name="music-off" size={64} color="#333" />
          <Text style={styles.emptyText}>暂无播放</Text>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" />

      {/* 头部 */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <MaterialIcons name="close" size={24} color="#fff" />
        </TouchableOpacity>
        <View style={styles.headerCenter}>
          <Text style={styles.headerTitle} numberOfLines={1}>
            {currentSong.title}
          </Text>
          <Text style={styles.headerSubtitle} numberOfLines={1}>
            {currentSong.artist}
          </Text>
        </View>
        <TouchableOpacity>
          <MaterialIcons name="more-vert" size={24} color="#fff" />
        </TouchableOpacity>
      </View>

      {/* 歌词内容 */}
      <ScrollView
        ref={scrollViewRef}
        style={styles.lyricsContainer}
        contentContainerStyle={styles.lyricsContent}
        showsVerticalScrollIndicator={false}
        scrollEventThrottle={16}>
        {isLoading ? (
          renderLoading()
        ) : syncedLyrics.length > 0 ? (
          syncedLyrics.map(renderLyricLine)
        ) : plainLyrics ? (
          <Text style={styles.plainLyrics}>{plainLyrics}</Text>
        ) : (
          renderEmptyState()
        )}
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
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#333',
  },
  headerCenter: {
    flex: 1,
    alignItems: 'center',
    marginHorizontal: 16,
  },
  headerTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#fff',
  },
  headerSubtitle: {
    fontSize: 12,
    color: '#888',
    marginTop: 2,
  },
  lyricsContainer: {
    flex: 1,
  },
  lyricsContent: {
    paddingVertical: 100,
    paddingHorizontal: 40,
    alignItems: 'center',
  },
  lyricLine: {
    fontSize: 18,
    color: '#666',
    textAlign: 'center',
    lineHeight: 50,
  },
  currentLyricLine: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#fff',
  },
  pastLyricLine: {
    color: '#444',
  },
  plainLyrics: {
    fontSize: 16,
    color: '#888',
    textAlign: 'center',
    lineHeight: 28,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 18,
    color: '#666',
    marginTop: 16,
  },
  emptyHint: {
    fontSize: 13,
    color: '#444',
    marginTop: 4,
    textAlign: 'center',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    fontSize: 14,
    color: '#666',
  },
});
