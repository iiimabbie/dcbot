
package per.iiimabbie.discordbotfelix.command;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * 按鈕處理接口
 */
public interface ButtonHandler {
  /**
   * 處理按鈕事件
   * @param event 按鈕事件
   * @return 如果處理了該事件返回true，否則返回false
   */
  boolean handleButtonInteraction(ButtonInteractionEvent event);
}