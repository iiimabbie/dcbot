
package per.iiimabbie.discordbot.command;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * 按鈕處理接口，用於處理 Discord 互動按鈕事件。
 * 實現此接口的類應該專注於處理特定類型的按鈕互動。
 *
 * @author iiimabbie
 */
public interface ButtonHandler {
  /**
   * 處理按鈕互動事件。
   * 當用戶點擊 Discord 中的按鈕時，該方法會被調用。
   * 實現類應該根據按鈕的 ID 或其他屬性來決定是否處理該事件。
   *
   * @param event 按鈕互動事件，包含關於按鈕點擊的所有信息
   * @return 如果該處理器處理了此事件返回 true，否則返回 false 以允許其他處理器處理
   */
  boolean handleButtonInteraction(ButtonInteractionEvent event);
}