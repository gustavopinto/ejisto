/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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

package com.ejisto.modules.gui.components;

import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.ChangeServerStatus;
import lombok.extern.log4j.Log4j;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;

import static com.ejisto.modules.gui.components.EjistoDialog.DEFAULT_WIDTH;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.registerApplicationEventListener;

@Log4j
public class LogViewer extends JXPanel {
    private static final long serialVersionUID = 2849704565034218976L;
    private JTextArea logText;
    private JScrollPane logPanel;
    private final transient ApplicationListener<ChangeServerStatus> listener = new ApplicationListener<ChangeServerStatus>() {
        @Override
        public void onApplicationEvent(ChangeServerStatus event) {
            if (event.getCommand() == ChangeServerStatus.Command.STARTUP) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            reset();
                        }
                    });
                } catch (Exception e) {
                    log.error("exception during log reset", e);
                }
            }
        }

        @Override
        public Class<ChangeServerStatus> getTargetEventType() {
            return ChangeServerStatus.class;
        }
    };

    public LogViewer() {
        super();
        registerApplicationEventListener(listener);
        init();
    }

    private void init() {
        setName(getMessage("main.tab.log.text"));
        setLayout(new BorderLayout());
        add(getLogPanel(), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder());
    }

    private JScrollPane getLogPanel() {
        if (this.logPanel != null) {
            return this.logPanel;
        }
        logText = new JTextArea();
        logPanel = new JScrollPane(logText);
        logPanel.setMinimumSize(new Dimension(DEFAULT_WIDTH, 100));
        logText.setEditable(false);
        logText.setFont(new java.awt.Font("Monospaced", 0, 9));
        return logPanel;
    }

    public void log(String message) {
        logText.append(message);
    }

    public void reset() {
        logText.setText("");
    }


}
