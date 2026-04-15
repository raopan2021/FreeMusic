import React, {useState} from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Switch,
  Alert,
  PermissionsAndroid,
  Platform,
} from 'react-native';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';
import {useMusicStore} from '../store/musicStore';

export default function SettingsScreen() {
  const {settings, updateSettings, setLocalSongs} = useMusicStore();

  const requestStoragePermission = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE,
          {
            title: '存储权限',
            message: 'FreeMusic需要访问您的音乐文件',
            buttonNeutral: '稍后询问',
            buttonPositive: '确定',
          },
        );
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          scanLocalMusic();
        } else {
          Alert.alert('权限被拒绝', '无法扫描本地音乐');
        }
      } catch (err) {
        console.warn(err);
      }
    }
  };

  const scanLocalMusic = async () => {
    // TODO: 实现本地音乐扫描
    // 这里需要使用 react-native-fs 来扫描媒体库
    Alert.alert('提示', '本地音乐扫描功能开发中');
  };

  const colors = [
    '#6366F1', // Indigo (默认)
    '#EC4899', // Pink
    '#10B981', // Green
    '#F59E0B', // Amber
    '#EF4444', // Red
    '#8B5CF6', // Purple
    '#06B6D4', // Cyan
  ];

  return (
    <ScrollView style={styles.container}>
      {/* 主题设置 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>外观</Text>

        <View style={styles.settingItem}>
          <Text style={styles.settingLabel}>深色主题</Text>
          <Switch
            value={settings.theme === 'dark'}
            onValueChange={(value) =>
              updateSettings({theme: value ? 'dark' : 'light'})
            }
            trackColor={{false: '#333', true: '#6366F1'}}
          />
        </View>

        <View style={styles.settingItem}>
          <Text style={styles.settingLabel}>主题颜色</Text>
          <View style={styles.colorPicker}>
            {colors.map((color) => (
              <TouchableOpacity
                key={color}
                style={[
                  styles.colorOption,
                  {backgroundColor: color},
                  settings.primaryColor === color && styles.colorSelected,
                ]}
                onPress={() => updateSettings({primaryColor: color})}
              />
            ))}
          </View>
        </View>

        <View style={styles.settingItem}>
          <Text style={styles.settingLabel}>歌词字体大小</Text>
          <View style={styles.sizeControl}>
            <TouchableOpacity
              style={styles.sizeButton}
              onPress={() =>
                updateSettings({
                  lyricsFontSize: Math.max(12, settings.lyricsFontSize - 2),
                })
              }>
              <MaterialIcons name="remove" size={20} color="#fff" />
            </TouchableOpacity>
            <Text style={styles.sizeValue}>{settings.lyricsFontSize}</Text>
            <TouchableOpacity
              style={styles.sizeButton}
              onPress={() =>
                updateSettings({
                  lyricsFontSize: Math.min(32, settings.lyricsFontSize + 2),
                })
              }>
              <MaterialIcons name="add" size={20} color="#fff" />
            </TouchableOpacity>
          </View>
        </View>
      </View>

      {/* 播放设置 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>播放</Text>

        <View style={styles.settingItem}>
          <Text style={styles.settingLabel}>自动播放</Text>
          <Switch
            value={settings.autoPlay}
            onValueChange={(value) => updateSettings({autoPlay: value})}
            trackColor={{false: '#333', true: '#6366F1'}}
          />
        </View>

        <View style={styles.settingItem}>
          <Text style={styles.settingLabel}>播放速度</Text>
          <View style={styles.speedPicker}>
            {[0.5, 0.75, 1.0, 1.25, 1.5, 2.0].map((speed) => (
              <TouchableOpacity
                key={speed}
                style={[
                  styles.speedOption,
                  settings.playbackSpeed === speed && styles.speedSelected,
                ]}
                onPress={() => updateSettings({playbackSpeed: speed})}>
                <Text
                  style={[
                    styles.speedText,
                    settings.playbackSpeed === speed && styles.speedTextSelected,
                  ]}>
                  {speed}x
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        <View style={styles.settingItem}>
          <Text style={styles.settingLabel}>跳过静音</Text>
          <Switch
            value={settings.skipSilence}
            onValueChange={(value) => updateSettings({skipSilence: value})}
            trackColor={{false: '#333', true: '#6366F1'}}
          />
        </View>
      </View>

      {/* 本地音乐 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>本地音乐</Text>

        <TouchableOpacity style={styles.button} onPress={requestStoragePermission}>
          <MaterialIcons name="refresh" size={24} color="#6366F1" />
          <Text style={styles.buttonText}>扫描本地音乐</Text>
        </TouchableOpacity>
      </View>

      {/* 关于 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>关于</Text>

        <View style={styles.aboutItem}>
          <Text style={styles.aboutLabel}>FreeMusic</Text>
          <Text style={styles.aboutValue}>版本 1.0.0</Text>
        </View>
        <View style={styles.aboutItem}>
          <Text style={styles.aboutLabel}>开发者</Text>
          <Text style={styles.aboutValue}>raopan</Text>
        </View>
      </View>

      <View style={styles.footer} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a1a',
  },
  section: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#333',
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#888',
    marginBottom: 16,
    textTransform: 'uppercase',
  },
  settingItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 12,
  },
  settingLabel: {
    fontSize: 14,
    color: '#fff',
  },
  colorPicker: {
    flexDirection: 'row',
  },
  colorOption: {
    width: 28,
    height: 28,
    borderRadius: 14,
    marginLeft: 8,
  },
  colorSelected: {
    borderWidth: 2,
    borderColor: '#fff',
  },
  sizeControl: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  sizeButton: {
    width: 32,
    height: 32,
    backgroundColor: '#333',
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
  },
  sizeValue: {
    fontSize: 14,
    color: '#fff',
    marginHorizontal: 16,
    minWidth: 30,
    textAlign: 'center',
  },
  speedPicker: {
    flexDirection: 'row',
  },
  speedOption: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: '#333',
    borderRadius: 4,
    marginLeft: 4,
  },
  speedSelected: {
    backgroundColor: '#6366F1',
  },
  speedText: {
    fontSize: 12,
    color: '#888',
  },
  speedTextSelected: {
    color: '#fff',
  },
  button: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    backgroundColor: '#2a2a2a',
    borderRadius: 8,
  },
  buttonText: {
    fontSize: 14,
    color: '#6366F1',
    marginLeft: 12,
  },
  aboutItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 8,
  },
  aboutLabel: {
    fontSize: 14,
    color: '#fff',
  },
  aboutValue: {
    fontSize: 14,
    color: '#888',
  },
  footer: {
    height: 100,
  },
});
