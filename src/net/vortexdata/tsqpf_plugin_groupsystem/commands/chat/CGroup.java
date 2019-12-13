package net.vortexdata.tsqpf_plugin_groupsystem.commands.chat;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import net.vortexdata.tsqpf.listeners.ChatCommandInterface;
import net.vortexdata.tsqpf.plugins.*;
import net.vortexdata.tsqpf_plugin_groupsystem.*;

public class CGroup implements ChatCommandInterface {

    private TS3Api api;
    private RequestManager requestManager;
    private PluginConfig config;

    public CGroup(TS3Api api, PluginConfig config, RequestManager requestManager) {
        this.api = api;
        this.config = config;
        this.requestManager = requestManager;
    }

    @Override
    public void gotCalled(TextMessageEvent textMessageEvent) {

        String[] command = textMessageEvent.getMessage().split(" ");

        if (command.length > 1) {

            if (command[1].equalsIgnoreCase("request")) {
                if (command.length > 2) {

                    if (GroupNameChecker.checkName(command[2])) {

                        requestManager.

                    } else {
                        api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestFailedNameInvalid"));
                    }

                } else {
                    api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestSyntax"));
                }
            }

        } else {
            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageSyntax"));
        }

    }

}
