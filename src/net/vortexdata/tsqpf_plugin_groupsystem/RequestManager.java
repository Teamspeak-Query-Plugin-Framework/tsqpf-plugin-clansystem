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
    }

    public boolean createRequest(String name, String uuid) throws GroupNameAlreadyTakenException, UserGroupAlreadyPendingException, UserAlreadyMemberOfGroupException {


        // TODO: Implement backcheck

        try {
            getGroupRequestByName(name);
            throw new GroupNameAlreadyTakenException();
        } catch (PendingGroupNotFoundException e) {

            try {
                getGroupRequestByUUID(uuid);
            } catch (PendingGroupNotFoundException e1) {
                try {
                    ArrayList<String> lines = new ArrayList<>(Files.lines(Paths.get(pathRequestsFile)).collect(Collectors.toList()));
                    lines.add(new GroupRequest(name, uuid).toString());

                    BufferedWriter bw = new BufferedWriter(new FileWriter(pathRequestsFile, false));
                    lines.forEach(x -> {
                        try {
                            bw.write(x);
                        } catch (IOException e) {
                            logger.printError("Failed to save line in group requests.");
                        }
                    });

                    bw.close();

                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        }

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

    public boolean declineRequest(String groupname) {
        for (int i = 0; i < pendingRequests.size(); i++) {
            if (pendingRequests.get(i).getGroupname().equalsIgnoreCase(groupname)) {
                return deleteRequest(pendingRequests.get(i));
            }
        }

        return false;
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

    public boolean loadRequests() {
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

}
