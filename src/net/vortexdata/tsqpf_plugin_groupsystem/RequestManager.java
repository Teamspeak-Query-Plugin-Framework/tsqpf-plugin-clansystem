package net.vortexdata.tsqpf_plugin_groupsystem;

import com.github.theholywaffle.teamspeak3.api.reconnect.*;
import net.vortexdata.tsqpf.plugins.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class RequestManager {

    private String pathRequestsFile;

    private ArrayList<GroupRequest> pendingRequests;
    private PluginLogger logger;


    public RequestManager(PluginLogger logger) {
        this.logger = logger;
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
