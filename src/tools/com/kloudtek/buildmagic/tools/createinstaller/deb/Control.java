/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb;

import org.apache.tools.ant.types.ResourceCollection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Control {
    private final ArrayList<Field> fields = new ArrayList<Field>();
    private final ArrayList<ResourceCollection> resources = new ArrayList<ResourceCollection>();
    private final ArrayList<TemplateEntry> templateEntries = new ArrayList<TemplateEntry>();
    private final ArrayList<DebScript> scripts = new ArrayList<DebScript>();
    private final ArrayList<Action> actions = new ArrayList<Action>();
    private final CreateDebTask createDebTask;
    private boolean purge = true;

    public Control(final CreateDebTask createDebTask) {
        this.createDebTask = createDebTask;
    }

    // Other

    @SuppressWarnings({"UnusedDeclaration"})
    public void setDepends(final String depends) {
        fields.add(new Field("Depends", depends));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Field createField() {
        final Field field = new Field();
        fields.add(field);
        return field;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public TemplateEntry createTemplate() {
        final TemplateEntry entry = new TemplateEntry();
        templateEntries.add(entry);
        return entry;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public DebScript createScript() {
        final DebScript script = new DebScript(createDebTask);
        scripts.add(script);
        return script;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addFileset(final org.apache.tools.ant.types.FileSet set) {
        addResource(set);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addZipGroupFileset(final org.apache.tools.ant.types.FileSet set) {
        addResource(set);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addZipfileset(final org.apache.tools.ant.types.ZipFileSet set) {
        addResource(set);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addResource(final ResourceCollection col) {
        getResources().add(col);
    }

    public boolean isPurge() {
        return purge;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setPurge(final boolean purge) {
        this.purge = purge;
    }

    public ArrayList<Field> getFields() {
        return fields;
    }

    public ArrayList<ResourceCollection> getResources() {
        return resources;
    }

    public ArrayList<TemplateEntry> getTemplateEntries() {
        return templateEntries;
    }

    @NotNull
    public ArrayList<DebScript> getScripts() {
        return scripts;
    }

    public DebScript getScript(final String name) {
        for (final DebScript script : scripts) {
            if (script.getName().equalsIgnoreCase(name)) {
                return script;
            }
        }
        return new DebScript(createDebTask, name);
    }

    public class Field {
        private String name;
        private String value = "";

        public Field() {
        }

        public Field(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public void setName(final String name) {
            this.name = name;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public void setValue(final String value) {
            this.value = value;
        }

        @SuppressWarnings({"UnusedDeclaration"})
        public void addText(final String value) {
            this.value += createDebTask.getProject().replaceProperties(value);
        }
    }
}
