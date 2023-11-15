package dev.tildejustin.priority;

import com.google.common.io.Resources;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.julti.Julti;
import xyz.duncanruns.julti.JultiAppLaunch;
import xyz.duncanruns.julti.JultiOptions;
import xyz.duncanruns.julti.gui.JultiGUI;
import xyz.duncanruns.julti.management.InstanceManager;
import xyz.duncanruns.julti.plugin.PluginEvents;
import xyz.duncanruns.julti.plugin.PluginInitializer;
import xyz.duncanruns.julti.plugin.PluginManager;
import xyz.duncanruns.julti.util.FileUtil;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;

public class PriorityPlugin implements PluginInitializer {
    private final Path config = JultiOptions.getJultiDir().resolve("priority.txt");
    private PriorityClass priorityClass = PriorityClass.ABOVE_NORMAL_PRIORITY_CLASS;

    public static void main(String[] args) throws IOException {
        JultiAppLaunch.launchWithDevPlugin(args, PluginManager.JultiPluginData.fromString(Resources.toString(
                Resources.getResource(PriorityPlugin.class, "/julti.plugin.json"), Charset.defaultCharset()
        )), new PriorityPlugin());
    }

    @Override
    public void initialize() {
        try {
            if (this.config.toFile().createNewFile()) FileUtil.writeString(this.config, this.priorityClass.toString());
            this.priorityClass = PriorityClass.nameToObject.get(FileUtil.readString(this.config));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PluginEvents.RunnableEventType.ALL_INSTANCES_FOUND.register(() ->
                InstanceManager.getInstanceManager().getInstances().forEach(instance -> {
                    WinNT.HANDLE handle = Kernel32.INSTANCE.OpenProcess(
                            WinNT.PROCESS_SET_INFORMATION | WinNT.PROCESS_QUERY_LIMITED_INFORMATION,
                            true, instance.getPid()
                    );
                    Julti.log(Level.INFO, "setting instance: " + instance.getName() +
                            " to priority: " + this.priorityClass.toString() +
                            ", was: " + PriorityClass.valueToObject.get((Kernel32Ext.INSTANCE.GetPriorityClass(handle))).toString() +
                            ", pid: " + instance.getPid()
                    );
                    Kernel32Ext.INSTANCE.SetPriorityClass(handle, this.priorityClass.DWORD);
                    Kernel32.INSTANCE.CloseHandle(handle);
                })
        );
    }

    @Override
    public String getMenuButtonName() {
        return "Open";
    }

    @Override
    public void onMenuButtonPress() {
        JComboBox<String> prioritySelector = new JComboBox<>(
                Arrays.stream(PriorityClass.values())
                        .map(PriorityClass::toString)
                        .toArray(String[]::new)
        );
        prioritySelector.setSelectedItem(this.priorityClass.toString());
        prioritySelector.addActionListener(action -> {
            this.priorityClass = PriorityClass.nameToObject.get(((String) prioritySelector.getSelectedItem()));
            try {
                FileUtil.writeString(this.config, this.priorityClass.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        JOptionPane.showMessageDialog(
                JultiGUI.getPluginsGUI(),
                prioritySelector,
                "Priority Selector",
                JOptionPane.QUESTION_MESSAGE
        );
    }
}
