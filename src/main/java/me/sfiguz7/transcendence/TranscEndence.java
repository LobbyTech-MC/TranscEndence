package me.sfiguz7.transcendence;

import java.io.File;
import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import me.sfiguz7.transcendence.implementation.core.attributes.TERegistry;
import me.sfiguz7.transcendence.implementation.core.commands.TranscEndenceCommand;
import me.sfiguz7.transcendence.implementation.items.generators.QuirpScatterer;
import me.sfiguz7.transcendence.implementation.items.items.Daxi;
import me.sfiguz7.transcendence.implementation.items.items.Polarizer;
import me.sfiguz7.transcendence.implementation.items.items.Quirps;
import me.sfiguz7.transcendence.implementation.items.items.StabilizedItems;
import me.sfiguz7.transcendence.implementation.items.items.UnstableIngots;
import me.sfiguz7.transcendence.implementation.items.items.Zots;
import me.sfiguz7.transcendence.implementation.items.items.Zots_2;
import me.sfiguz7.transcendence.implementation.items.machines.QuirpAnnihilator;
import me.sfiguz7.transcendence.implementation.items.machines.QuirpCycler;
import me.sfiguz7.transcendence.implementation.items.machines.QuirpOscillator;
import me.sfiguz7.transcendence.implementation.items.machines.Stabilizer;
import me.sfiguz7.transcendence.implementation.items.machines.ZotOverloader;
import me.sfiguz7.transcendence.implementation.items.multiblocks.NanobotCrafter;
import me.sfiguz7.transcendence.implementation.listeners.DaxiAnimationArmorStandHeadListener;
import me.sfiguz7.transcendence.implementation.listeners.DaxiDeathListener;
import me.sfiguz7.transcendence.implementation.listeners.DaxiEffectModificationListener;
import me.sfiguz7.transcendence.implementation.listeners.TranscEndenceGuideListener;
import me.sfiguz7.transcendence.implementation.listeners.UnstableIngotDropListener;
import me.sfiguz7.transcendence.implementation.listeners.UnstableListener;
import me.sfiguz7.transcendence.implementation.tasks.RecurrentRefreshTask;
import me.sfiguz7.transcendence.implementation.tasks.StableTask;
import me.sfiguz7.transcendence.implementation.utils.SaveUtils;
import me.sfiguz7.transcendence.lists.TEItems;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;

public class TranscEndence extends JavaPlugin implements SlimefunAddon {

    private static TranscEndence instance;
    private final TERegistry registry = new TERegistry();
    private int researchId = 7100;

    private int highchance;
    private int zotRequiredCharge;

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
            getLogger().log(Level.SEVERE, "本插件需要 鬼斩前置库插件(GuizhanLibPlugin) 才能运行!");
            getLogger().log(Level.SEVERE, "从此处下载: https://50l.cc/gzlib");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;

        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        Config cfg = new Config(this);

        if (getConfig().getBoolean("options.auto-update") && getDescription().getVersion().startsWith("Build")) {
            GuizhanUpdater.start(this, getFile(), "SlimefunGuguProject", "TranscEndence", "master");
        }

        int bStatsId = 7329;
        new Metrics(this, bStatsId);


        // Commands
        getCommand("transcendence").setExecutor(new TranscEndenceCommand());
        // Listeners
        new UnstableListener(this);
        new DaxiDeathListener(this);
        new DaxiEffectModificationListener(this);
        new DaxiAnimationArmorStandHeadListener(this);
        new UnstableIngotDropListener(this);
        new TranscEndenceGuideListener(cfg.getBoolean("options.give-guide-on-first-join"));

        // Instability Update Task
        if (cfg.getBoolean("options.enable-instability-effects")) {
            getServer().getScheduler().runTaskTimerAsynchronously(
                this,
                new StableTask(),
                0L,
                cfg.getInt("options.instability-update-interval") * 20L
            );
        }
        // Recurrent refresh task (only really needed for absorption)
        getServer().getScheduler().runTaskTimerAsynchronously(
            this,
            new RecurrentRefreshTask(),
            0L,
            15 * 20L
        );

        // Config fetching
        zotRequiredCharge = getConfig().getInt("options.zot-required-charge");
        highchance = getConfig().getInt("options.polarizer-affinity-chance");
        if (highchance < 26 || highchance > 50) {
            getLogger().log(Level.SEVERE, "无效的配置: options.polarizer-affinity-chance");
            getLogger().log(Level.SEVERE, "有效范围: 26 - 50");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }




        /* Items */
        for (Quirps.Type type : Quirps.Type.values()) {
            new Quirps(type).register(this);
        }

        for (UnstableIngots.Type type : UnstableIngots.Type.values()) {
            new UnstableIngots(type).register(this);
        }

        new Research(new NamespacedKey(this, "unstable"),
            ++researchId, "不稳定的材料", 23)
            .addItems(TEItems.UNSTABLE_INGOT,
                TEItems.UNSTABLE_INGOT_2,
                TEItems.UNSTABLE_INGOT_3,
                TEItems.UNSTABLE_INGOT_4).register();

        for (Zots.Type type : Zots.Type.values()) {
            new Zots(type).register(this);
        }

        for (StabilizedItems.Type type : StabilizedItems.Type.values()) {
            new StabilizedItems(type).register(this);
        }

        new Research(new NamespacedKey(this, "stable"),
            ++researchId, "稳定的材料", 30)
            .addItems(TEItems.STABLE_INGOT,
                TEItems.STABLE_BLOCK).register();
        /* More items moved below for aesthetic purposes */


        /* Machines pt. 1 */
        new QuirpScatterer().register(this);

        new Research(new NamespacedKey(this, "quirp_scatterer"),
            ++researchId, "粒子发电机", 20)
            .addItems(TEItems.QUIRP_SCATTERER).register();

        new NanobotCrafter().register(this);

        new Research(new NamespacedKey(this, "nanobot_crafter"),
            ++researchId, "超能纳米工作台", 15)
            .addItems(TEItems.NANOBOT_CRAFTER).register();

        new QuirpOscillator().register(this);

        new Research(new NamespacedKey(this, "quirp_oscillator"),
            ++researchId, "粒子生产机", 37)
            .addItems(TEItems.QUIRP_OSCILLATOR,
                TEItems.QUIRP_UP,
                TEItems.QUIRP_DOWN,
                TEItems.QUIRP_LEFT,
                TEItems.QUIRP_RIGHT,
                TEItems.QUIRP_CONDENSATE).register();


        /* Items pt. 2 */
        for (Zots_2.Type type : Zots_2.Type.values()) {
            new Zots_2(type).register(this);
        }
        new Research(new NamespacedKey(this, "zots"),
            ++researchId, "充能粒子", 30)
            .addItems(TEItems.ZOT_UP,
                TEItems.ZOT_DOWN,
                TEItems.ZOT_LEFT,
                TEItems.ZOT_RIGHT,
                TEItems.ZOT_UP_2,
                TEItems.ZOT_DOWN_2,
                TEItems.ZOT_LEFT_2,
                TEItems.ZOT_RIGHT_2).register();

        for (Daxi.Type type : Daxi.Type.values()) {
            new Daxi(type).register(this);
        }

        new Research(new NamespacedKey(this, "daxis"),
            ++researchId, "超能药水", 30)
            .addItems(TEItems.DAXI_STRENGTH,
                TEItems.DAXI_ABSORPTION,
                TEItems.DAXI_FORTITUDE,
                TEItems.DAXI_SATURATION,
                TEItems.DAXI_REGENERATION).register();

        for (Polarizer.Type type : Polarizer.Type.values()) {
            new Polarizer(type).register(this);
        }

        new Research(new NamespacedKey(this, "polarizers"),
            ++researchId, "偏振器", 23)
            .addItems(TEItems.VERTICAL_POLARIZER,
                TEItems.HORIZONTAL_POLARIZER).register();

        /* Machines pt. 2 */
        new QuirpAnnihilator().register(this);

        new QuirpCycler().register(this);

        new Stabilizer().register(this);

        new Research(new NamespacedKey(this, "quirp_annihilator"),
            ++researchId, "粒子冷凝机", 40)
            .addItems(TEItems.QUIRP_ANNIHILATOR,
                TEItems.QUIRP_CYCLER,
                TEItems.STABILIZER).register();

        new ZotOverloader().register(this);

        new Research(new NamespacedKey(this, "zot_overloader"),
            ++researchId, "粒子过载机", 35)
            .addItems(TEItems.ZOT_OVERLOADER).register();

        /*
        new SlimefunItem(TEItems.transcendence, TEItems.TE_INFO, RecipeType.NULL, new ItemStack[0]
        ).register(this);
        */


        // Initialise data if it exists
        SaveUtils.readData();
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        SaveUtils.writeData();
        instance = null;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/SlimefunGuguProject/TranscEndence/issues";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    public int getHighchance() {
        return instance.highchance;
    }

    public int getZotRequiredCharge() {
        return instance.zotRequiredCharge;
    }

    public static TERegistry getRegistry() {
        return instance.registry;
    }

    public static TranscEndence getInstance() {
        return instance;
    }

    public static String getVersion() {
        return instance.getDescription().getVersion();
    }

}
