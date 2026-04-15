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

### Phase 3 - 本地音乐 ⏳
- [ ] 本地音乐扫描
- [ ] 本地音乐播放
- [ ] 外部音频打开

### Phase 4 - 视觉效果 ✅
- [x] 主题切换（默认/暗色/纯黑）
- [x] 歌词显示（同步/普通）
- [x] 迷你播放器
- [ ] 封面模糊背景

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
│   ├── hooks/                  # 自定义 Hooks
│   ├── navigation/             # 导航 ✅
│   │   └── AppNavigator.tsx
│   ├── screens/               # 页面 ✅
│   │   ├── HomeScreen.tsx
│   │   ├── SearchScreen.tsx
│   │   ├── PlayerScreen.tsx
│   │   ├── LibraryScreen.tsx
│   │   ├── PlaylistScreen.tsx
│   │   ├── PlaylistDetailScreen.tsx
│   │   ├── QueueScreen.tsx
│   │   ├── LyricsScreen.tsx
│   │   └── SettingsScreen.tsx
│   ├── services/              # 服务 ✅
│   │   └── playerService.ts   # Track Player 服务
│   ├── store/                 # 状态管理 ✅
│   │   └── musicStore.ts      # Zustand Store
│   ├── types/                 # 类型定义 ✅
│   │   └── index.ts
│   └── utils/                 # 工具函数
│       └── format.ts
├── android/                   # Android 原生
├── ios/                       # iOS 原生
└── index.js
```

---

## 📝 开发步骤

### Step 1: 项目初始化与依赖安装 ✅
**目标**: 确认现有 RN 项目结构，安装核心依赖

**Commit**: `chore: 项目初始化，核心依赖安装`

---

### Step 2: 类型定义与 API 层 ✅
**目标**: 建立 TypeScript 类型和 API 请求层

**Commit**: `feat: 类型定义与 API 层实现`

---

### Step 3: 状态管理 (Zustand Store) ✅
**目标**: 实现播放状态、收藏、歌单、设置的持久化

**Commit**: `feat: Zustand 状态管理完整实现`

---

### Step 4: 播放服务 (Track Player) ✅
**目标**: 集成 react-native-track-player，实现后台播放

**Commit**: `feat: 播放服务实现，支持后台播放`

---

### Step 5: 基础 UI 组件 ✅
**目标**: 实现通用 UI 组件

**Commit**: `feat: 基础 UI 组件实现`

---

### Step 6: 播放器页面 (PlayerScreen) ✅
**目标**: 全屏播放器页面

**Commit**: `feat: 播放器页面实现`

---

### Step 7: 首页 (HomeScreen) ✅
**目标**: 首页展示

**Commit**: `feat: 首页实现`

---

### Step 8: 搜索页面 (SearchScreen) ✅
**目标**: 歌曲搜索功能

**Commit**: `feat: 搜索页面实现`

---

### Step 9: 歌单页面 (PlaylistScreen) ✅
**目标**: 歌单管理

**Commit**: `feat: 歌单页面实现`

---

### Step 10: 歌词功能 ✅
**目标**: 歌词显示与同步

**Commit**: `feat: 歌词功能实现`

---

### Step 11: 本地音乐 ⏳
**目标**: 本地音乐扫描与播放

**状态**: 待实现

---

### Step 12: 设置页面 ✅
**目标**: 应用设置

**Commit**: `feat: 核心页面实现完成`

---

### Step 13: 主题与样式优化 ⏳
**目标**: 完善视觉体验

**状态**: 部分完成（设置页面支持主题切换）

---

### Step 14: 测试与打包 ⏳
**目标**: 可发布版本

**状态**: 待执行

---

## 🔗 API 端点 (与 Android 保持一致)
```
搜索: https://zm.wwoyun.cn/cloudsearch?keywords={keyword}
播放: https://api.qijieya.cn/meting/?id={songId}&type=song
歌词: https://zm.wwoyun.cn/lyric/new?id={songId}
```

## 🔗 参考资源

### React Native 音频
- [react-native-track-player](https://github.com/doublesymmetry/react-native-track-player) - 音频播放核心
- [LRCLIB API](https://lrclib.net) - 免费歌词 API

### 参考项目
- [itsOwn3r/rn-music-player](https://github.com/itsOwn3r/rn-music-player) - Expo + Track Player
- [kinshukkush/MUME-MUSIC-STREAMER](https://github.com/kinshukkush/MUME-MUSIC-STREAMER) - Expo 音乐播放器

### Android 原始实现
- API 实现: `/home/rao/code/FreeMusic/Android/app/src/main/java/com/freemusic/data/remote/api/`
- 播放服务: `/home/rao/code/FreeMusic/Android/app/src/main/java/com/freemusic/service/`
- UI 实现: `/home/rao/code/FreeMusic/Android/app/src/main/java/com/freemusic/presentation/ui/`

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
```bash
# Debug APK
cd android && ./gradlew assembleDebug

# Release APK (需要签名)
cd android && ./gradlew assembleRelease
```

---

## 📊 Git 提交历史

| Commit | 描述 |
|--------|------|
| `9e3ce43` | feat: 基础 UI 组件实现 |
| `c821c2a` | feat: 播放器页面实现 |
| `90d34a7` | feat: 首页实现 |
| `f0dbc76` | feat: 搜索页面实现 |
| `9ef6b1e` | feat: 歌单页面实现 |
| `00cef78` | feat: 核心页面实现完成 |

---

*最后更新: 2026-04-16*
