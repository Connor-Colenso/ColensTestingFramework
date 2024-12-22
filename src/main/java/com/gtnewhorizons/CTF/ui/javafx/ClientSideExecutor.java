package com.gtnewhorizons.CTF.ui.javafx;

import java.util.LinkedList;
import java.util.Queue;

public class ClientSideExecutor {

    private static final Queue<Runnable> clientSideExecutionQueue = new LinkedList<>();

    public static void add(Runnable runnable) {
        clientSideExecutionQueue.add(runnable);
    }

    public static void runQueue() {
        while (!clientSideExecutionQueue.isEmpty()) {
            Runnable task = clientSideExecutionQueue.poll(); // Retrieve and remove the head of the queue
            if (task != null) {
                task.run(); // Execute the task
            }
        }
    }

}
