# FreeMusic - Android 音乐播放器

一个基于 Jetpack Compose 的第三方网易云音乐 Android 客户端。

## 🎯 项目目标

- **核心功能**：搜索、播放网易云音乐，支持歌词同步显示
- **技术栈**：Kotlin + Jetpack Compose + MVVM + Media3
- **设计风格**：简约现代，类 Apple Music 风格

## ✨ 已实现功能

### 核心播放
- [x] 歌曲搜索（网易云音乐搜索 API）
- [x] 播放控制（播放/暂停/上下首）
- [x] 进度条控制（拖动SeekBar）
- [x] 专辑封面显示（多种样式：圆形、方形、菱形等）
- [x] 后台播放 + 通知栏控制
- [x] 歌词显示（逐字同步）
- [x] 迷你播放器

### 歌单与收藏
- [x] 搜索历史记录
- [x] 收藏歌曲（我喜欢的音乐）
- [x] 创建/删除歌单
- [x] 歌单详情页（添加/删除歌曲）
- [x] 播放队列

### 视觉效果
- [x] 粒子效果动画
- [x] 音频可视化器
- [x] 主题切换（默认/暗色/纯黑）
- [x] 均衡器预设（10种预设）

### 本地功能
- [x] 本地音乐扫描（MediaStore）
- [x] 从外部打开音频文件（mp3/flac/m4a等）
- [x] 导入歌单（通过链接）
- [x] 背景图片导入

### 其他
- [x] 分享功能（分享音乐到各平台）
- [x] 设置页面（粒子效果、可视化器、均衡器等）
- [x] 缓存清理

## 📋 功能规划

### 🔜 下一步开发
- [ ] 桌面组件 (Widget)
- [ ] 锁屏显示控制
- [ ] 灵动岛 (Dynamic Island) 支持
- [ ] 状态栏歌词
- [ ] 自定义主题色
- [ ] 歌单添加歌曲选择器

### ⚠️ 待解决/注意事项
- [ ] 版权合规 - 所有音乐来源需合法，避免侵权问题
- [ ] 接口稳定性 - 第三方代理可能不稳定，需准备备用方案
- [ ] 搜索闪退 - 偶发问题，需进一步调试
- [ ] 权限授予 - 部分设备权限弹窗可能不生效

### 📝 长期规划
- [ ] 听歌统计与每日推荐
- [ ] 歌词翻译（双语歌词）
- [ ] 睡眠定时器
- [ ] 蓝牙控制优化

## 🛠️ 技术栈

| 组件 | 技术 |
|------|------|
| 语言 | Kotlin 1.9+ |
| UI | Jetpack Compose |
| 架构 | MVVM + Clean Architecture |
| DI | Hilt |
| 网络 | Retrofit + OkHttp + Kotlin Serialization |
| 音频 | Media3 ExoPlayer |
| 本地存储 | Room |
| 图片加载 | Coil |
| 导航 | Compose Navigation |

## 📡 API 来源

本项目参考 [aura-music](https://github.com/dingyi222666/aura-music) 的 API 对接方案：

- 搜索/详情：`https://zm.wwoyun.cn/cloudsearch`（网易云音乐代理）
- 播放 URL：`https://api.qijieya.cn/meting/`（Meting 代理）
- 歌词：`https://zm.wwoyun.cn/lyric/new`

## ⚠️ 注意事项

1. **接口稳定性**：`zm.wwoyun.cn` / `api.qijieya.cn` 为第三方代理，可能不稳定
2. **版权限制**：网易云音乐有版权限制，部分歌曲可能无法播放
3. **频率限制**：搜索接口有 QPS 限制，避免频繁请求
4. **播放 URL 时效**：播放 URL 建议每次播放前重新获取
5. **CORS 问题**：所有接口均需通过代理访问

## 📂 目录结构

```
Android/
├── app/                    # 主应用模块
│   └── src/main/
│       ├── java/com/freemusic/
│       │   ├── data/       # 数据层
│       │   ├── domain/      # 领域层
│       │   ├── presentation/# 表现层
│       │   └── service/     # 播放服务
│       └── res/            # 资源文件
├── build.gradle.kts        # 根构建配置
└── settings.gradle.kts     # 项目设置
```

## 🔗 参考项目

- [aura-music](https://github.com/dingyi222666/aura-music) - Web 音乐播放器，API 方案参考
- [NetEaseCloudMusicApi](https://github.com/Binaryify/NeteaseCloudMusicApi) - 网易云音乐 Node.js API

## 📄 License

MIT
