package dev.tildejustin.priority;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

public interface Kernel32Ext extends Library {
    Kernel32Ext INSTANCE = Native.load("kernel32", Kernel32Ext.class);

    @SuppressWarnings("UnusedReturnValue")
    boolean SetPriorityClass(WinNT.HANDLE hProcess, WinDef.DWORD dwProcessClass);

    WinDef.DWORD GetPriorityClass(WinNT.HANDLE hProcess);
}
