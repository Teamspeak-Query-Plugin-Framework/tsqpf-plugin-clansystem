package net.vortexdata.tsqpf_plugin_groupsystem;

import com.github.theholywaffle.teamspeak3.*;
import com.github.theholywaffle.teamspeak3.api.*;
import com.github.theholywaffle.teamspeak3.api.wrapper.*;
import net.vortexdata.tsqpf.plugins.*;
import net.vortexdata.tsqpf_plugin_groupsystem.exceptions.*;

import java.io.*;
import java.util.*;

public class GroupManager {

    private ArrayList<Integer> whitelistedGroups;
    private TS3Api api;
    private PluginLogger logger;
    private PluginConfig config;

    private String pathToJoinrequests;
    private String pathToGroupprefs;

    public GroupManager(PluginConfig config, TS3Api api, PluginLogger logger) {

        whitelistedGroups = new ArrayList<>();
        this.api = api;
        this.logger = logger;
        this.config = config;

        pathToJoinrequests = "plugins//GroupSystem//joinrequests.tsqpf";

        String[] groupWhitelistRaw = config.readValue("whitelistGroupIds").split(",");

        for (String id : groupWhitelistRaw) {
            whitelistedGroups.add(Integer.parseInt(id));
        }

    }

    public boolean deleteGroup(int groupId) throws GroupNotFoundException, WhitelistedGroupDeletionException {

        if (whitelistedGroups.contains(groupId))
            throw new WhitelistedGroupDeletionException();

        try {
            api.deleteServerGroup(groupId);
            return true;
        } catch (Exception e) {
            logger.printDebug("Could not delete group, appending details:" + e.getMessage());
            return false;
        }


    }

    public boolean deleteGroup(String name) throws GroupNotFoundException, WhitelistedGroupDeletionException {

        int id = -1;

        ArrayList<ServerGroup> groups = new ArrayList<>(api.getServerGroups());

        for (ServerGroup g : groups) {
            if (g.getName().equalsIgnoreCase(name)) {
                id = g.getId();
                break;
            }
        }

        return deleteGroup(id);

    }

    public boolean createJoinRequest(String clientUid, String group) {

        ArrayList<ServerGroup> clientGroups = new ArrayList<>(api.getServerGroupsByClient(api.getClientByUId(clientUid)));
        ArrayList<Integer> adminGroupIds = new ArrayList<>();

        String[] raw = config.readValue("adminGroupIds").split(",");
        for (String s : raw)
            adminGroupIds.add(Integer.parseInt(s));

        for (int id : adminGroupIds) {
            if (clientGroups.contains(id)) {
                return false;
            }
        }

        BufferedWriter bw = null;
        BufferedReader br = null;
        try {

            int groupId = -1;

            ArrayList<ServerGroup> allGroups = new ArrayList<>(api.getServerGroups());

            for (ServerGroup g : allGroups) {
                if (g.getName().equalsIgnoreCase(group))
                    groupId = g.getId();
            }

            if (groupId == -1)
                return false;

            br = new BufferedReader(new FileReader(pathToJoinrequests));

            ArrayList<String> oldLines = new ArrayList<>();
            ArrayList<String> newLines = new ArrayList<>();

            while (br.ready()) {
                oldLines.add(br.readLine());
            }

            for (String line : oldLines) {
                if (line.split(";")[0].equals(clientUid)) {
                    return false;
                }
            }

            bw = new BufferedWriter(new FileWriter(pathToJoinrequests, true));
            bw.write(clientUid +";"+ groupId + "\n");

        } catch (Exception e) {
            logger.printError(e.getMessage());
        }


        return true;
    }

    public boolean deleteJoinRequest(String clientUid) {

        ArrayList<ServerGroup> clientGroups = new ArrayList<>(api.getServerGroupsByClient(api.getClientByUId(clientUid)));

        BufferedWriter bw = null;
        BufferedReader br = null;
        try {

            br = new BufferedReader(new FileReader(pathToJoinrequests));

            ArrayList<String> oldLines = new ArrayList<>();
            ArrayList<String> newLines = new ArrayList<>();

            while (br.ready()) {
                oldLines.add(br.readLine());
            }

            for (String line : oldLines) {
                if (!line.split(";")[0].equals(clientUid)) {
                    newLines.add(line);
                }
            }

            bw = new BufferedWriter(new FileWriter(pathToJoinrequests, false));

            for (String line : newLines)
                bw.write(line);

        } catch (Exception e) {
            logger.printError(e.getMessage());
        }


        return true;
    }

    public String createInvite(int groupId) {
        return api.addPrivilegeKey(PrivilegeKeyType.SERVER_GROUP, groupId, 0, "Group invite key");
    }

    public ArrayList<PrivilegeKey> getInviteCodes(int groupId) {

        ArrayList<PrivilegeKey> groupKeys = new ArrayList<>();
        ArrayList<PrivilegeKey> keys = new ArrayList<>(api.getPrivilegeKeys());

        for (PrivilegeKey key : keys) {
            if (key.getGroupId() == groupId)
                groupKeys.add(key);
        }

        return groupKeys;

    }

}
