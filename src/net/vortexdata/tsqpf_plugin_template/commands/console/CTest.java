package net.vortexdata.tsqpf_plugin_template.commands.console;

import net.vortexdata.tsqpf.Framework;
import net.vortexdata.tsqpf.commands.CommandInterface;
import net.vortexdata.tsqpf.console.Logger;
import net.vortexdata.tsqpf.console.VirtualTerminal;

public class CTest extends CommandInterface {

    private Framework framework;

    public CTest(Logger logger, Framework framework) {
        super(logger);
        this.framework = framework;
    }

    @Override
    public String getHelpMessage() {
        return "A test command that shuts down the framework.";
    }

    @Override
    public void gotCalled(String[] strings, VirtualTerminal virtualTerminal) {
        framework.shutdown();
    }

    @Override
    public String getName() {
        return "Test";
    }

}
