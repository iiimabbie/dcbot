# Discord Bot 

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![JDA](https://img.shields.io/badge/Discord-JDA_5.0-7289DA.svg)](https://github.com/discord-jda/JDA)
[![Gemini](https://img.shields.io/badge/AI-Gemini_2.5-blue.svg)](https://ai.google.dev/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![GitHub license](https://img.shields.io/badge/license-MIT-pink.svg)](https://opensource.org/licenses/MIT)

基於 JDA 和 Gemini API 構建的 Discord 機器人，支援 AI 對話和實用斜線命令。

## 核心功能

- **AI 對話**：標記機器人即可進行對話，用戶問題透過 Gemini 模型處理
- **斜線命令**：
  - `/幫助` - 瀏覽所有可用命令，支援分頁導航
  - `/清空` - 批量刪除頻道訊息（需管理權限）
- **Docker 部署**：提供容器化部署方案
- **錯誤處理**：完善的錯誤捕獲與處理機制

## 系統需求

- Java 21+
- Maven
- Docker (選用)
- Discord Bot Token
- Gemini API 金鑰

## 快速開始

### 設定配置

1. 在專案根目錄建立 `config.properties`：

```properties
# Discord bot token
discord.token=你的TOKEN
# Gemini API
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-04-17:generateContent
gemini.api.key=你的GEMINI_API_KEY
# 伺服器ID（可選，用於伺服器專屬命令）
guild.id=你的GUILD_ID
# 機器人設定
bot.name=你的機器人名稱
bot.status.text=在線狀態文字
# 系統提示詞
bot.system.prompt=AI系統提示詞
```

### 使用 Maven 編譯

```bash
mvn clean package
```

### 直接執行

```bash
java -jar target/discord-bot.jar
```

### Docker 部署

```bash
# 建構 Docker 映像
docker-compose up -d
```

## 專案結構

```
src/main/java/per/iiimabbie/discordbot/
├── BotMain.java                 # 入口點
├── command/                     # 命令系統
│   ├── ButtonHandler.java       # 按鈕處理器介面
│   ├── CommandManager.java      # 命令註冊與管理
│   ├── SlashCommand.java        # 斜線命令介面
│   └── impl/                    # 命令實現
│       ├── ClearCommand.java    # 清空訊息命令
│       └── HelpCommand.java     # 幫助命令
├── core/                        # 核心模組
│   ├── BotCore.java             # 機器人初始化與管理
│   └── MessageListener.java     # 訊息事件監聽器
├── enums/                       # 列舉類型
│   ├── ColorEnums.java          # 嵌入訊息顏色
│   └── ErrorEnums.java          # 錯誤訊息定義
├── model/                       # 資料模型
│   ├── ChatMessage.java         # 聊天訊息記錄
│   └── Conversation.java        # 對話管理
├── service/                     # 服務層
│   ├── AiService.java           # AI 服務介面
│   └── GeminiService.java       # Google Gemini 實現
└── util/                        # 工具類
    ├── ButtonUtils.java         # 按鈕工具
    ├── ConfigLoader.java        # 配置讀取
    ├── EmbedUtils.java          # 嵌入訊息工具
    └── MessageUtils.java        # 訊息處理工具
```

## 添加新命令

1. 在 `command/impl/` 目錄下創建新類，實現 `SlashCommand` 介面
2. 在 `BotCore.java` 中註冊命令：

```java
// 註冊命令
commandManager.registerCommand(new YourNewCommand());
```

## 自訂 AI 回應

編輯 `config.properties` 中的 `bot.system.prompt` 屬性來定製 AI 回應風格。

## 授權協議

MIT © [iiimabbie](https://github.com/iiimabbie)
