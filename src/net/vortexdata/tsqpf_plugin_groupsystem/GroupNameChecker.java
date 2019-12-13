package net.vortexdata.tsqpf_plugin_groupsystem;

public class GroupNameChecker {

    public static boolean checkName(String name) {

        if (name.length() >= 4 && name.length() <= 20) {
            return name.matches("[A-Za-z0-9]+");
        } else
            return false;

    }

}
