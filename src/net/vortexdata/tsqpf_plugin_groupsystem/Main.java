package net.vortexdata.tsqpf_plugin_groupsystem;

import com.github.theholywaffle.teamspeak3.api.event.*;
import net.vortexdata.tsqpf.plugins.TeamspeakPlugin;
import net.vortexdata.tsqpf_plugin_groupsystem.commands.chat.*;

public class Main extends TeamspeakPlugin {

    @Override
    public void onEnable() {

        getConfig().setDefault("adminGroupIds", "1");
        getConfig().setDefault("whitelistGroupIds", "1");
        getConfig().setDefault("groupOwnerGroupId", "1");
        getConfig().setDefault("groupTagPosition", "before");
        getConfig().setDefault("i_group_needed_modify_power", "10");
        getConfig().setDefault("i_group_needed_member_add_power", "10");
        getConfig().setDefault("i_group_needed_member_remove_power", "10");
        // Message Group Request
        getConfig().setDefault("messageGroupRequestSyntax", "Create your own group by using '!group request <GROUPNAME>'.");
        getConfig().setDefault("messageGroupRequestSuccess", "Your group has been registered and will be reviewed by an administrator as soon as possible.");
        getConfig().setDefault("messageGroupRequestFailedAlreadyMemberOfGroup", "You can not request a group if you are already member of one.");
        getConfig().setDefault("messageGroupRequestFailedAlreadyPending", "You have already requested a group. Please wait till its verified or rejected. If you want to cancel your group request, you can do that by using the command '!group cancel request'.");
        getConfig().setDefault("messageGroupRequestFailedNameTaken", "Sorry, but this group name is already taken.");
        getConfig().setDefault("messageGroupRequestFailedNameInvalid", "Sorry, but this group name is not valid. Make sure it only contains alpha-numeric characters and is between 4 - 20 characters in length.");
        getConfig().setDefault("messageGroupRequestCancelSuccess", "All of your pending group requests have been cancelled.");
        getConfig().setDefault("messageGroupRequestCancelFailed", "You don't have any pending group requests.");
        getConfig().setDefault("messageGroupRequestCancelSyntax", "Syntax: !group cancel request");
        getConfig().setDefault("messageGroupRequestFailedNoPermissions", "Sorry, but you don't have the required permissions to perform this action.");
        getConfig().setDefault("messageGroupRequestAccepted", "Your requested group has been reviewed and verified.");

        // Message Group Validation
        getConfig().setDefault("messageGroupValidationSuccess", "The group you requested has been validated.");
        getConfig().setDefault("messageGroupValidationFailed", "The group you requested has been rejected for the following reason: ");

        // Message Group Join
        getConfig().setDefault("messageGroupJoinRequestAccepted", "Your join request has been accepted.");
        getConfig().setDefault("messageGroupJoinRequestDeclined", "Your join request has been declined.");
        getConfig().setDefault("messageGroupJoinRequestFailedAlreadyPending", "You already have a pending join request. If you want to cancel the current one, used '!group cancel join'");
        getConfig().setDefault("messageGroupJoinRequestFailedClosedGroup", "Sorry, but this group does not accept any join requests at this time.");
        getConfig().setDefault("messageGroupJoinRequestFailedUnknownGroup", "This group does not exist. Please check for any typos.");
        getConfig().setDefault("messageGroupJoinRequestCreated", "Join request has been created.");
        getConfig().setDefault("messageGroupJoinSyntax", "Syntax: !group join <GROUPNAME>");

        getConfig().setDefault("messageGroupManageJoinRequestAccepted", "Join request has been accepted.");
        getConfig().setDefault("messageGroupManageJoinRequestDecline", "Join request has been declined.");

        getConfig().setDefault("messageGroupManageJoinRequestDisable", "Join requests have been disabled.");
        getConfig().setDefault("messageGroupManageJoinRequestEnable", "Join requests have been enabled.");
        getConfig().setDefault("messageGroupManageJoinRequestAlreadyEnabled", "Join requests are already enabled.");
        getConfig().setDefault("messageGroupManageJoinRequestAlreadyDisabled", "Join requests are already disabled.");

        // Message group invite
        getConfig().setDefault("messageGroupInviteSyntax", "You can generate keys for your group by using the command '!group invite <AMOUNT_OF_KEYS_YOU_WANT_TO_GENERATE>'. You can then send the generated keys to your friends. They in turn can redeem their code by expanding the 'Permissions' dropdown menu in their Teamspeak application and then select 'Use Privilege Key' just at the bottom. You can list existing keys by using the command '!group invites'.");
        getConfig().setDefault("messageGroupInviteFailedMaxInviteExceeded", "You've exceeded the maximum number of open invite keys. Use '!group invites' to list them.");
        getConfig().setDefault("messageGroupInviteFailedMaxInviteGenerationAtATimeExceeded", "You can not generate more than 5 invites at once.");
        getConfig().setDefault("messageGroupInviteFailedInvalidAmount", "The number of codes to generate is not valid. Please check for typos.");

        // Message Group Ownership Transfer
        getConfig().setDefault("messageGroupManageOwnershipTransferSuccess", "Ownership has been transferred.");
        getConfig().setDefault("messageGroupManageOwnershipTransferFailedClientNotMember", "Ownership can not be transferred to clients who are not member of this group.");
        getConfig().setDefault("messageGroupManageOwnershipTransferFailedClientNotFound", "Could not find client.");

        // Message Group Delete
        getConfig().setDefault("messageGroupDeleteNoPermission", "You don't have the permission to delete this group.");
        getConfig().setDefault("messageGroupDeleteNoGroup", "You aren't owner of this group.");
        getConfig().setDefault("messageGroupDeleteWarn", "Are you sure you want to delete this group? Use '!group delete IAMSURE' to confirm.");
        getConfig().setDefault("messageGroupDeleteSuccess", "Your group has been deleted.");
        getConfig().setDefault("messageGroupDeleteSyntax", "Syntax: !group delete <GROUP>");

        // Message Group Kick
        getConfig().setDefault("messageGroupMemberKickSuccess", "Member has been removed.");
        getConfig().setDefault("messageGroupMemberKickFailedNotFound", "Sorry, but this user can't be found.");

        getConfig().setDefault("messageErrorUnknown", "Sorry, but an unknown error occurred.");
        getConfig().setDefault("messageErrorNoPermission", "Sorry, but you don't have permission for this action.");
        getConfig().setDefault("messageSyntax", "Syntax: !group <join | request | delete>");

        // Message Admin
        getConfig().setDefault("messageAdminGroupRequestPending", "There are new group requests awaiting validation. Use '!group requests' to list all requests. Use '!group validate <GROUP>' to validate it.");
        getConfig().setDefault("messageAdminGroupRequestDeclined", "Group request has been declined due to following reason: ");
        getConfig().setDefault("messageAdminGroupRequestDeclinedFailed", "Could not decline group request.");
        getConfig().setDefault("messageAdminGroupRequestDeclinedMissingReason", "Please specify a reason.");
        getConfig().setDefault("messageAdminGroupRequestAccepted", "Group request has been accepted.");
        getConfig().setDefault("messageAdminGroupRequestNotFound", "Group request could not be found.");
        getConfig().setDefault("messageAdminGroupRequestSyntax", "Syntax: !group <validate | decline> <GROUPNAME>");
        getConfig().setDefault("messageAdminGroupRequestListNoRequests", "There are currently no open group requests.");

        getConfig().setDefault("messageAdminGroupDeleteValidate", "Are you sure you want to delete this group? Use '!group delete <GROUPNAME> IAMSURE' to confirm.");
        getConfig().setDefault("messageAdminGroupDeleteSuccess", "Group has been deleted.");
        getConfig().setDefault("messageAdminGroupDeleteFailedBlacklist", "This group can not be deleted.");
        getConfig().setDefault("messageAdminGroupDeleteFailedNotFound", "Group not found.");
        getConfig().setDefault("messageAdminGroupDeleteSyntax", "Syntax: !group delete <GROUPNAME>");

        RequestManager rm = new RequestManager(getLogger(), getAPI(), getConfig());
        rm.loadRequests();
        registerChatCommand(new CGroup(getAPI(), getConfig(), rm, getLogger()), "!group");

        getConfig().saveAll();

    }

    @Override
    public void onDisable() {


    }

    @Override
    public void onClientJoin(ClientJoinEvent clientJoinEvent) {
        super.onClientJoin(clientJoinEvent);
    }
}
