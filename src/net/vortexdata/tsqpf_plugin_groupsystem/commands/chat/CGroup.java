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
    private GroupManager groupManager;
    private PluginConfig config;
    private PluginLogger logger;

    public CGroup(TS3Api api, PluginConfig config, RequestManager requestManager, PluginLogger logger) {
        this.api = api;
        this.config = config;
        this.requestManager = requestManager;
        this.groupManager = new GroupManager(config, api, logger);
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
                            boolean isOneWhitelisted = false;
                            for (int j = 0; j < whitelistedGroups.size(); j++) {
                                if (groups[i] == whitelistedGroups.get(j)) {
                                    isOneWhitelisted = true;
                                }
                            }

                            if (!isOneWhitelisted) {
                                api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestFailedAlreadyMemberOfGroup"));
                                return;
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
                                logger.printDebug("Failed to send new request broadcast to admins, maybe none are online? Appending details: " + e.getMessage());
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
                        requestManager.validateRequest(command[2], textMessageEvent.getInvokerUniqueId());
                        api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupRequestAccepted"));
                    } catch (PendingGroupNotFoundException e) {
                        api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupRequestNotFound"));
                        return;
                    }

                } else {
                    api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupRequestSyntax"));
                }
            } else if (command[1].equalsIgnoreCase("delete")) {

                if (isInvokerAdmin(textMessageEvent.getInvokerUniqueId())) {

                    if (command.length > 2) {

                        ArrayList<ServerGroup> groups = new ArrayList<>(api.getServerGroups());

                        for (ServerGroup g : groups) {
                            if (g.getName().equalsIgnoreCase(command[2])) {
                                try {
                                    groupManager.deleteGroup(g.getId());
                                    api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupDeleteSuccess"));
                                } catch (GroupNotFoundException e) {
                                    api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupDeleteFailedNotFound"));
                                } catch (WhitelistedGroupDeletionException e) {
                                    api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupDeleteFailedBlacklist"));
                                }
                                return;
                            }
                        }

                        api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupDeleteFailedNotFound"));

                    } else {
                        api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupDeleteSyntax"));
                    }

                } else if (command.length > 2) {

                    if (isInvokerGroupOwner(textMessageEvent.getInvokerUniqueId(), command[2])) {

                        try {
                            groupManager.deleteGroup(command[2]);
                            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupDeleteSuccess"));
                        } catch (GroupNotFoundException e) {
                            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupDeleteNoGroup"));
                        } catch (WhitelistedGroupDeletionException e) {
                            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupDeleteNoPermission"));
                        }

                    } else {
                        api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageErrorNoPermission"));
                    }

                }

            } else if (command[1].equalsIgnoreCase("requests") && isInvokerAdmin(textMessageEvent.getInvokerUniqueId())) {

                api.sendPrivateMessage(textMessageEvent.getInvokerId(), "Group Name || Owner UID");
                ArrayList<GroupRequest> pending = requestManager.getPendingRequests();
                if (pending.size() == 0) {
                    api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupRequestListNoRequests"));
                    return;
                }

                for (GroupRequest gr : pending) {
                    api.sendPrivateMessage(textMessageEvent.getInvokerId(), gr.getGroupname() + " || " + gr.getInvokerUUID());
                }


            } else if (command[1].equalsIgnoreCase("decline") && isInvokerAdmin(textMessageEvent.getInvokerUniqueId())) {

                if (command.length > 2) {

                    if (command.length > 3) {

                        String reason = "";
                        for (int i = 3; i < command.length; i++) {
                            reason += command[i];
                        }

                        if (requestManager.declineRequest(command[2], reason)) {
                            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupRequestDeclined") + reason);
                        } else {
                            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupRequestDeclinedFailed"));
                        }

                    } else {
                        api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupRequestDeclinedMissingReason"));
                    }

                } else {
                    api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageAdminGroupRequestSyntax"));
                }

            } else if (command[1].equalsIgnoreCase("cancel")) {

                if (command.length > 2) {

                    if (command[2].equalsIgnoreCase("request")) {
                        if (requestManager.cancelRequest(textMessageEvent.getInvokerUniqueId())) {
                            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestCancelSuccess"));
                        } else {
                            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageGroupRequestCancelFailed"));
                        }
                    } else if (command[2].equalsIgnoreCase("join")) {

                    }

                }

            }

        }



        else {
            api.sendPrivateMessage(textMessageEvent.getInvokerId(), config.readValue("messageSyntax"));
        }

    }

    public boolean isInvokerGroupOwner(String uid, String groupName) {
        ArrayList<ServerGroup> invokerGroups = new ArrayList<>(api.getServerGroupsByClient(api.getClientByUId(uid)));

        boolean isOwner = false;
        for (int i = 0; i < invokerGroups.size(); i++) {

            if (isOwner)
                break;

            for (int j = 0; j < invokerGroups.size(); j++) {

                if (invokerGroups.get(i).getName().equalsIgnoreCase(groupName)) {
                    isOwner = true;
                    break;
                }

            }

        }

        return isOwner;
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
