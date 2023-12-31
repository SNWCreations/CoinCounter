package snw.coincounter;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public final class CoinCounter extends JavaPlugin implements Listener {
    private boolean exec;

    @Override
    public void onEnable() {
        // Plugin startup logic
        new BukkitRunnable() {
            @Override
            public void run() {
                if (exec) {
                    final HashMap<Player, Integer> map = new LinkedHashMap<>();
                    for (Player player : getServer().getOnlinePlayers()) {
                        if (!shouldCompute(player)) {
                            continue;
                        }
                        final int total = computeCoin(player);
                        map.put(player, total);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("你当前持有的硬币总数: " + total));
                    }
                    final ArrayList<Map.Entry<Player, Integer>> entries = new ArrayList<>(map.entrySet());
                    entries.sort(Comparator.comparingInt(Map.Entry::getValue));
                    final String names = entries.stream()
                            .limit(3)
                            .map(e -> e.getKey().getName() + ":" + e.getValue())
                            .collect(Collectors.joining(", "));
                    String msg = "当前拥有最少硬币的是: " + names;
                    Optional.ofNullable(getServer().getPlayerExact("Murasame_mao"))
                            .ifPresent(mao -> mao.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg)));
                }
            }
        }.runTaskTimer(this, 0L, 10L);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("CoinCounter v1.0.0 正在运行。作者: ZX夏夜之风");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        exec = !exec;
        sender.sendMessage("OK");
        return true;
    }

    private boolean shouldCompute(Player player) {
        return player.getScoreboardTags().contains("player");
    }

    private int computeCoin(Player player) {
        int sum = 0;
        for (ItemStack stack : player.getInventory()) {
            for (CoinType type : CoinType.values()) {
                sum = type.compute(stack, sum);
            }
        }
        return sum;
    }

    enum CoinType {
        ONE(1, "tzz:yingbi_1"),
        THREE(3, "tzz:yingbi_3"),
        FIVE(5, "tzz:yingbi_5"),
        TEN(10, "tzz:yingbi_10");
        private final int val;
        private final String type;

        CoinType(int val, String type) {
            this.val = val;
            this.type = type;
        }

        public int compute(ItemStack stack, int prev) {
            return stack != null && fits(stack) ? prev + val * stack.getAmount() : prev;
        }

        public boolean fits(ItemStack stack) {
            return stack.getType().getKey().toString().equals(type);
        }
    }
}
