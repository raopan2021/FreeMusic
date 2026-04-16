/**
 * 搜索页面
 */

import React, {useState, useCallback, useEffect} from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  Keyboard,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import {NativeStackNavigationProp} from '@react-navigation/native-stack';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';
import {SongItem} from '../components';
import {searchSongs, getPlayUrl} from '../api/netease';
import {playSong} from '../services/playerService';
import {Song} from '../types';
import {RootStackParamList} from '../navigation/AppNavigator';

type NavigationProp = NativeStackNavigationProp<RootStackParamList>;

export default function SearchScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const {searchHistory, addSearchHistory, removeSearchHistory, clearSearchHistory, player} =
    useMusicStore();

  const [keyword, setKeyword] = useState('');
  const [searchResults, setSearchResults] = useState<Song[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [total, setTotal] = useState(0);

  // 执行搜索
  const performSearch = useCallback(async (query: string) => {
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    setIsSearching(true);
    try {
      const result = await searchSongs(query.trim(), 30, 0);
      setSearchResults(result.songs);
      setHasMore(result.hasMore);
      setTotal(result.total);
      addSearchHistory(query.trim());
    } catch (error) {
      console.error('Search error:', error);
      setSearchResults([]);
    } finally {
      setIsSearching(false);
    }
  }, [addSearchHistory]);

  // 处理搜索提交
  const handleSearch = useCallback(() => {
    Keyboard.dismiss();
    performSearch(keyword);
  }, [keyword, performSearch]);

  // 处理歌曲点击
  const handleSongPress = useCallback(
    async (song: Song, index: number) => {
      try {
        // 获取播放 URL
        if (song.isNetease && song.neteaseId) {
          const url = await getPlayUrl(song.neteaseId);
          if (url) {
            // 构建带 URL 的歌曲对象
            const songWithUrl = {...song, url};
            // 设置播放队列并播放
            const queue = searchResults.map(s => ({...s}));
            const queueWithUrls = queue.map(s =>
              s.id === song.id ? songWithUrl : s,
            );
            
            // 先添加到队列
            useMusicStore.getState().setQueue(queueWithUrls, index);
            await playSong(songWithUrl, url);
            return;
          }
        }
      } catch (error) {
        console.error('Play song error:', error);
      }
    },
    [searchResults],
  );

  // 处理更多按钮
  const handleMorePress = useCallback((song: Song) => {
    // TODO: 显示操作菜单（添加到我喜欢的音乐、添加到歌单等）
    console.log('More press:', song.title);
  }, []);

  // 渲染搜索历史
  const renderSearchHistory = () => {
    if (searchHistory.length === 0 || keyword.length > 0) {
      return null;
    }

    return (
      <View style={styles.historyContainer}>
        <View style={styles.historyHeader}>
          <Text style={styles.historyTitle}>搜索历史</Text>
          <TouchableOpacity onPress={clearSearchHistory}>
            <MaterialIcons name="delete-outline" size={20} color="#666" />
          </TouchableOpacity>
        </View>
        <View style={styles.historyTags}>
          {searchHistory.slice(0, 10).map((item, index) => (
            <TouchableOpacity
              key={`${item}-${index}`}
              style={styles.historyTag}
              onPress={() => {
                setKeyword(item);
                performSearch(item);
              }}>
              <MaterialIcons name="history" size={14} color="#888" />
              <Text style={styles.historyTagText}>{item}</Text>
              <TouchableOpacity
                style={styles.historyRemove}
                onPress={() => removeSearchHistory(item)}>
                <MaterialIcons name="close" size={14} color="#666" />
              </TouchableOpacity>
            </TouchableOpacity>
          ))}
        </View>
      </View>
    );
  };

  // 渲染搜索结果
  const renderSearchResults = () => {
    if (isSearching) {
      return (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#6366F1" />
          <Text style={styles.loadingText}>搜索中...</Text>
        </View>
      );
    }

    if (keyword.length > 0 && searchResults.length === 0) {
      return (
        <View style={styles.emptyContainer}>
          <MaterialIcons name="search-off" size={64} color="#333" />
          <Text style={styles.emptyText}>未找到相关歌曲</Text>
          <Text style={styles.emptyHint}>试试其他关键词</Text>
        </View>
      );
    }

    return (
      <FlatList
        data={searchResults}
        keyExtractor={item => item.id}
        renderItem={({item, index}) => (
          <SongItem
            song={item}
            index={index}
            onPress={handleSongPress}
            onMorePress={handleMorePress}
          />
        )}
        contentContainerStyle={styles.resultsList}
        showsVerticalScrollIndicator={false}
      />
    );
  };

  // 渲染热门搜索提示
  const renderHotSearch = () => {
    if (keyword.length > 0 || searchResults.length > 0) {
      return null;
    }

    return (
      <View style={styles.hotContainer}>
        <Text style={styles.hotTitle}>试试搜索</Text>
        <View style={styles.hotTags}>
          {['周杰伦', 'Taylor Swift', 'Coldplay', '林俊杰', '邓紫棋'].map(
            (item, index) => (
              <TouchableOpacity
                key={`${item}-${index}`}
                style={styles.hotTag}
                onPress={() => {
                  setKeyword(item);
                  performSearch(item);
                }}>
                <Text style={styles.hotTagText}>{item}</Text>
              </TouchableOpacity>
            ),
          )}
        </View>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      {/* 搜索框 */}
      <View style={styles.searchHeader}>
        <View style={styles.searchInputContainer}>
          <MaterialIcons name="search" size={20} color="#666" />
          <TextInput
            style={styles.searchInput}
            placeholder="搜索歌曲、歌手"
            placeholderTextColor="#666"
            value={keyword}
            onChangeText={setKeyword}
            onSubmitEditing={handleSearch}
            returnKeyType="search"
            autoCorrect={false}
          />
          {keyword.length > 0 && (
            <TouchableOpacity onPress={() => setKeyword('')}>
              <MaterialIcons name="close" size={20} color="#666" />
            </TouchableOpacity>
          )}
        </View>
        <TouchableOpacity style={styles.cancelButton} onPress={() => navigation.goBack()}>
          <Text style={styles.cancelText}>取消</Text>
        </TouchableOpacity>
      </View>

      {renderSearchHistory()}
      {renderHotSearch()}
      {renderSearchResults()}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a1a',
  },
  searchHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#1a1a1a',
  },
  searchInputContainer: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#2a2a2a',
    borderRadius: 8,
    paddingHorizontal: 12,
    height: 40,
  },
  searchInput: {
    flex: 1,
    fontSize: 15,
    color: '#fff',
    marginLeft: 8,
    padding: 0,
  },
  cancelButton: {
    marginLeft: 12,
    padding: 8,
  },
  cancelText: {
    fontSize: 15,
    color: '#6366F1',
  },
  historyContainer: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  historyHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  historyTitle: {
    fontSize: 14,
    color: '#888',
  },
  historyTags: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  historyTag: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#252525',
    borderRadius: 16,
    paddingVertical: 6,
    paddingLeft: 10,
    paddingRight: 4,
    gap: 4,
  },
  historyTagText: {
    fontSize: 13,
    color: '#fff',
  },
  historyRemove: {
    padding: 4,
  },
  hotContainer: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  hotTitle: {
    fontSize: 14,
    color: '#888',
    marginBottom: 12,
  },
  hotTags: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  hotTag: {
    backgroundColor: '#252525',
    borderRadius: 16,
    paddingVertical: 8,
    paddingHorizontal: 16,
  },
  hotTagText: {
    fontSize: 13,
    color: '#fff',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    fontSize: 14,
    color: '#666',
    marginTop: 12,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
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
  resultsList: {
    paddingBottom: 100,
  },
});
