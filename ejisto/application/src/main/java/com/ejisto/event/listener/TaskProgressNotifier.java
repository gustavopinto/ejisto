/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
 *
 * Ejisto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ejisto is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ejisto.event.listener;

import com.ejisto.event.def.BlockingTaskProgress;
import com.ejisto.modules.controller.DialogController;
import com.ejisto.modules.executor.BackgroundTask;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.ProgressPanel;
import lombok.extern.log4j.Log4j;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/6/12
 * Time: 6:32 PM
 */
@Log4j
public class TaskProgressNotifier implements ApplicationListener<BlockingTaskProgress> {

    @Resource private Application application;
    @Resource TaskManager taskManager;
    private final ConcurrentMap<String, DialogController> activeControllers = new ConcurrentHashMap<String, DialogController>();
    private final AtomicReference<DialogController> currentController = new AtomicReference<DialogController>();

    @Override
    public void onApplicationEvent(final BlockingTaskProgress event) {
        if (event.isRunning()) {
            taskManager.addNewTask(new BackgroundTask<Void>(new Callable<Void>() {
                @Override
                public Void call() throws InterruptedException {
                    DialogController controller = DialogController.Builder.newInstance().
                            withContent(buildPanel()).
                            withParentFrame(application).
                            withDecorations(false).
                            withIconKey(event.getIconKey()).
                            withHeader(getMessage(event.getPanelTitle()), getMessage(event.getPanelDescription())).
                            build();
                    activeControllers.put(event.getId(), controller);
                    while (activeControllers.containsKey(event.getId()) && !currentController.compareAndSet(null,
                                                                                                            controller)) {
                        Thread.sleep(100L);
                    }
                    controller.showUndecorated(true);
                    return null;
                }
            }));
        } else {
            taskManager.addNewTask(new BackgroundTask<Void>(new Callable<Void>() {
                @Override
                public Void call() throws InterruptedException {
                    DialogController controller = activeControllers.get(event.getId());
                    while (controller == null || controller != currentController.get()) {
                        Thread.sleep(100L);
                        controller = activeControllers.get(event.getId());
                    }
                    currentController.compareAndSet(controller, null);
                    activeControllers.remove(event.getId());
                    controller.hide();
                    return null;
                }
            }));

        }
    }

    private JPanel buildPanel() {
        return new ProgressPanel();
    }
}