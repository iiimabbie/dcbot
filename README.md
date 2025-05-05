# Discord Bot

[![GitHub license](https://img.shields.io/badge/license-MIT-pink.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Discord](https://img.shields.io/badge/Discord-JDA-7289DA.svg)](https://github.com/discord-jda/JDA)
[![Gemini](https://img.shields.io/badge/AI-Gemini-blue.svg)](https://ai.google.dev/)

## 功能特色

- **AI 互動**：標記機器人即可與其對話，由 Gemini 2.0 Flash 模型提供支援
- **斜線命令**：提供實用的頻道管理功能
    - `/幫助` - 列出所有可用命令，支援分頁瀏覽
    - `/清空` - 批量清除指定數量的聊天訊息 (需要管理員權限)
- **可配置**：透過配置文件調整機器人行為和回應模式

## 系統需求

- Java 21 或更高版本
- Maven

## 快速開始

### 配置設定

使用前需在專案根目錄建立 `config.properties` 檔案：

```properties
# Discord bot token
discord.token=你的Discord機器人Token
# Gemini API key
gemini.api.key=你的Gemini API金鑰
# 伺服器ID（可選，用於伺服器專屬命令）
guild.id=你的Discord伺服器ID
# 機器人設定
bot.name=機器人名稱
bot.status.text=在線狀態文字
# 系統提示詞
bot.system.prompt=系統提示詞內容
```

### 編譯與執行

```bash
# 使用Maven編譯
mvn clean package

# 執行生成的jar檔案
java -jar target/discord-bot-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 項目結構

```
src/main/java/per/iiimabbie/discordbot/
├── BotMain.java                 # 入口點
├── command/                     # 命令系統
│   ├── ButtonHandler.java       # 按鈕處理接口
│   ├── CommandManager.java      # 命令管理器
│   ├── SlashCommand.java        # 斜線命令接口 
│   └── impl/                    # 命令實現
│       ├── ClearCommand.java    # 清空訊息命令
│       └── HelpCommand.java     # 幫助命令
├── core/                        # 核心模組
│   ├── BotCore.java             # 機器人核心
│   └── MessageListener.java     # 訊息監聽器
├── model/                       # 資料模型
│   ├── ChatMessage.java         # 聊天訊息記錄
│   └── Conversation.java        # 對話管理器
├── service/                     # 服務層
│   ├── AiService.java           # AI服務接口
│   └── GeminiService.java       # Google Gemini實現
└── util/                        # 工具類
    ├── ConfigLoader.java        # 配置讀取器
    └── MessageUtils.java        # 訊息處理工具
```

## 自訂開發

### 新增命令

1. 建立新的命令類，實現 `SlashCommand` 接口
2. 在 `BotCore.java` 中註冊命令：
```java
commandManager.registerCommand(new YourNewCommand());
```

### 修改系統提示

編輯 `config.properties` 中的 `bot.system.prompt` 屬性來自訂機器人的回應風格。

## 作者 & 授權協議

MIT © [iiimabbie](https://github.com/iiimabbie)
