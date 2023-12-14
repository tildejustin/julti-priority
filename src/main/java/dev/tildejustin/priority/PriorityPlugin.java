package dev.tildejustin.priority;

import com.google.common.io.*;
import com.sun.jna.platform.win32.*;
import org.apache.logging.log4j.*;
import xyz.duncanruns.julti.*;
import xyz.duncanruns.julti.gui.*;
import xyz.duncanruns.julti.management.*;
import xyz.duncanruns.julti.plugin.*;
import xyz.duncanruns.julti.util.*;

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
            this.priorityClass = PriorityClass.nameToObject.getOrDefault(FileUtil.readString(this.config), PriorityClass.ABOVE_NORMAL_PRIORITY_CLASS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PluginEvents.RunnableEventType.ALL_INSTANCES_FOUND.register(() ->
                InstanceManager.getInstanceManager().getInstances().forEach(instance -> {
                    Kernel32.HANDLE handle = Kernel32.INSTANCE.OpenProcess(
                            Kernel32.PROCESS_ALL_ACCESS, false, instance.getPid()
                    );
                    Julti.log(Level.INFO, "setting instance: " + instance.getName() +
                            " to priority: " + this.priorityClass.toString() +
                            ", was: " + PriorityClass.valueToObject.get((Kernel32.INSTANCE.GetPriorityClass(handle))).toString()
                    );
                    Julti.log(Level.INFO, "succeeded: " + Kernel32.INSTANCE.SetPriorityClass(handle, this.priorityClass.DWORD));
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
