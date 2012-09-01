/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb;

import com.kloudtek.buildmagic.tools.createinstaller.deb.scriptaction.*;
import com.kloudtek.buildmagic.tools.util.PythonScriptWriter;
import org.apache.tools.ant.BuildException;

import java.util.*;

/**
 * Debian script
 */
public class DebScript {
    public static final String CFG = "configure";
    public static final String INSTALL = "install";
    public static final String PREINST = "preinst";
    public static final String POSTINST = "postinst";
    public static final List<String> PRIORITIES = Arrays.asList("LOW", "MEDIUM", "HIGH", "CRITICAL");
    public static final String PREDEBCONF = "predebconf";
    public static final String POSTDEBCONF = "postdebconf";
    public static final String MAIN = "main";
    public static final List<String> SPSTAGES = Arrays.asList(PREDEBCONF, POSTDEBCONF, MAIN);
    private final ArrayList<ScriptAction> scriptActions = new ArrayList<ScriptAction>();
    private String name;
    private String actions;
    private PythonScriptWriter script;
    private final CreateDebTask createDebTask;
    private final Set<String> availableActions = new HashSet<String>();
    private final Map<String, Stage> specialStages = new HashMap<String, Stage>();
    private final ArrayList<Stage> stages = new ArrayList<Stage>();
    private final Map<String, Stage> stagesIdx = new HashMap<String, Stage>();
    private final ArrayList<String> stageNames = new ArrayList<String>();
    private final Set<String> imports = new HashSet<String>();

    public DebScript(final CreateDebTask createDebTask) {
        this.createDebTask = createDebTask;
        for (final String name : SPSTAGES) {
            specialStages.put(name, new Stage(name));
        }
        imports.add("sys");
        imports.add("os");
        imports.add("debconf");
        imports.add("subprocess");
    }

    public DebScript(final CreateDebTask createDebTask, final String name) {
        this(createDebTask);
        this.name = name;
    }

    public void init() {
        for (final ScriptAction action : scriptActions) {
            if (action.stage == null) {
                action.stage = "stage" + scriptActions.indexOf(action);
            }
        }
        for (final ScriptAction action : scriptActions) {
            action.init();
        }
    }

    public void process() {
        for (final ScriptAction action : scriptActions) {
            action.process();
        }
        for (int i = 0; i < stages.size(); i++) {
            final Stage stage = stages.get(i);
            if (i > 0) {
                stage.previous = stages.get(i - 1).name;
            }
            final int nextStageIdx = i + 1;
            if (nextStageIdx < stages.size()) {
                stage.next = stages.get(nextStageIdx).name;
            } else if (nextStageIdx == stages.size()) {
                stage.next = "end";
            }
        }
    }

    public CreateDebTask getCreateDebTask() {
        return createDebTask;
    }

    public ArrayList<ScriptAction> getScriptActions() {
        return scriptActions;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setName(final String name) {
        // TODO validate valid name
        this.name = name;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setActions(final String actions) {
        this.actions = actions;
    }

    private <X extends ScriptAction> X add(final X scriptAction) {
        scriptActions.add(scriptAction);
        return scriptAction;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public RawScriptLineAction createLine() {
        return add(new RawScriptLineAction(this));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public ScriptInputAction createInput() {
        return add(new ScriptInputAction(this));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public ScriptImportAction createImport() {
        return add(new ScriptImportAction(this));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public CreateGroupAction createCreateGroup() {
        return add(new CreateGroupAction(this));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public CreateUserAction createCreateUser() {
        return add(new CreateUserAction(this));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public ExecAction createExec() {
        return add(new ExecAction(this));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public ConfirmLicenseAction createConfirmlicense() {
        return add(new ConfirmLicenseAction(this));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public ValidateAction createValidation() {
        return add(new ValidateAction(this));
    }

    public String getName() {
        return name;
    }

    public void addStageLine(final String stageName, final String line) {
        getStage(stageName, true).add(line);
    }

    public void addNavStageLine(final String stageName, final String line) {
        getStage(stageName, false).addNav(line);
    }

    public void addImport(final String pkg) {
        imports.add(pkg);
    }

    private Stage getStage(final String name, final boolean newStageAllowed) {
        if (SPSTAGES.contains(name)) {
            return specialStages.get(name);
        }
        Stage stage = stagesIdx.get(name);
        if (stage == null && newStageAllowed) {
            stage = new Stage(name);
        } else if (stage == null) {
            throw new BuildException("stage does not exist, and cannot be created: " + name);
        }
        return stage;
    }

    public synchronized void addFailLine(final String stageName, final String errorMsg, final int errorCode) {
        addStageLine(stageName, "sys.stderr.write('" + errorMsg + "')");
        addStageLine(stageName, "sys.exit(" + errorCode + ")");
    }

    public boolean isScriptEmpty() {
        for (final Stage stage : specialStages.values()) {
            if (!stage.getLines().isEmpty()) {
                return false;
            }
        }
        for (final Stage stage : stages) {
            if (!stage.getLines().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    void writeDebugMsg(final String msg) {
        script.doIf("debugenabled");
        script.stderr(msg);
        script.endBlock();
    }

    public byte[] generate() {
        script = new PythonScriptWriter();
        script.line("#!/usr/bin/python\n\n");
        for (final String mod : imports) {
            script.doImport(mod);
        }
        script.defMethod("debug", "txt");
        script.doIf("debugenabled");
        script.line("sys.stderr.write(txt)");
        script.lastLine("sys.stderr.write('\\n')");
        script.endBlock();
        script.newLine();

        script.defClass("DebConfVars");
        script.line("pass");
        script.endBlock();
        script.newLine();

        specialStages.get(PREDEBCONF).writeStage();
        for (final Stage stage : stages) {
            stage.writeStage();
            stage.writeStageNavigation();
        }
        specialStages.get(POSTDEBCONF).writeStage();
        specialStages.get(MAIN).writeStage();
        script.newLine();
        script.doTry();
        script.line("os.environ['PERL_DL_NONLAZY'] = '1'");
        script.line("os.environ['DEBIAN_HAS_FRONTEND']");
        script.except();
        script.line("os.execv(debconf._frontEndProgram, [debconf._frontEndProgram] + sys.argv)");
        script.endBlock();
        script.line("db = debconf.Debconf()");
        script.line("db.capb('backup')");
        script.doTry();
        script.line("debugenv = os.environ['DEBCONF_DEBUG']");
        script.line("debugenabled = debugenv == 'developer'");
        script.except();
        script.line("debugenabled = None");
        script.endBlock();
        script.doIf("len(sys.argv) > 1");
        script.line("action = sys.argv[1]");
        script.doElse();
        script.line("action = None");
        script.endBlock();
        script.doIf("len(sys.argv) > 2");
        script.line("version = sys.argv[2]");
        script.doElse();
        script.line("version = None");
        script.endBlock();
        if (actions != null && actions.trim().length() > 0) {
            final StringTokenizer tok = new StringTokenizer(actions, ",");
            assert tok.hasMoreElements();
            final StringBuilder buf = new StringBuilder();
            buf.append("action != '").append(tok.nextElement()).append("'");
            while (tok.hasMoreElements()) {
                buf.append(" and action != '").append(tok.nextElement()).append("'");
            }
            script.doIf(buf.toString());
            script.lastLine("sys.exit(0)");
        }
        script.line("vars = DebConfVars()");
        script.line("returned = 0");
        script.line("running = 1");
        script.line("previousStages = []");
        if (!stages.isEmpty()) {
            script.line("stage = '" + stages.get(0).name + "'");
            specialStages.get(PREDEBCONF).writeCallStage();
            script.doWhile("stage != 'end'");
            script.startElseIf();
            for (final Stage stage : stages) {
                script.doElseIf("stage == '" + stage.name + "'");
                stage.writeCallStage();
            }
            script.endElseIf();
            script.line("returned = 0");
            script.doTry();
            script.line("db.go()");
            script.line("previousStages.append(stage)");
            script.startElseIf();
            for (final Stage stage : stages) {
                script.doElseIf("stage == '" + stage.name + "'");
                stage.writeCallNavStage("stage");
                script.doIf("not stage");
                script.line("stage = '" + stage.next + "'");
                script.stderr("Stage not specified defaulting to:'+stage+'");
                script.endBlock();
            }
            script.endElseIf();
            script.doIf("stage == 'exit'");
            script.lastLine("sys.exit(0)");
            script.line("debug('Stage Nav: '+stage)");
            script.except("debconf.DebconfError as e");
            script.doIf("e.args[0] == 30");
            writeDebugMsg("Going back to previous stage");
            script.line("returned = 1");
            script.doIf("len(previousStages) > 0");
            script.line("stage = previousStages.pop()");
            script.endBlock();
            script.doElse();
            script.lastLine("raise");
            script.endBlock();
            script.endBlock();
            specialStages.get(POSTDEBCONF).writeCallStage();
        }
        specialStages.get(MAIN).writeCallStage();
        return script.getBytes();
    }

    public CreateDebTask getTask() {
        return createDebTask;
    }

    private class Stage {
        private final String name;
        private final ArrayList<String> lines = new ArrayList<String>();
        private final ArrayList<String> navLines = new ArrayList<String>();
        public String previous;
        public String next;

        private Stage(final String name) {
            if (!stageNames.contains(name)) {
                stageNames.add(name);
            }
            if (SPSTAGES.contains(name)) {
                specialStages.put(name, this);
            } else {
                stages.add(this);
                stagesIdx.put(name, this);
            }
            this.name = name;
        }

        public void add(final String line) {
            lines.add(line);
        }

        public void addNav(final String line) {
            navLines.add(line);
        }

        public ArrayList<String> getLines() {
            return lines;
        }

        public ArrayList<String> getNavLines() {
            return navLines;
        }

        public String getNavMethodName() {
            return "stage_" + name + "_nav";
        }

        private void writeCallStage() {
            script.line("stage_" + name + "(db,vars,version,returned)");
        }

        private void writeCallNavStage(final String retValue) {
            script.line(retValue + " = stage_" + name + "_nav(db,vars,version)");
        }

        private void writeStage() {
            script.defMethod("stage_" + name, "db,vars,version,returned");
            writeDebugMsg("starting main stage " + name);
            for (final String line : lines) {
                script.line(line);
            }
            writeDebugMsg("ended main stage " + name);
            script.endBlock();
        }

        private void writeStageNavigation() {
            script.defMethod("stage_" + name + "_nav", "db,vars,version");
            if (navLines.isEmpty()) {
                if (next == null) {
                    script.line("running = 0");
                    script.line("return 'finished'");
                } else {
                    script.line("return None");
                }
            } else {
                for (final String line : navLines) {
                    script.line(line);
                }
            }
            script.endBlock();
        }
    }

}
