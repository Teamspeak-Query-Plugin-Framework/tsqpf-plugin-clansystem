package net.vortexdata.tsqpf_plugin_groupsystem;

import com.github.theholywaffle.teamspeak3.*;
import net.vortexdata.tsqpf.plugins.*;
import net.vortexdata.tsqpf_plugin_groupsystem.exceptions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class RequestManager {

    private String pathRequestsFile;

    private ArrayList<GroupRequest> pendingRequests;
    private PluginLogger logger;
    private PluginConfig config;
    private TS3Api api;

    public RequestManager(PluginLogger logger, TS3Api api, PluginConfig config) {
        this.logger = logger;
        this.api = api;
        this.config = config;
        pendingRequests = new ArrayList<>();
        pathRequestsFile = "plugins//GroupSystem//requests.tsqpfd";
    }

    public boolean createRequest(String name, String uuid) {

        try {
            getGroupRequestByUUID(uuid);
        } catch (PendingGroupNotFoundException e1) {
            try {
                ArrayList<String> lines = new ArrayList<>(Files.lines(Paths.get(pathRequestsFile)).collect(Collectors.toList()));
                lines.add(new GroupRequest(name, uuid).toString());

                BufferedWriter bw = new BufferedWriter(new FileWriter(pathRequestsFile, false));
                lines.forEach(x -> {
                    try {
                        bw.write(x + "\n");
                    } catch (IOException e) {
                        logger.printError("Failed to save line in group requests.");
                    }
                });

                bw.close();
            } catch (IOException e) {
                return false;
            }
        }

        loadRequests();

        return true;

    }

    public GroupRequest getGroupRequestByName(String name) throws PendingGroupNotFoundException {

        for (GroupRequest gr : pendingRequests)
            if (gr.getGroupname().equalsIgnoreCase(name))
                return gr;

        throw new PendingGroupNotFoundException();

    }

    public GroupRequest getGroupRequestByUUID(String uuid) throws PendingGroupNotFoundException {
        for (GroupRequest gr : pendingRequests)
            if (gr.getInvokerUUID().equalsIgnoreCase(uuid))
                return gr;

        throw new PendingGroupNotFoundException();
    }

    public boolean declineRequest(String groupname, String reason) {
        loadRequests();
        boolean isDeleted = false;

        for (int i = 0; i < pendingRequests.size(); i++) {
            if (pendingRequests.get(i).getGroupname().equalsIgnoreCase(groupname)) {

                 GroupRequest gr = pendingRequests.get(i);
                 isDeleted = deleteRequest(gr);
                 if (isDeleted) {
                     String uid = gr.getInvokerUUID();
                     int id = api.getClientByUId(uid).getId();
                     api.sendPrivateMessage(id, config.readValue("messageGroupValidationFailed") + reason);
                 }

                 return isDeleted;
            }
        }

        return isDeleted;
    }

    public boolean deleteRequest(GroupRequest gr) {
        try {
            ArrayList<String> oldLines = new ArrayList<>(Files.lines(Paths.get(pathRequestsFile)).collect(Collectors.toList()));
            ArrayList<String> newLines = new ArrayList<>();

            oldLines.stream().forEach(x -> {

                if (!x.split(";")[0].equalsIgnoreCase(gr.getGroupname()))
                    newLines.add(x);

            });

            if (!newLines.equals(oldLines)) {

                logger.printDebug("Old collected lines are not equal to new lines, deletion process was successful.");
                BufferedWriter bw = new BufferedWriter(new FileWriter(pathRequestsFile, false));
                newLines.forEach(x -> {
                    try {
                        bw.write(x);
                    } catch (IOException e) {
                        logger.printError("Failed to save line in group requests.");
                    }
                });

                bw.close();

                loadRequests();
                return true;

            } else {
                logger.printDebug("Old collected lines are equal to new lines, deletion process was unsuccessful.");
                return false;
            }

        } catch (IOException e) {
            logger.printError("Failed to delete group creation request " + gr.getGroupname() + ", appending details: " + e.getMessage());
            return false;
        }

    }

    public boolean validateRequest(String name, String clientUid) {
        try {
            GroupRequest gr = getGroupRequestByName(name);
            deleteRequest(gr);
            loadRequests();

            try {
                int newGroupId = api.addServerGroup(name);
                api.addServerGroupPermission(newGroupId, "i_group_show_name_in_tree", 1, false, false);
                api.addClientToServerGroup(newGroupId, api.getClientByUId(gr.getInvokerUUID()).getDatabaseId());
                api.addClientToServerGroup(Integer.parseInt(config.readValue("groupOwnerGroupId")), api.getClientByUId(gr.getInvokerUUID()).getDatabaseId());
                api.sendPrivateMessage(api.getClientByUId(gr.getInvokerUUID()).getId(), config.readValue("messageGroupRequestAccepted"));
                logger.printDebug("Group request for group " + name + " has been validated by " + api.getClientByUId(clientUid).getLoginName() + ".");
            } catch (Exception e) {
                logger.printDebug("Could not add client to requested group.");
            }

            return true;
        } catch (PendingGroupNotFoundException e) {
            logger.printDebug("Could not validate group.");
            return false;
        }
    }

    public boolean loadRequests() {

        File file = new File(pathRequestsFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }

        ArrayList<GroupRequest> newGroupRequests = new ArrayList<>();
        try {
            Files.lines(Paths.get(pathRequestsFile)).forEach(x -> {

                String[] split = x.split(";");
                newGroupRequests.add(new GroupRequest(
                        split[0],
                        split[1]
                ));

            });
            pendingRequests = newGroupRequests;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public ArrayList<GroupRequest> getPendingRequests() {
        return pendingRequests;
    }

    public boolean cancelRequest(String uid) {

        boolean success = false;

        for (GroupRequest gr : pendingRequests) {

            if (gr.getInvokerUUID().equals(uid)) {
                deleteRequest(gr);
                success = true;
                break;
            }

        }

        return success;

    }

}
