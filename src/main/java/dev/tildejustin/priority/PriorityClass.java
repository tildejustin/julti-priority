package dev.tildejustin.priority;

import com.sun.jna.platform.win32.WinDef;

import java.util.*;

public enum PriorityClass {
    REALTIME_PRIORITY_CLASS(0x00000100, "Realtime"),
    HIGH_PRIORITY_CLASS(0x00000080, "High"),
    ABOVE_NORMAL_PRIORITY_CLASS(0x00008000, "Above Normal"),
    NORMAL_PRIORITY_CLASS(0x00000020, "Normal"),
    BELOW_NORMAL_PRIORITY_CLASS(0x00004000, "Below Normal"),
    IDLE_PRIORITY_CLASS(0x00000040, "Idle"),
    PROCESS_MODE_BACKGROUND_BEGIN(0x00100000),
    PROCESS_MODE_BACKGROUND_END(0x00200000);

    public static final Map<String, PriorityClass> nameToObject = new HashMap<>();
    public static final Map<WinDef.DWORD, PriorityClass> valueToObject = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(priority -> {
            nameToObject.put(priority.toString(), priority);
            valueToObject.put(priority.DWORD, priority);
        });
    }

    public final WinDef.DWORD DWORD;
    private final String name;

    PriorityClass(int dword) {
        this(dword, null);
    }

    PriorityClass(int dword, String name) {
        this.DWORD = new WinDef.DWORD(dword);
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name != null ? this.name : super.toString();
    }
}
