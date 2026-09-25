# Shizzi 简体中文 + Material 3 改造说明

基于原项目 `0.4.0-rc.3` 源码修改。

## 本次修改

- 汉化主要用户界面：首次引导、权限、兼容性、主页、设置、VPN、自动化、日志、诊断提示、通知与快捷设置磁贴。
- 保留技术标识原文，包括包名、Broadcast Action、Intent Extra、JSON 字段、内部状态码与协议字段，避免破坏兼容性。
- 界面统一为 Material 3 风格：
  - 默认色板改用 Material 3 tonal scheme；支持系统动态壁纸取色。
  - 旧 Neo-Brutalism 设置不再改变实际渲染，避免旧配置切回非 MD3 外观。
  - 设置页改为 Material 3 分组卡片。
  - 主连接按钮改为 Material 3 Button。
  - 首次引导按钮改为 Material 3 Button / OutlinedButton。
  - 顶栏使用 Material 3 Surface 层级。
  - 底部弹层统一使用 ModalBottomSheet。
  - 所有交互表面统一圆角、轻量 elevation 与 ripple。
- 普通界面字体改用 Android 系统无衬线字体，确保简体中文 CJK 字形稳定；日志仍保留等宽字体。
- 设置页移除了“设计语言”切换入口，只保留主题（跟随系统 / 浅色 / 深色）和强调色。

## 构建说明

项目构建逻辑保持原样。原项目 `app/build.gradle.kts` 会在 `preBuild` 前执行 `gomobile bind`，因此构建环境需要：

- Java 17
- Android SDK（compileSdk 35）
- Android NDK
- Go + `gomobile`（位于 `$HOME/go/bin/gomobile`）
- Gradle Wrapper 所需的 Gradle 8.14.3 与 Maven/Google 依赖缓存或可联网环境

建议 Work 直接在本源码根目录执行：

```bash
./gradlew assembleDebug
```

或准备签名后执行 release 构建。

## 校验情况

已做源码差异检查，并确认用户界面汉化没有修改广播 Action、Intent Extra、包名、JSON key 等技术协议字符串。
当前 ChatGPT 执行环境缺少可用的 Gradle 8.14.3 离线发行包，Gradle Wrapper 会尝试访问 `services.gradle.org`，因此本轮没有在本环境完成 APK 构建；交由 Work 使用其构建环境继续即可。
