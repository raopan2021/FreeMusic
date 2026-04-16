/**
 * @format
 */

import React, {useEffect, useState} from 'react';
import {StatusBar, View, ActivityIndicator, StyleSheet} from 'react-native';
import {SafeAreaProvider} from 'react-native-safe-area-context';
import {GestureHandlerRootView} from 'react-native-gesture-handler';
import AppNavigator from './src/navigation/AppNavigator';
import {useMusicStore} from './src/store/musicStore';
import {setupPlayer} from './src/services/playerService';

function App(): React.JSX.Element {
  const [isPlayerReady, setIsPlayerReady] = useState(false);
  const theme = useMusicStore(state => state.settings.theme);

  useEffect(() => {
    // 初始化播放器
    const initPlayer = async () => {
      try {
        await setupPlayer();
        setIsPlayerReady(true);
      } catch (error) {
        console.error('Failed to initialize player:', error);
        setIsPlayerReady(true); // 仍然允许应用运行
      }
    };

    initPlayer();
  }, []);

  if (!isPlayerReady) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator size="large" color="#6366F1" />
      </View>
    );
  }

  return (
    <GestureHandlerRootView style={styles.container}>
      <SafeAreaProvider>
        <StatusBar
          barStyle={theme === 'dark' ? 'light-content' : 'dark-content'}
          backgroundColor="#1a1a1a"
        />
        <AppNavigator />
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  loading: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#1a1a1a',
  },
});

export default App;
