# FreeMusic RN - React Native 重构计划

> 基于 Android Kotlin 版本的 React Native 重构项目

## 📋 项目概述

- **目标**: 使用 React Native (TypeScript) 重构 FreeMusic Android 应用
- **参考项目**: `/home/rao/code/FreeMusic/Android` - Kotlin/Jetpack Compose 实现
- **技术栈**: React Native 0.85 + TypeScript + react-native-track-player + Zustand

## 🎯 功能目标

### Phase 1 - 核心播放 (MVP)
- [ ] 歌曲搜索（网易云音乐 API）
- [ ] 在线歌曲播放
- [ ] 播放控制（播放/暂停/上下首）
- [ ] 进度条控制
- [ ] 后台播放 + 通知栏控制

### Phase 2 - 歌单与收藏
- [ ] 收藏歌曲（我喜欢的音乐）
- [ ] 创建/删除歌单
- [ ] 歌单详情页
- [ ] 播放队列
- [ ] 搜索历史

### Phase 3 - 本地音乐
- [ ] 本地音乐扫描
- [ ] 本地音乐播放
- [ ] 外部音频打开

### Phase 4 - 视觉效果
- [ ] 主题切换（默认/暗色/纯黑）
- [ ] 歌词显示（同步/普通）
- [ ] 迷你播放器
- [ ] 封面模糊背景

## 🛠 技术架构

### 目录结构
```
FreeMusicRN/
├── src/
│   ├── api/                    # API 请求
│   │   ├── netease.ts         # 网易云音乐 API
│   │   └── lrclib.ts          # 歌词 API (LRCLIB)
│   ├── components/             # 通用组件
│   │   ├── MiniPlayer.tsx
│   │   ├── SongItem.tsx
│   │   └── ProgressBar.tsx
│   ├── hooks/                  # 自定义 Hooks
│   │   └── usePlayer.ts
│   ├── navigation/             # 导航
│   │   └── AppNavigator.tsx
│   ├── screens/               # 页面
│   │   ├── HomeScreen.tsx
│   │   ├── SearchScreen.tsx
│   │   ├── PlayerScreen.tsx
│   │   ├── LibraryScreen.tsx
│   │   ├── PlaylistScreen.tsx
│   │   └── SettingsScreen.tsx
│   ├── services/              # 服务
│   │   └── playerService.ts   # Track Player 服务
│   ├── store/                 # 状态管理
│   │   └── musicStore.ts      # Zustand Store
│   ├── types/                 # 类型定义
│   │   └── index.ts
│   └── utils/                 # 工具函数
│       └── format.ts
├── android/                   # Android 原生
├── ios/                       # iOS 原生
└── index.js
```

### 状态管理 (Zustand)
```typescript
interface MusicStore {
  // 播放状态
  player: PlayerState;
  setCurrentSong: (song: Song | null) => void;
  setIsPlaying: (isPlaying: boolean) => void;
  // ...
}
```

### API 端点 (与 Android 保持一致)
```
搜索: https://zm.wwoyun.cn/cloudsearch?keywords={keyword}
播放: https://api.qijieya.cn/meting/?id={songId}&type=song
歌词: https://zm.wwoyun.cn/lyric/new?id={songId}
```

---

## 📝 开发步骤

### Step 1: 项目初始化与依赖安装 ✅
**目标**: 确认现有 RN 项目结构，安装核心依赖

**完成标准**:
- react-native-track-player 已安装
- zustand + AsyncStorage 已安装
- react-native-vector-icons 已安装
- 基础项目可运行

**Commit**: `chore: 项目初始化，核心依赖安装`

---

### Step 2: 类型定义与 API 层
**目标**: 建立 TypeScript 类型和 API 请求层

**完成标准**:
- `src/types/index.ts` - Song, Playlist, PlayerState 类型
- `src/api/netease.ts` - 网易云音乐搜索/播放 API
- `src/api/lrclib.ts` - LRCLIB 歌词 API
- API 请求可正常获取数据

**Commit**: `feat: 类型定义与 API 层实现`

---

### Step 3: 状态管理 (Zustand Store)
**目标**: 实现播放状态、收藏、歌单、设置的持久化

**完成标准**:
- `src/store/musicStore.ts` 完整实现
- 播放状态持久化
- 歌单/收藏持久化
- 设置持久化

**Commit**: `feat: Zustand 状态管理实现`

---

### Step 4: 播放服务 (Track Player)
**目标**: 集成 react-native-track-player，实现后台播放

**完成标准**:
- `src/services/playerService.ts` - Track Player 配置
- 后台播放正常
- 通知栏控制正常
- 蓝牙/耳机控制正常

**Commit**: `feat: 播放服务实现，支持后台播放`

---

### Step 5: 基础 UI 组件
**目标**: 实现通用 UI 组件

**完成标准**:
- `SongItem.tsx` - 歌曲列表项
- `ProgressBar.tsx` - 进度条
- `MiniPlayer.tsx` - 迷你播放器
- 组件可复用、主题适配

**Commit**: `feat: 基础 UI 组件实现`

---

### Step 6: 播放器页面 (PlayerScreen)
**目标**: 全屏播放器页面

**完成标准**:
- 封面展示
- 歌曲信息显示
- 播放控制（上一首/播放暂停/下一首）
- 进度条 + 时间显示
- 收藏/队列/歌词按钮

**Commit**: `feat: 播放器页面实现`

---

### Step 7: 首页 (HomeScreen)
**目标**: 首页展示

**完成标准**:
- 最近播放歌曲
- 本地音乐入口
- 快速播放控制

**Commit**: `feat: 首页实现`

---

### Step 8: 搜索页面 (SearchScreen)
**目标**: 歌曲搜索功能

**完成标准**:
- 搜索输入
- 搜索历史
- 搜索结果展示
- 点击播放

**Commit**: `feat: 搜索页面实现`

---

### Step 9: 歌单页面 (PlaylistScreen)
**目标**: 歌单管理

**完成标准**:
- 歌单列表
- 歌单详情
- 创建/删除歌单
- 添加/移除歌曲

**Commit**: `feat: 歌单页面实现`

---

### Step 10: 歌词功能
**目标**: 歌词显示与同步

**完成标准**:
- 普通歌词显示
- 同步歌词解析与滚动
- 歌词 API 集成

**Commit**: `feat: 歌词功能实现`

---

### Step 11: 本地音乐
**目标**: 本地音乐扫描与播放

**完成标准**:
- MediaStore 扫描
- 本地文件播放
- 权限处理

**Commit**: `feat: 本地音乐功能实现`

---

### Step 12: 设置页面
**目标**: 应用设置

**完成标准**:
- 主题切换
- 播放速度设置
- 睡眠定时器
- 关于页面

**Commit**: `feat: 设置页面实现`

---

### Step 13: 主题与样式优化
**目标**: 完善视觉体验

**完成标准**:
- 暗色/纯黑/亮色主题
- 封面模糊背景
- 动画效果

**Commit**: `feat: 主题与样式优化`

---

### Step 14: 测试与打包
**目标**: 可发布版本

**完成标准**:
- Debug APK 可正常安装
- Release APK 可正常构建
- 基本功能测试通过

**Commit**: `chore: 测试打包，准备发布`

---

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

### 添加新依赖
```bash
npm install <package>
cd android && ./gradlew clean
```

### 代码规范
- 使用 TypeScript strict 模式
- 组件使用 `.tsx` 扩展名
- 工具函数使用 `.ts` 扩展名
- 遵循 React Native 社区编码规范

---

*最后更新: 2026-04-16*
