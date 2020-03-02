/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import land.face.strife.StrifePlugin;
import org.bukkit.entity.Player;
import se.ranzdo.bukkit.methodcommand.Arg;
import se.ranzdo.bukkit.methodcommand.Command;

public class AgilityCommand {

  private final StrifePlugin plugin;

  public AgilityCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Command(identifier = "agility create", permissions = "strife.command.agility")
  public void creationCommand(Player sender, @Arg(name = "name") String name,
      @Arg(name = "difficulty") double difficulty, @Arg(name = "xp") double xp) {

    boolean success = plugin.getAgilityManager()
        .createAgilityContainer(name, sender.getLocation(), (float) difficulty, (float) xp);

    if (success) {
      sendMessage(sender, "&eMAde it");
    } else {
      sendMessage(sender, "&efailed");
    }
  }

  @Command(identifier = "agility add", permissions = "strife.command.agility")
  public void addCommand(Player sender, @Arg(name = "name") String name) {

    boolean success = plugin.getAgilityManager().addAgilityLocation(name, sender.getLocation());

    if (success) {
      sendMessage(sender, "&eMAde it");
    } else {
      sendMessage(sender, "&efailed");
    }
  }
}
