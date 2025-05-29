package per.iiimabbie.dcbot.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiceRollListener extends ListenerAdapter {

    private final Random random = new Random();
    private final Pattern dicePattern = Pattern.compile("(\\d+)[dD](\\d+)");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        
        Message message = event.getMessage();
        String content = message.getContentRaw();
        
        // 檢查消息內容是否包含xdo格式
        Matcher matcher = dicePattern.matcher(content);
        if (matcher.find()) {
            try {
                int min = Integer.parseInt(matcher.group(1));
                int max = Integer.parseInt(matcher.group(2));
                
                if (min > 0 && max >= min) {
                    int result = generateRandomNumber(min, max);
                    
                    User author = event.getAuthor();
                    String mention = author.getAsMention();
                    String nickname = event.getMember() != null ? event.getMember().getEffectiveName() : author.getName();
                    
                    // 提取用戶在骰子後面的說明文字
                    String description = content.substring(matcher.end()).trim();

                    String response = String.format("%s\n%sd%s： %s\n%d[%d] = %d", 
                        mention, min, max, description, result, result, result);
                    
                    message.reply(response).queue();
                    log.info("已回應用戶 {} 的骰子請求：{}", nickname, content);
                }
            } catch (NumberFormatException e) {
                log.error("解析骰子參數時出錯", e);
            }
        }
    }
    
    /**
     * 生成 min 到 max 之間的隨機數字
     */
    private int generateRandomNumber(int min, int max) {
        if (min == 1 && max == 100) {
            // 1d100 特殊處理
            return random.nextInt(100) + 1;
        } else {
            return random.nextInt(max - min + 1) + min;
        }
    }
}