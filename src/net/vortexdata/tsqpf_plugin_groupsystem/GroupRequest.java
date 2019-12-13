package net.vortexdata.tsqpf_plugin_groupsystem;

public class GroupRequest {

    private String groupname;
    private String invokerUUID;

    public GroupRequest(String groupname, String invokerUUID) {
        this.groupname = groupname;
        this.invokerUUID = invokerUUID;
    }

    public String getGroupname() {
        return groupname;
    }

    public String getInvokerUUID() {
        return invokerUUID;
    }
}
