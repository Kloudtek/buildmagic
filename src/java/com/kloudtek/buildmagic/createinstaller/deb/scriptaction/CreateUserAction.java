/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.createinstaller.deb.scriptaction;

import com.kloudtek.buildmagic.createinstaller.deb.*;
import com.kloudtek.buildmagic.util.ant.AntAttribute;
import com.kloudtek.buildmagic.util.ant.AntValidator;

public class CreateUserAction extends ScriptAction {
    @AntAttribute(required = true)
    private String username;
    private String group;
    private String fullName;
    private boolean system = true;
    private String home;
    private String shell;
    private boolean createHomeDir = true;

    public CreateUserAction(final DebScript debScript) {
        super(debScript);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(final boolean system) {
        this.system = system;
    }

    public String getHome() {
        return home;
    }

    public void setHome(final String home) {
        this.home = home;
    }

    public String getShell() {
        return shell;
    }

    public void setShell(final String shell) {
        this.shell = shell;
    }

    public boolean isCreateHomeDir() {
        return createHomeDir;
    }

    public void setCreateHomeDir(final boolean createHomeDir) {
        this.createHomeDir = createHomeDir;
    }

    @Override
    public void process() {
        AntValidator.validate(this);
        final StringBuilder addUser = new StringBuilder("os.system('getent passwd ").append(username);
        addUser.append(" >/dev/null 2>&1 || adduser --disabled-password");
        if (system) {
            addUser.append(" --system");
        }
        if (home != null) {
            addUser.append(" --home ").append(home);
        }
        if (!createHomeDir) {
            addUser.append(" --no-create-home");
        }
        if (group != null) {
            addUser.append(" --ingroup ").append(group);
        }
        if (fullName != null) {
            addUser.append(" --gecos \"").append(fullName).append("\"");
        }
        if (shell != null) {
            addUser.append(" --shell ").append(shell);
        }
        addUser.append(" ").append(username).append("')");
        debScript.addStageLine(DebScript.MAIN,addUser.toString());
    }
}
