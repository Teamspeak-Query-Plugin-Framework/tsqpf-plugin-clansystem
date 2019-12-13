package net.vortexdata.tsqpf_plugin_groupsystem.commands.chat;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.*;
import net.vortexdata.tsqpf.listeners.ChatCommandInterface;
import net.vortexdata.tsqpf.plugins.*;
import net.vortexdata.tsqpf_plugin_groupsystem.*;
import net.vortexdata.tsqpf_plugin_groupsystem.exceptions.*;

import java.util.*;

public class CGroup implements ChatCommandInterface {

    private TS3Api api;
    private RequestManager requestManager;
    private PluginConfig config;
    private PluginLogger logger;

    public CGroup(TS3Api api, PluginConfig config, RequestManager requestManager, PluginLogger logger) {
        this.api = api;
        this.config = config;
        this.requestManager = requestManager;
        this.logger = logger;
    }

    @Override
    public void gotCalled(TextMessageEvent textMessageEvent) {

        String[] command = textMessageEvent.getMessage().split(" ");

        if (command.length > 1) {

            if (command[1].equalsIgnoreCase("request")) {
                if (command.length > 2) {

                    if (GroupNameChecker.checkName(command[2])) {

                        // Check if user is already member of a group
                        int[] groups = api.getClientByUId(textMessageEvent.getInvokerUniqueId()).getServerGroups();
                        String[] whitelistedGroupsRaw = config.readValue("whitelistGroupIds").split(",");
                        ArrayList<Integer> whitelistedGroups = new ArrayList<>();

                        for (String rGroup : whitelistedGroupsRaw) {
                            whitelistedGroups.add(Integer.parseInt(rGroup));
                        }

                        for (int i = 0; i < groups.length; i++) {
                            for (int j = 0; j < whitelistedGroups.size(); j++) {
                                if (groups[i] != whitelistedGroups.get(j)) {
                                    api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestFailedAlreadyMemberOfGroup"));
                                    return;
                                }
                            }
                        }


                        // Check if user has already requested this group or if group name is already taken
                        try {
                            requestManager.getGroupRequestByUUID(textMessageEvent.getInvokerUniqueId());
                            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestFailedAlreadyPending"));
                            return;
                        } catch (PendingGroupNotFoundException e) {
                            // Ignore and continue
                        }

                        try {
                            requestManager.getGroupRequestByName(command[2]);
                            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestFailedNameTaken"));
                            return;
                        } catch (PendingGroupNotFoundException e) {
                            // Ignore and continue
                        }

                        requestManager.createRequest(command[2], textMessageEvent.getInvokerUniqueId());
                        api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestSuccess"));

                        ArrayList<String> adminBroadcast = new ArrayList<>();
                        ArrayList<Integer> adminGroups = new ArrayList<>();

                        String[] adminGroupsRaw = config.readValue("adminGroupIds").split(",");

                        for (String s : adminGroupsRaw)
                            adminGroups.add(Integer.parseInt(s));

                        for (int i = 0; i < adminGroups.size(); i++) {

                            for (ServerGroupClient c : api.getServerGroupClients(adminGroups.get(i))) {
                                adminBroadcast.add(c.getUniqueIdentifier());
                            }

                        }

                        for (String uid : adminBroadcast) {
                            try {
                                api.sendPrivateMessage(api.getClientByUId(uid).getId(), config.readValue("messageAdminGroupRequestPending"));
                            } catch (Exception e) {
                                logger.printDebug("Failed to send new request broadcast to client ");
                            }
                        }


                    } else {
                        api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestFailedNameInvalid"));
                    }

                } else {
                    api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestSyntax"));
                }
            } else if (command[1].equalsIgnoreCase("validate") && isInvokerAdmin(textMessageEvent.getInvokerUniqueId())) {
                if (command.length > 2) {

                    try {
                        requestManager.getGroupRequestByName(command[2]);
                        requestManager.
                    } catch (PendingGroupNotFoundException e) {
                        api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupRequestNotFound"));
                        return;
                    }

                } else {
                    api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestSyntax"));
                }
            }

        } else {
            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageSyntax"));
        }

    }

    public boolean isInvokerAdmin(String uid) {
        ArrayList<ServerGroup> invokerGroups = new ArrayList<>(api.getServerGroupsByClient(api.getClientByUId(uid)));
        ArrayList<Integer> adminGroups = new ArrayList<>();

        String[] adminGroupsRaw = config.readValue("adminGroupIds").split(",");

        for (String s : adminGroupsRaw) {
            adminGroups.add(Integer.parseInt(s));
        }

        boolean isAdmin = false;
        for (int i = 0; i < invokerGroups.size(); i++) {

            if (isAdmin)
                break;

            for (int j = 0; j < adminGroups.size(); j++) {

                if (invokerGroups.get(i).getId() == adminGroups.get(j)) {
                    isAdmin = true;
                    break;
                }

            }

        }

        return isAdmin;
    }

}
