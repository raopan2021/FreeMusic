/**
 * 设置页面
 */

import React, {useCallback} from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  StatusBar,
  Switch,
  Alert,
} from 'react-native';
import {useMusicStore} from '../store/musicStore';

export default function SettingsScreen(): React.JSX.Element {
  const {settings, updateSettings, resetSettings} = useMusicStore();

  // 主题选项
  const themeOptions = [
    {value: 'dark', label: '暗色'},
    {value: 'light', label: '亮色'},
    {value: 'system', label: '跟随系统'},
  ];

  // 播放速度选项
  const speedOptions = [0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0];

  // 渲染设置项
  const renderSettingItem = ({
    title,
    subtitle,
    right,
    onPress,
  }: {
    title: string;
    subtitle?: string;
    right?: React.ReactNode;
    onPress?: () => void;
  }) => (
    <TouchableOpacity
      style={styles.settingItem}
      onPress={onPress}
      disabled={!onPress}>
      <View style={styles.settingInfo}>
        <Text style={styles.settingTitle}>{title}</Text>
        {subtitle && <Text style={styles.settingSubtitle}>{subtitle}</Text>}
      </View>
      {right}
    </TouchableOpacity>
  );

  // 渲染分段选项
  const renderSegmentOptions = <T extends string | number>({
    options,
    value,
    onChange,
    renderLabel,
  }: {
    options: T[];
    value: T;
    onChange: (value: T) => void;
    renderLabel: (value: T) => string;
  }) => (
    <View style={styles.segmentContainer}>
      {options.map(option => (
        <TouchableOpacity
          key={String(option)}
          style={[
            styles.segmentItem,
            value === option && styles.segmentItemActive,
          ]}
          onPress={() => onChange(option)}>
          <Text
            style={[
              styles.segmentText,
              value === option && styles.segmentTextActive,
            ]}>
            {renderLabel(option)}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );

  // 渲染关于部分
  const renderAbout = () => (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>关于</Text>
      {renderSettingItem({
        title: '版本',
        right: <Text style={styles.versionText}>1.0.0</Text>,
      })}
      {renderSettingItem({
        title: '关于 FreeMusic',
        onPress: () => {
          Alert.alert(
            'FreeMusic RN',
            '基于 React Native 的网易云音乐第三方客户端\n\n使用 react-native-track-player 实现音频播放\n使用 Zustand 进行状态管理',
          );
        },
      })}
    </View>
  );

  // 渲染重置选项
  const renderReset = () => (
    <View style={styles.section}>
      {renderSettingItem({
        title: '重置设置',
        subtitle: '恢复默认设置',
        onPress: () => {
          Alert.alert('重置设置', '确定要恢复默认设置吗？', [
            {text: '取消', style: 'cancel'},
            {
              text: '重置',
              style: 'destructive',
              onPress: resetSettings,
            },
          ]);
        },
      })}
    </View>
  );

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" />

      {/* 头部 */}
      <View style={styles.header}>
        <Text style={styles.title}>设置</Text>
      </View>

      <ScrollView
        style={styles.scrollView}
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}>
        {/* 主题设置 */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>外观</Text>
          {renderSettingItem({
            title: '主题',
            subtitle: '选择应用主题',
            right: renderSegmentOptions<string>({
              options: themeOptions.map(t => t.value) as string[],
              value: settings.theme,
              onChange: value => updateSettings({theme: value}),
              renderLabel: value => {
                const option = themeOptions.find(t => t.value === value);
                return option?.label || value;
              },
            }),
          })}
        </View>

        {/* 播放设置 */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>播放</Text>
          {renderSettingItem({
            title: '自动播放',
            subtitle: '歌曲加载完成后自动播放',
            right: (
              <Switch
                value={settings.autoPlay}
                onValueChange={value => updateSettings({autoPlay: value})}
                trackColor={{false: '#333', true: '#6366F1'}}
                thumbColor="#fff"
              />
            ),
          })}
          {renderSettingItem({
            title: '跳过静音',
            subtitle: '自动跳过音频中的静音片段',
            right: (
              <Switch
                value={settings.skipSilence}
                onValueChange={value => updateSettings({skipSilence: value})}
                trackColor={{false: '#333', true: '#6366F1'}}
                thumbColor="#fff"
              />
            ),
          })}
          {renderSettingItem({
            title: '播放速度',
            subtitle: `当前: ${settings.playbackSpeed}x`,
            right: renderSegmentOptions<number>({
              options: speedOptions,
              value: settings.playbackSpeed,
              onChange: value => updateSettings({playbackSpeed: value}),
              renderLabel: value => `${value}x`,
            }),
          })}
        </View>

        {/* 歌词设置 */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>歌词</Text>
          {renderSettingItem({
            title: '歌词字体大小',
            subtitle: `当前: ${settings.lyricsFontSize}px`,
            right: renderSegmentOptions<number>({
              options: [14, 16, 18, 20, 22],
              value: settings.lyricsFontSize,
              onChange: value => updateSettings({lyricsFontSize: value}),
              renderLabel: value => `${value}`,
            }),
          })}
        </View>

        {/* 数据管理 */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>数据</Text>
          {renderSettingItem({
            title: '清除搜索历史',
            onPress: () => {
              useMusicStore.getState().clearSearchHistory();
              Alert.alert('提示', '搜索历史已清除');
            },
          })}
          {renderSettingItem({
            title: '清除播放历史',
            onPress: () => {
              useMusicStore.getState().clearPlayHistory();
              Alert.alert('提示', '播放历史已清除');
            },
          })}
        </View>

        {renderAbout()}
        {renderReset()}
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
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 16,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
  },
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    paddingBottom: 100,
  },
  section: {
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 14,
    color: '#888',
    paddingHorizontal: 20,
    marginBottom: 8,
  },
  settingItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 14,
    paddingHorizontal: 20,
    backgroundColor: '#252525',
    borderBottomWidth: 1,
    borderBottomColor: '#1a1a1a',
  },
  settingInfo: {
    flex: 1,
    marginRight: 12,
  },
  settingTitle: {
    fontSize: 15,
    color: '#fff',
  },
  settingSubtitle: {
    fontSize: 12,
    color: '#888',
    marginTop: 2,
  },
  versionText: {
    fontSize: 14,
    color: '#666',
  },
  segmentContainer: {
    flexDirection: 'row',
    backgroundColor: '#1a1a1a',
    borderRadius: 8,
    padding: 2,
  },
  segmentItem: {
    paddingVertical: 6,
    paddingHorizontal: 10,
    borderRadius: 6,
  },
  segmentItemActive: {
    backgroundColor: '#6366F1',
  },
  segmentText: {
    fontSize: 12,
    color: '#888',
  },
  segmentTextActive: {
    color: '#fff',
    fontWeight: '600',
  },
});
