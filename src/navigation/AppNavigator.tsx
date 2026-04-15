/**
 * 应用导航配置
 */

import React from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';
import MaterialIcons from 'react-native-vector-icons/MaterialIcons';

// Screens
import HomeScreen from '../screens/HomeScreen';
import LibraryScreen from '../screens/LibraryScreen';
import SearchScreen from '../screens/SearchScreen';
import PlayerScreen from '../screens/PlayerScreen';
import PlaylistScreen from '../screens/PlaylistScreen';
import PlaylistDetailScreen from '../screens/PlaylistDetailScreen';
import QueueScreen from '../screens/QueueScreen';
import LyricsScreen from '../screens/LyricsScreen';
import SettingsScreen from '../screens/SettingsScreen';

// 类型定义
export type RootStackParamList = {
  Main: undefined;
  Player: undefined;
  Playlist: undefined;
  PlaylistDetail: {playlistId: string};
  Queue: undefined;
  Lyrics: undefined;
};

export type MainTabParamList = {
  Home: undefined;
  Library: undefined;
  Search: undefined;
  Settings: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator<MainTabParamList>();

function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={{
        tabBarActiveTintColor: '#6366F1',
        tabBarInactiveTintColor: '#666',
        tabBarStyle: {
          backgroundColor: '#1a1a1a',
          borderTopColor: '#333',
        },
        headerStyle: {
          backgroundColor: '#1a1a1a',
        },
        headerTintColor: '#fff',
      }}>
      <Tab.Screen
        name="Home"
        component={HomeScreen}
        options={{
          title: '首页',
          tabBarIcon: ({color, size}) => (
            <MaterialIcons name="home" size={size} color={color} />
          ),
        }}
      />
      <Tab.Screen
        name="Library"
        component={LibraryScreen}
        options={{
          title: '音乐库',
          tabBarIcon: ({color, size}) => (
            <MaterialIcons name="library-music" size={size} color={color} />
          ),
        }}
      />
      <Tab.Screen
        name="Search"
        component={SearchScreen}
        options={{
          title: '搜索',
          tabBarIcon: ({color, size}) => (
            <MaterialIcons name="search" size={size} color={color} />
          ),
        }}
      />
      <Tab.Screen
        name="Settings"
        component={SettingsScreen}
        options={{
          title: '设置',
          tabBarIcon: ({color, size}) => (
            <MaterialIcons name="settings" size={size} color={color} />
          ),
        }}
      />
    </Tab.Navigator>
  );
}

export default function AppNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator
        screenOptions={{
          headerStyle: {
            backgroundColor: '#1a1a1a',
          },
          headerTintColor: '#fff',
          contentStyle: {
            backgroundColor: '#1a1a1a',
          },
        }}>
        <Stack.Screen
          name="Main"
          component={MainTabs}
          options={{headerShown: false}}
        />
        <Stack.Screen
          name="Player"
          component={PlayerScreen}
          options={{
            headerShown: false,
            presentation: 'fullScreenModal',
          }}
        />
        <Stack.Screen
          name="Playlist"
          component={PlaylistScreen}
          options={{title: '我的歌单'}}
        />
        <Stack.Screen
          name="PlaylistDetail"
          component={PlaylistDetailScreen}
          options={{headerShown: false}}
        />
        <Stack.Screen
          name="Queue"
          component={QueueScreen}
          options={{title: '播放队列'}}
        />
        <Stack.Screen
          name="Lyrics"
          component={LyricsScreen}
          options={{
            title: '歌词',
            presentation: 'modal',
          }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
