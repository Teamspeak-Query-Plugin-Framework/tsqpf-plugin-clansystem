package net.vortexdata.tsqpf_plugin_groupsystem;

import com.github.theholywaffle.teamspeak3.*;
import com.github.theholywaffle.teamspeak3.api.wrapper.*;
import net.vortexdata.tsqpf.plugins.*;
import net.vortexdata.tsqpf_plugin_groupsystem.exceptions.*;

import java.util.*;

public class GroupManager {

    private ArrayList<Integer> whitelistedGroups;
    private TS3Api api;
    private PluginLogger logger;

    public GroupManager(PluginConfig config, TS3Api api, PluginLogger logger) {

        whitelistedGroups = new ArrayList<>();
        this.api = api;
        this.logger = logger;

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

}
