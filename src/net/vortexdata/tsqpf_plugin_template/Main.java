package net.vortexdata.tsqpf_plugin_template;

import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import net.vortexdata.tsqpf.plugins.TeamspeakPlugin;
import net.vortexdata.tsqpf_plugin_template.commands.chat.CTest;

public class Main extends TeamspeakPlugin {

    // This is run when the Framework has loaded your plugin and tries to enable it.
    @Override
    public void onEnable() {

        // Sets the plugin.conf default values
        getConfig().setDefault("myJoinMessage", "Framework is now online!");
        // Saves the config
        getConfig().saveAll();

        // Sends a global server message
        getAPI().sendServerMessage(getConfig().readValue("myJoinMessage"));

        // Register a chat command
        registerChatCommand(new CTest(getAPI()), "!test");


        getLogger().printInfo("Template plugin loaded");

    }

    // This is run when the Framework shuts down or your plugin gets disabled.
    @Override
    public void onDisable() {

        getAPI().sendServerMessage("I'm going offline again,... bye!");

    }

    /*
        Register a server event listener
     */
    @Override
    public void onClientJoin(ClientJoinEvent clientJoinEvent) {
        getAPI().sendPrivateMessage(clientJoinEvent.getClientId(), "Hello!");
    }

}
