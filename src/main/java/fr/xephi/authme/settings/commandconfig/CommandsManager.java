package fr.xephi.authme.settings.commandconfig;

import com.github.authme.configme.SettingsManager;
import com.github.authme.configme.beanmapper.BeanProperty;
import com.github.authme.configme.knownproperties.ConfigurationData;
import com.github.authme.configme.properties.Property;
import com.github.authme.configme.resource.YamlFileResource;
import fr.xephi.authme.initialization.DataFolder;
import fr.xephi.authme.initialization.Reloadable;
import fr.xephi.authme.service.BukkitService;
import fr.xephi.authme.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;

/**
 * Manages configurable commands to be run when various events occur.
 */
public class CommandsManager implements Reloadable {

    private CommandConfig commandConfig;

    @Inject
    @DataFolder
    private File dataFolder;

    @Inject
    private BukkitService bukkitService;


    CommandsManager() {
    }

    public void runCommandsOnJoin(Player player) {
        executeCommands(player, commandConfig::getOnJoin);
    }

    public void runCommandsOnRegister(Player player) {
        executeCommands(player, commandConfig::getOnRegister);
    }

    private void executeCommands(Player player, Supplier<Map<String, Command>> configMapGetter) {
        for (Command command : configMapGetter.get().values()) {
            final String execution = command.getCommand().replace("%p", player.getName());
            if (Executor.CONSOLE.equals(command.getExecutor())) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), execution);
            } else {
                Bukkit.dispatchCommand(player, execution);
            }
        }
    }

    @PostConstruct
    @Override
    public void reload() {
        File file = new File(dataFolder, "commands.yml");
        FileUtils.copyFileFromResource(file, "commands.yml");

        Property<CommandConfig> commandConfigProperty =
            new BeanProperty<>(CommandConfig.class, "", new CommandConfig());
        SettingsManager settingsManager = new SettingsManager(
            new YamlFileResource(file), null, new ConfigurationData(singletonList(commandConfigProperty)));
        commandConfig = settingsManager.getProperty(commandConfigProperty);
    }


}
