# FreeMusic RN - React Native 重构计划

> 基于 Android Kotlin 版本的 React Native 重构项目

## 📋 项目概述

- **目标**: 使用 React Native (TypeScript) 重构 FreeMusic Android 应用
- **参考项目**: `/home/rao/code/FreeMusic/Android` - Kotlin/Jetpack Compose 实现
- **技术栈**: React Native 0.85 + TypeScript + react-native-track-player + Zustand

## ✅ 已完成功能

### Phase 1 - 核心播放 (MVP) ✅
- [x] 歌曲搜索（网易云音乐 API）
- [x] 在线歌曲播放
- [x] 播放控制（播放/暂停/上下首）
- [x] 进度条控制
- [x] 后台播放 + 通知栏控制

### Phase 2 - 歌单与收藏 ✅
- [x] 收藏歌曲（我喜欢的音乐）
- [x] 创建/删除歌单
- [x] 歌单详情页
- [x] 播放队列
- [x] 搜索历史

### Phase 3 - 本地音乐 ✅
- [x] 本地音乐扫描框架
- [x] 权限处理
- [x] 本地音乐播放

### Phase 4 - 视觉效果 ✅
- [x] 主题切换（暗色/亮色/跟随系统）
- [x] 歌词显示（同步/普通）
- [x] 迷你播放器
- [x] 工具函数和主题 Hook

## 🛠 技术架构

### 目录结构
```
FreeMusicRN/
├── src/
│   ├── api/                    # API 请求 ✅
│   │   ├── netease.ts         # 网易云音乐 API
│   │   ├── lrclib.ts          # 歌词 API (LRCLIB)
│   │   └── index.ts
│   ├── components/             # 通用组件 ✅
│   │   ├── MiniPlayer.tsx
│   │   ├── SongItem.tsx
│   │   ├── ProgressBar.tsx
│   │   └── index.ts
│   ├── hooks/                  # 自定义 Hooks ✅
│   │   └── useTheme.ts         # 主题 Hook
│   ├── navigation/             # 导航 ✅
│   │   └── AppNavigator.tsx
│   ├── screens/               # 页面 ✅
│   │   ├── HomeScreen.tsx
│   │   ├── SearchScreen.tsx
│   │   ├── PlayerScreen.tsx
│   │   ├── LibraryScreen.tsx
│   │   ├── LocalMusicScreen.tsx
│   │   ├── PlaylistScreen.tsx
│   │   ├── PlaylistDetailScreen.tsx
│   │   ├── QueueScreen.tsx
│   │   ├── LyricsScreen.tsx
│   │   └── SettingsScreen.tsx
│   ├── services/              # 服务 ✅
│   │   ├── playerService.ts   # Track Player 服务
│   │   └── localMusicService.ts # 本地音乐服务
│   ├── store/                 # 状态管理 ✅
│   │   └── musicStore.ts      # Zustand Store
│   ├── types/                 # 类型定义 ✅
│   │   └── index.ts
│   └── utils/                 # 工具函数 ✅
│       └── format.ts
├── android/                   # Android 原生
├── ios/                       # iOS 原生
└── index.js
```

---

## 🔗 API 端点 (与 Android 保持一致)
```
搜索: https://zm.wwoyun.cn/cloudsearch?keywords={keyword}
播放: https://api.qijieya.cn/meting/?id={songId}&type=song
歌词: https://zm.wwoyun.cn/lyric/new?id={songId}
```

## 📦 依赖清单

```json
{
  "dependencies": {
    "react-native-track-player": "^4.1.1",
    "@react-native-async-storage/async-storage": "^1.23.1",
    "zustand": "^5.0.12",
    "react-native-vector-icons": "^10.3.0",
    "@react-navigation/native": "^7.2.2",
    "@react-navigation/bottom-tabs": "^7.15.9",
    "@react-navigation/native-stack": "^7.14.11",
    "react-native-gesture-handler": "^2.31.1",
    "react-native-safe-area-context": "^5.7.0",
    "react-native-screens": "^4.24.0"
  }
}
```

---

## 🚀 开发指南

### 快速开始
```bash
cd /home/rao/code/FreeMusic/FreeMusicRN

# 安装依赖
npm install

# 启动 Metro
npm start

# 运行 Android
npm run android
```

### 构建 APK

#### Debug APK
```bash
cd android
./gradlew assembleDebug
# 输出: android/app/build/outputs/apk/debug/app-debug.apk
```

#### Release APK (需要签名)
```bash
cd android
./gradlew assembleRelease
# 输出: android/app/build/outputs/apk/release/app-release.apk
```

### 添加新依赖
```bash
npm install <package>
cd android && ./gradlew clean
```

---

## 🔗 参考资源

### React Native 音频
- [react-native-track-player](https://github.com/doublesymmetry/react-native-track-player) - 音频播放核心
- [LRCLIB API](https://lrclib.net) - 免费歌词 API

### 参考项目
- [itsOwn3r/rn-music-player](https://github.com/itsOwn3r/rn-music-player) - Expo + Track Player
- [kinshukkush/MUME-MUSIC-STREAMER](https://github.com/kinshukkush/MUME-MUSIC-STREAMER) - Expo 音乐播放器

---
