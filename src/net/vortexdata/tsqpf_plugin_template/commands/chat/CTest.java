package net.vortexdata.tsqpf_plugin_template.commands.chat;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import net.vortexdata.tsqpf.listeners.ChatCommandInterface;

public class CTest implements ChatCommandInterface {

    private TS3Api api;

    public CTest(TS3Api api) {
        this.api = api;
    }

    @Override
    public void gotCalled(TextMessageEvent textMessageEvent) {
        api.sendPrivateMessage(textMessageEvent.getInvokerId(), "You just called the test command.");
    }

}
