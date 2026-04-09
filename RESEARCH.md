# FreeMusic 项目研究文档

> 研究日期: 2026-04-10
> 研究目标: 为 FreeMusic 项目寻找替代音乐 API 和参考开源项目

---

## 1. 替代音乐 API 研究

### 1.1 Deezer API ⭐ 推荐

**文档**: https://developers.deezer.com/api

**特点**:
- **无需 API Key** 即可进行基础调用（搜索、30秒预览）
- 返回丰富的元数据：专辑封面、艺术家信息、时长、歌词状态等
- 有 30 秒音频预览 URL（可播放）
- 支持按曲名、艺术家、专辑搜索
- 免费，无需申请

**限制**:
- 无需 Key，但有隐式 rate limit
- 30 秒预览版非完整歌曲（但可用于播放）

**示例请求**:
```bash
# 搜索曲目
curl "https://api.deezer.com/search/track?q=the%20weeknd%20blinding%20lights&limit=1"

# 搜索专辑
curl "https://api.deezer.com/search/album?q=after%20hours&limit=1"

# 获取艺术家热门曲目
curl "https://api.deezer.com/artist/4050205/top?limit=5"
```

**示例响应字段**:
```json
{
  "id": 908604612,
  "title": "Blinding Lights",
  "duration": 200,
  "preview": "https://cdnt-preview.dzcdn.net/api/1/1/1/b/2/0/...mp3",
  "artist": { "name": "The Weeknd", "picture_medium": "..." },
  "album": { "title": "After Hours", "cover_medium": "..." }
}
```

**Pros**: 开箱即用，数据丰富，有预览音频
**Cons**: 完整播放需要 Deezer 账号；非真正的自由音乐（商业版权）

---

### 1.2 LRCLIB (歌词 API) ⭐ 推荐

**文档**: https://lrclib.net (网页) | **GitHub**: https://github.com/tranxuanthang/lrclib

**特点**:
- **完全免费开源**，无需 API Key
- 提供**同步歌词（LRC格式）** 和普通歌词
- 数据库庞大，涵盖大量流行歌曲
- 自托管友好（开源可自行部署）
- 无明显 rate limit

**API 端点**:
```
GET /api/search?artist_name={artist}&track_name={track}
GET /api/get?artist_name={artist}&track_name={track}&duration={duration}
```

**实测验证**（2026-04-10）:
```bash
curl "https://lrclib.net/api/search?artist_name=The%20Weeknd&track_name=Blinding%20Lights"
```
返回 20+ 个匹配结果，包含完整的 `[00:13.42]` 格式同步歌词。

**示例响应**:
```json
{
  "id": 390,
  "name": "Blinding Lights",
  "trackName": "Blinding Lights",
  "artistName": "The Weeknd",
  "albumName": "After Hours",
  "duration": 200.0,
  "plainLyrics": "Yeah\n\nI've been tryna call...",
  "syncedLyrics": "[00:13.42] Yeah\n[00:14.81] \n[00:26.95] I've been tryna call..."
}
```

**Pros**: 完全免费开源，无需 key，歌词质量高，支持同步歌词
**Cons**: 仅歌词，不含音频

---

### 1.3 Spotify Web API

**文档**: https://developer.spotify.com/documentation/web-api

**特点**:
- 全球最大音乐目录
- 完整的艺术家、专辑、曲目元数据
- 推荐算法强大
- OAuth 2.0 认证

**限制**:
- **免费用户无法播放完整歌曲** — Web API 在免费层仅提供 30 秒预览
- 需要 Client ID + Secret（免费注册）
- 播放控制（Spotify Connect）需要 Premium
- 有 rate limit

**可用功能（免费层）**:
- 搜索音乐目录
- 获取曲目/专辑/艺术家元数据
- 获取用户播放列表
- 获取推荐

**不可用（需要 Premium）**:
- 完整音频流播放
- 高音质播放

**示例**:
```kotlin
// Spotify Web API (Kotlin coroutines)
val token = getSpotifyToken(clientId, clientSecret)
val response = httpClient.get("https://api.spotify.com/v1/search") {
    header("Authorization", "Bearer $token")
    parameter("q", "blinding lights")
    parameter("type", "track")
    parameter("limit", 1)
}
```

**Pros**: 曲库最全，数据质量高
**Cons**: 免费层无法播放完整歌曲（与 Netease 相比差距大）

---

### 1.4 SoundCloud API

**文档**: https://developers.soundcloud.com/docs/api/guide

**特点**:
- 大量独立音乐人作品，很多可自由使用
- 支持上传、播放、点赞、播放列表
- OAuth 2.0 认证
- 嵌入式播放器 Widget

**限制**:
- 2024 年更新了 API，很多旧文档可能过时
- 需要注册 Developer Account 获取 Client ID
- 部分功能需要付费订阅

**示例**:
```bash
curl "https://api.soundcloud.com/tracks?q=chill&limit=5&client_id=YOUR_CLIENT_ID"
```

**Pros**: 独立音乐多，可发现性好
**Cons**: 官方 API 文档更新较乱，需要申请 key

---

### 1.5 Genius API

**文档**: https://docs.genius.com/

**特点**:
- 全球最大众包歌词平台
- 提供歌词、注释、艺术家信息
- 有 Python 库 `lyricsgenius` 可用

**限制**:
- 需要免费 API Token
- 不直接提供音频（仅歌词）
- 很多歌词需要网页爬取（API 不直接暴露）

**示例**:
```python
import lyricsgenius
genius = lyricsgenius.Genius("YOUR_ACCESS_TOKEN")
song = genius.search_song("Blinding Lights", "The Weeknd")
print(song.lyrics)
```

**Pros**: 歌词库最全，注释丰富
**Cons**: 非音乐播放 API，仅歌词；需要 token

---

### 1.6 Jamendo API

**文档**: https://developer.jamendo.com/

**特点**:
- 专注**免版税音乐**（Royalty-Free）
- 音乐人可免费上传分发
- 适合需要合法免费背景音乐的应用

**限制**:
- 需要 Client ID（免费注册）
- 免费层有 API 调用频率限制
- 音乐主要用于个人/商业背景音乐，非流行歌曲

**注册**: https://developer.jamendo.com/join

---

### 1.7 其他备选 API

| API | 特点 | 限制 |
|-----|------|------|
| **YouTube Music Data API** | 曲目数据丰富 | 需要 Google Cloud Project，官方无开放 API |
| **Musixmatch** | 大型歌词库 | 免费层只能获取部分歌词，商用需付费 |
| **Freesound API** | 音效/采样为主 | 不适合流行音乐 |
| **Audiomack API** | 独立音乐为主 | 文档不完善 |

---

## 2. 开源 Android 音乐播放器项目参考

### 2.1 项目总览

| 项目 | GitHub Stars | 语言 | 特点 | 最后更新 |
|------|-------------|------|------|---------|
| [RetroMusicPlayer](https://github.com/RetroMusicPlayer/RetroMusicPlayer) | **5,066** | Kotlin | YouTube Music 后端，Material 3 | 2026-04-09 |
| [OuterTune](https://github.com/OuterTune/OuterTune) | **4,928** | Kotlin | YouTube Music fork, Jetpack Compose | 2026-04-09 |
| [PixelPlayer](https://github.com/theovilardo/PixelPlayer) | **3,876** | Kotlin | 本地音乐，Jetpack Compose, Material 3 | 2026-04-05 |
| [Auxio](https://github.com/OxygenCobalt/Auxio) | **3,700** | Kotlin | 本地音乐，ExoPlayer，极简设计 | 2026-04-09 |
| [Gramophone](https://github.com/FoedusProgramme/Gramophone) | **2,001** | Kotlin | Spotify + 本地音乐 | 2026-04-09 |
| [Music-Player-GO](https://github.com/enricocid/Music-Player-GO) | **1,886** | Kotlin | 本地音乐，极简, Material Design | 2026-04-09 |
| [APlayer](https://github.com/rRemix/APlayer) | **1,718** | Kotlin | 网易云音乐 API | 2026-03-30 |
| [Symphony](https://github.com/zyrouge/symphony) | **1,652** | Kotlin | 现代 UI，支持 YouTube Music | 2026-03-30 |
| [TimberX](https://github.com/naman14/TimberX) | **1,542** | Kotlin | 简洁，支持多源 | 2026-03-30 |
| [VLC Android](https://github.com/videolan/vlc-android) | **~10k+** | Java/C++ | 万能播放器，codec 全支持 | 活跃 |

---

### 2.2 重点推荐项目详解

#### Auxio (⭐ 强烈推荐参考 UI/架构)
- **Stars**: 3,700 | **License**: GPL-3.0
- **GitHub**: https://github.com/OxygenCobalt/Auxio
- **技术栈**: Kotlin, Jetpack Compose, Media3 (ExoPlayer), Material 3
- **特点**:
  - 极简设计哲学，只做音乐播放
  - 基于 Media3 ExoPlayer，播放质量高
  - 快速响应的 UI
  - 仅本地音乐，无广告
  - 代码结构清晰，适合学习参考
- **适合 FreeMusic 的点**: UI 设计、架构模式、ExoPlayer 集成
- **状态**: 活跃开发中

#### OuterTune (⭐ 推荐参考流媒体实现)
- **Stars**: 4,928 | **License**: GPL-3.0
- **GitHub**: https://github.com/OuterTune/OuterTune
- **技术栈**: Kotlin, Jetpack Compose, YouTube Music backend (InnerTune)
- **特点**:
  - YouTube Music 作为音频源（可播放完整歌曲）
  - Material 3 设计语言
  - 搜索、播放列表、歌手页面完整
  - 开源但依赖 YouTube Music 私有 API
- **适合 FreeMusic 的点**: 流媒体播放架构、YouTube Music 集成方式
- **状态**: 活跃开发中（v4.0.0 刚发布）

#### RetroMusicPlayer (⭐ 成熟项目参考)
- **Stars**: 5,066 | **License**: GPL-3.0
- **GitHub**: https://github.com/RetroMusicPlayer/RetroMusicPlayer
- **技术栈**: Kotlin, Jetpack Compose, YouTube Music backend
- **特点**:
  - 功能最完整的 YouTube Music 播放器之一
  - 支持歌词（部分）、播放列表、收藏
  - 暗黑模式、封面动画、均衡器
- **适合 FreeMusic 的点**: 完整功能集、UI 丰富度
- **状态**: 活跃

#### VLC Android (通用播放器参考)
- **Stars**: 10k+ | **License**: LGPL-2.1 / GPL-2.0
- **GitHub**: https://github.com/videolan/vlc-android
- **技术栈**: Java/C++, libVLC
- **特点**:
  - 支持几乎所有音频格式
  - 网络流媒体播放
  - 均衡器、声音效果
  - 活跃维护（2026年3月刚发布 0.13.0）
- **适合 FreeMusic 的点**: 音频解码、均衡器实现
- **状态**: 非常活跃

#### Gramophone
- **Stars**: 2,001 | **License**: GPL-3.0
- **GitHub**: https://github.com/FoedusProgramme/Gramophone
- **技术栈**: Kotlin
- **特点**: 支持 Spotify 和本地音乐
- **适合 FreeMusic 的点**: 多源音乐整合架构

---

### 2.3 Orthrus-Android

搜索结果**未找到** "Orthrus-Android" 项目。该名称可能是虚构的或已被重命名/下线的项目。

建议搜索关键词:
- `Orthrus music android github` （如项目存在会有结果）

---

## 3. 歌词 API 详细对比

### 3.1 LRCLIB（强烈推荐）

| 项目 | 详情 |
|-----|------|
| **API 文档** | https://lrclib.net |
| **GitHub** | https://github.com/tranxuanthang/lrclib |
| **是否开源** | ✅ 完全开源 (MIT) |
| **是否需要 Key** | ❌ 不需要 |
| **是否免费** | ✅ 完全免费（可自托管） |
| **歌词格式** | 同步歌词(LRC) + 普通歌词 |
| **曲库规模** | 大量流行歌曲 |
| **Rate Limit** | 无明确限制 |

**API 端点**:
```
GET https://lrclib.net/api/search?artist_name={name}&track_name={name}
GET https://lrclib.net/api/get?artist_name={name}&track_name={name}&duration={s}
```

**Flutter/Dart 示例**:
```dart
final response = await http.get(
  Uri.parse('https://lrclib.net/api/search?artist_name=The%20Weeknd&track_name=Blinding%20Lights'),
);
final data = jsonDecode(response.body);
// data[0]['syncedLyrics'] → 同步歌词
// data[0]['plainLyrics'] → 普通歌词
```

**Kotlin 示例**:
```kotlin
suspend fun getLyrics(artist: String, track: String): LyricsResult? {
    val url = "https://lrclib.net/api/search?artist_name=$artist&track_name=$track"
    return json.decodeFromBytes<LyricsResult>(httpClient.get(url).body())
}
```

---

### 3.2 Genius API

| 项目 | 详情 |
|-----|------|
| **文档** | https://docs.genius.com/ |
| **是否开源** | ❌ 官方 API，闭源 |
| **是否需要 Key** | ✅ 需要 free API token |
| **免费限制** | 每小时 120 次请求 |
| **歌词格式** | 普通文本（需解析） |

**注册**: https://genius.com/api-clients

**Kotlin 示例**:
```kotlin
// 需要 genius API token
val response = httpClient.get("https://api.genius.com/search") {
    header("Authorization", "Bearer $GENIUS_ACCESS_TOKEN")
    parameter("q", "$trackName $artistName")
}
```

---

### 3.3 Musixmatch

| 项目 | 详情 |
|-----|------|
| **文档** | https://developer.musixmatch.com/ |
| **是否需要 Key** | ✅ 需要 API Key |
| **免费限制** | 歌词不可直接用于商业应用，需付费 |
| **歌词格式** | 同步歌词 + 普通歌词 |

> ⚠️ Musixmatch 免费层有严格使用限制，商用可能侵权，不推荐作为主要歌词源。

---

## 4. UI/UX 参考

### 4.1 设计风格推荐

基于上述开源项目的 UI 分析，推荐以下设计方向：

1. **Material Design 3 (Material You)**
   - 参考: Auxio, OuterTune, RetroMusicPlayer
   - 动态配色（基于专辑封面）
   - 现代卡片式布局

2. **极简主义**
   - 参考: Auxio
   - 专注于内容和播放控制
   - 无多余功能入口

3. **全屏播放器 + 迷你播放器**
   - 参考: 大多数现代 Android 音乐播放器
   - 下滑切换迷你播放器

### 4.2 UI 参考资源

| 资源 | 链接 |
|-----|------|
| **Auxio F-Droid** | https://f-droid.org/packages/org.oxycblt.auxio/ |
| **Auxio 截图** | 项目 GitHub README |
| **Dribbble - Music Player** | https://dribbble.com/search/music-player?i=0&q=music+player+app |
| **Dribbble - Material Music** | https://dribbble.com/search/material+music+player |
| **Behance - Music App Design** | https://www.behance.net/search/projects?search=music+player+app |
| **Material Design 3 Gallery** | https://m3.material.io/ |

### 4.3 关键 UI 组件建议

```
┌─────────────────────────────┐
│  ← 返回        ⋮ 更多       │  ← 导航栏
├─────────────────────────────┤
│                             │
│     ┌───────────────┐      │
│     │   专辑封面    │      │  ← 大面积封面展示
│     │   (圆角)      │      │
│     └───────────────┘      │
│                             │
│  歌曲名称 (大标题)          │
│  艺术家名 (副标题)          │
│                             │
│  ○────────●─────○ 3:21    │  ← 进度条 + 时间
│                             │
│    ⏮    ▶/⏸    ⏭        │  ← 播放控制
│                             │
│  🔀    🔁    ❤️    ⋮      │  ← 功能按钮
└─────────────────────────────┘
```

---

## 5. 综合建议

### API 选择建议

| 场景 | 推荐 API | 原因 |
|-----|---------|------|
| 音频搜索 + 30秒预览 | **Deeer API** | 无需 Key，开箱即用 |
| 完整歌词（同步） | **LRCLIB** | 完全免费开源，无需 Key |
| 全曲库搜索 + 元数据 | **Spotify Web API** | 曲库最全，需注册（免费 Key） |
| 独立/免版权音乐 | **SoundCloud / Jamendo** | 独立音乐多 |
| 歌词 + 注释 | **Genius API** | 需要免费 Token |

### 推荐组合

**方案 A（最简）**:
- 音频: Deezer API（无需 Key）
- 歌词: LRCLIB（无需 Key）
- 成本: 0

**方案 B（完整）**:
- 音频: Spotify Web API（注册免费）
- 歌词: LRCLIB + Genius API 组合
- 成本: 0（开发者免费层）

**方案 C（流媒体 + YouTube）**:
- 参考 OuterTune/RetroMusicPlayer 架构
- YouTube Music 作为音频源
- LRCLIB 歌词

### 开源项目参考优先级

1. **Auxio** → UI 设计 + 架构模式 + ExoPlayer 集成
2. **OuterTune** → 流媒体播放 + 多线程架构
3. **RetroMusicPlayer** → 完整功能参考
4. **VLC Android** → 音频解码 + 均衡器

---

## 6. 附录

### A. GitHub 搜索技巧

```bash
# 搜索 Kotlin 音乐播放器（按 stars 排序）
https://github.com/search?q=music+player+language:kotlin&type=repositories&sort=stars

# 搜索 Jetpack Compose 音乐播放器
https://github.com/search?q=music+player+jetpack+compose&type=repositories
```

### B. 相关技术栈建议

| 组件 | 推荐方案 |
|-----|---------|
| 音频播放 | Media3 ExoPlayer (AndroidX) |
| 歌词解析 | 自解析 LRC 格式 或 lrclib-net |
| 网络请求 | Retrofit + OkHttp + Kotlin Coroutines |
| 状态管理 | ViewModel + StateFlow |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt 或 Koin |
| 本地存储 | Room Database |
| 图片加载 | Coil (Compose 友好) |

### C. 相关资源链接

- Media3 ExoPlayer: https://developer.android.com/guide/topics/media/media3/exoplayer
- LRCLIB GitHub: https://github.com/tranxuanthang/lrclib
- Deezer Developer: https://developers.deezer.com/
- Spotify Developer: https://developer.spotify.com/
- SoundCloud Developers: https://developers.soundcloud.com/
- Genius Developers: https://docs.genius.com/

---

## 7. YouTube Music 相关项目（补充流媒体播放方案）

> 以下项目均为**无需 Premium 账号**即可播放完整歌曲的开源 Android 项目，基于 YouTube/YouTube Music 非官方 API 实现。

### 7.1 InnerTune

- **Stars**: 估计 3,000+（活跃开发中）
- **GitHub**: https://github.com/z-huang/InnerTune
- **License**: GPL-3.0
- **语言**: Kotlin, Jetpack Compose
- **特点**:
  - Material 3 设计语言
  - YouTube Music 作为音频源（无需 Premium）
  - 搜索、播放列表、收藏、歌手页面完整
  - 后台播放、通知栏控制
  - 引用 ViMusic 作为 Compose 学习示例
- **适合 FreeMusic 的点**: 流媒体播放架构、YouTube Music 私有 API 调用方式
- **状态**: 活跃开发

### 7.2 ViMusic

- **GitHub**: https://github.com/vfsfitvnm/ViMusic
- **License**: AGPL-3.0
- **语言**: Kotlin, Jetpack Compose
- **特点**:
  - 被多个项目（InnerTune、OuterTune）引用为参考的早期 Compose 音乐播放器
  - YouTube Music 非官方 API
  - 简洁架构，适合学习 Compose + ExoPlayer 集成
- **状态**: 维护中（2024 年有更新）

### 7.3 SimpMusic

- **F-Droid**: https://f-droid.org/packages/com.maxrave.simpmusic/
- **GitHub**: https://github.com/maxrava/SimpMusic
- **语言**: Kotlin, Jetpack Compose
- **特点**:
  - YouTube Music + YouTube 免费播放（无广告）
  - 后台播放
  - Home/Charts/Podcast/Moods 浏览
  - 1080p 视频选项
  - 播放数据统计 + 自定义播放列表
  - Spotify Canvas 支持
- **状态**: 活跃开发

### 7.4 Spotube

- **GitHub**: https://github.com/KiokoBui/Spotube
- **语言**: Kotlin
- **特点**:
  - Spotify 歌曲直接通过 YouTube 播放（无需 Premium）
  - 开源跨平台
- **状态**: 建议核实最新仓库地址

---

## 8. QQ Music / 咪咕音乐 API（非官方）

### 8.1 QQ Music 非官方 API（推荐自部署）

| 项目 | 地址 | 备注 |
|-----|------|------|
| **qq-music-api (copws)** | https://github.com/copws/qq-music-api | Koa2 开发，接口丰富 |
| **QQ音乐 API (Cloudflare Workers)** | https://doc.ygking.top/ | 免费部署，全球加速 |
| **QQ音乐 API (sansenjian)** | https://sansenjian.github.io/qq-music-api/api/ | 完整接口文档 |
| **QQ音乐官方开放平台** | https://developer.y.qq.com/docs/openapi | 需申请，需企业资质 |

**非官方 API 能力（以 copws/qq-music-api 为例）**:
```bash
# 搜索歌曲
GET /search?key=周杰伦
# 获取播放链接
GET /song/{songId}/url
# 获取歌词
GET /lyric/{songId}
# 获取专辑信息
GET /album/{albumId}
```

**Pros**: 中文歌曲最全（周杰伦、林俊杰等华语歌手）；有官方和非官方两种方案
**Cons**: 
- 官方 API 需要企业资质，个人开发者难以申请
- 非官方 API 有被封禁风险，需自部署
- 咪咕音乐暂无成熟的非官方 API

### 8.2 咪咕音乐（补充）

- **官网**: https://music.migu.cn/
- **官方开发者平台**: 未找到公开的个人开发者 API
- **非官方方案**: 咪咕音乐暂无成熟的非官方 API，建议参考 QQ Music 或使用 Deezer/Spotify 作为中文音乐补充

**结论**: 华语音乐建议以 **QQ Music 非官方 API（自部署）** 作为后端，配合 Deezer/Spotify 补全国际曲目。

---

## 9. lyrics.ovh API（免费歌词 API）

**文档**: https://lyrics.ovh/

**特点**:
- 完全免费，**无需 API Key**
- 极简 REST API
- 支持 artist/song 直接查询

**端点**:
```
GET https://api.lyrics.ovh/v1/{artist}/{title}
```

**示例请求**:
```bash
curl "https://api.lyrics.ovh/v1/the%20weeknd/blinding%20lights"
```

**示例响应**:
```json
{
  "lyrics": "Yeah\n\nI've been tryna call\nI've been on my own for long enough..."
}
```

**Pros**: 零门槛，免费，无需注册
**Cons**: 
- **仅普通歌词（非同步歌词）** — 无时间轴
- 曲库相对较小，不如 LRCLIB 全
- 部分歌曲可能缺失

**对比 LRCLIB**:

| 特性 | lyrics.ovh | LRCLIB |
|-----|-----------|--------|
| 是否需要 Key | ❌ 否 | ❌ 否 |
| 是否免费 | ✅ 是 | ✅ 是 |
| 同步歌词 | ❌ 否 | ✅ 是 |
| 曲库规模 | 中等 | 较大 |
| 开源 | ❌ 否 | ✅ 是 |

**推荐**: 主要用 **LRCLIB**（同步歌词），备选 **lyrics.ovh**（普通歌词兜底）

---

## 10. Orthrus-Android 调查结果

**结论**: 未找到名为 "Orthrus-Android" 或 "Orthrus" 的活跃开源 Android 音乐播放器项目。

可能的情况:
1. 项目已被重命名（可能是 InnerTune/OuterTune 的前身）
2. 项目已下线
3. 名称拼写有误（可能是 "Orchis" 或其他类似名称）

**建议**: 如果需要类似项目，推荐参考 **Auxio**（极简本地音乐）或 **InnerTune/OuterTune**（YouTube Music 流媒体）。

---

## 11. UI/UX 设计趋势（音乐播放器 2024-2025）

基于对 Dribbble、Behance 及上述开源项目分析：

### 11.1 主流设计模式

1. **全屏播放器 → 迷你播放器**
   - 上滑/下滑切换
   - 底部进度条始终可见
   - 专辑封面模糊背景

2. **Material Design 3 (Material You)**
   - 动态配色（从专辑封面提取主色）
   - 参考: Auxio, OuterTune, InnerTune
   - 圆角卡片 + 大面积留白

3. **极简 vs 丰富功能**
   - 极简派: Auxio（无广告、无社交、专注播放）
   - 功能派: RetroMusicPlayer（均衡器、歌词、封面动画）

### 11.2 关键 UI 组件布局

```
┌──────────────────────────────────────┐
│  ← Back            ⋮ Menu    ❤️ Save │  导航栏 + 收藏
├──────────────────────────────────────┤
│                                      │
│         ╭────────────────╮          │
│         │                │          │
│         │   专辑封面      │          │  大面积封面，圆角+阴影
│         │   (圆角/阴影)   │          │
│         │                │          │
│         ╰────────────────╯          │
│                                      │
│     歌曲名称 (大标题, 18-22sp)        │
│     艺术家名 (副标题, 14-16sp)        │
│                                      │
│  0:00  ═══════════●═══════  3:21   │  进度条
│                                      │
│       ⏮      ▶/⏸      ⏭           │  播放控制，居中大按钮
│                                      │
│   🔀 Shuffle  🔁 Repeat  📋 Queue  │  功能按钮
│                                      │
│         ════════════════           │  迷你歌词预览(可选)
└──────────────────────────────────────┘
```

### 11.3 Jetpack Compose 音乐播放器 UI 参考

| 项目 | GitHub | Stars | 特点 | 适合参考 |
|-----|--------|-------|------|---------|
| **OmarNofal/Material-3-Music-Player** | https://github.com/OmarNofal/Material-3-Music-Player | 67 | 纯离线播放器, Material 3 | 基础 UI 组件布局 |
| **Auxio** | https://github.com/OxygenCobalt/Auxio | 3,700 | 极简, Compose, ExoPlayer | UI 架构 + 状态管理 |
| **InnerTune** | https://github.com/z-huang/InnerTune | 3,000+ | Material 3, YouTube Music | 列表/搜索 UI |
| **OuterTune** | https://github.com/OuterTune/OuterTune | 4,928 | Material 3, YouTube Music | 完整功能参考 |

### 11.4 UI 设计资源

- **Dribbble 搜索**: https://dribbble.com/search/music-player?q=music+player+app
- **Dribbble Material Music**: https://dribbble.com/search/material+music+player
- **Auxio F-Droid**: https://f-droid.org/packages/org.oxycblt.auxio/
- **Material Design 3**: https://m3.material.io/

### 11.5 UI 设计趋势关键词

- `music player android ui` → 通用播放器设计
- `jetpack compose music app` → Compose 实现参考
- `spotify clone ui` → 流媒体播放器设计
- `material you music player` → Material 3 设计趋势
- `dark mode music app` → 暗黑模式设计

---

## 12. 综合 API + 开源项目推荐（最终推荐）

### 音频 API 优先级

| 优先级 | API | 原因 | 是否需要 Key |
|-------|-----|------|-------------|
| 1 | **Deezer API** | 无需 Key，开箱即用，30秒预览 | ❌ |
| 2 | **Spotify Web API** | 曲库最全，元数据丰富 | ✅ (免费) |
| 3 | **QQ Music 非官方 API** | 华语歌曲最全，需自部署 | ❌ |
| 4 | **YouTube Music (InnerTune 架构)** | 可免费播放完整歌曲 | ❌ (非官方 API) |
| 5 | **SoundCloud API** | 独立音乐多 | ✅ |
| 6 | **Jamendo API** | 免版税音乐 | ✅ |

### 歌词 API 优先级

| 优先级 | API | 原因 | 是否需要 Key |
|-------|-----|------|-------------|
| 1 | **LRCLIB** | 同步歌词，免费开源，无需 Key | ❌ |
| 2 | **lyrics.ovh** | 普通歌词，零门槛 | ❌ |
| 3 | **Genius API** | 歌词+注释，需 Token | ✅ (免费) |

### 开源项目参考优先级

| 优先级 | 项目 | 适合参考 |
|-------|------|---------|
| 1 | **Auxio** | UI 架构、极简设计、ExoPlayer 集成 |
| 2 | **InnerTune** | YouTube Music 流媒体架构 |
| 3 | **OuterTune** | 完整功能集、Material 3 实现 |
| 4 | **RetroMusicPlayer** | 功能最完整参考 |
| 5 | **OmarNofal/Material-3-Music-Player** | 基础 Compose UI 组件 |

### 推荐技术栈组合（FreeMusic）

```
音频播放:     Media3 ExoPlayer (AndroidX)
歌词:         LRCLIB API (同步) + lyrics.ovh (兜底)
搜索/元数据:   Deezer API (无需 Key) + Spotify Web API (注册免费)
华语补充:     QQ Music 非官方 API (自部署)
UI 框架:     Jetpack Compose + Material 3
状态管理:     ViewModel + StateFlow
DI:          Hilt 或 Koin
本地存储:     Room Database
图片加载:     Coil (Compose 友好)
```

---

## 13. 更新日志

### 2026-04-10 (本次更新)

**新增内容**:
- YouTube Music 生态项目: InnerTune, ViMusic, SimpMusic, Spotube
- QQ Music / 咪咕音乐 API（非官方）详解
- lyrics.ovh 免费歌词 API（对比 LRCLIB）
- Orthrus-Android 调查结果（未找到）
- UI/UX 设计趋势详细分析
- Material 3 Music Player by OmarNofal
- 综合 API + 开源项目最终推荐表
- FreeMusic 推荐技术栈组合

**API 优先级最终推荐**:
1. 音频: **Deezer API**（无需 Key）+ **QQ Music 非官方 API**（自部署，华语）
2. 歌词: **LRCLIB**（同步歌词）+ **lyrics.ovh**（兜底）
3. 流媒体架构参考: **InnerTune / OuterTune**（YouTube Music）

*研究完成时间: 2026-04-10 00:08 GMT+8*
