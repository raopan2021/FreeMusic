# FreeMusic - Android 音乐播放器

一个基于 Jetpack Compose 的第三方网易云音乐 Android 客户端。

## 🎯 项目目标

- **核心功能**：搜索、播放网易云音乐，支持歌词同步显示
- **技术栈**：Kotlin + Jetpack Compose + MVVM + Media3
- **设计风格**：简约现代，类 Apple Music 风格

## 📚 功能规划

### P0 - MVP
- [ ] 歌曲搜索（网易云音乐搜索 API）
- [ ] 播放控制（播放/暂停/上下首）
- [ ] 进度条控制
- [ ] 专辑封面显示
- [ ] 后台播放 + 通知栏控制

### P1 - 完善
- [ ] 歌词显示（逐字同步）
- [ ] 搜索历史
- [ ] 收藏歌曲
- [ ] 迷你播放器

### P2 - 增强
- [ ] 歌单管理
- [ ] 播放历史
- [ ] 双语歌词
- [ ] 音效均衡器

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
