/**
 * @format
 */

import {AppRegistry} from 'react-native';
import TrackPlayer from 'react-native-track-player';
import App from './App';
import {name as appName} from './app.json';

// 注册应用
AppRegistry.registerComponent(appName, () => App);

// 注册播放服务（后台播放、通知栏控制）
TrackPlayer.registerPlaybackService(() => require('./src/services/playerService'));
