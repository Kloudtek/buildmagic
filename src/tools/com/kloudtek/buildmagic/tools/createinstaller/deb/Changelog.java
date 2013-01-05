/*
 * Copyright (c) KloudTek Ltd 2013.
 */

package com.kloudtek.buildmagic.tools.createinstaller.deb;

import org.apache.tools.ant.BuildException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Changelog {
    private String packageName;
    private String[] distributions;
    private ArrayList<Entry> entries = new ArrayList<Entry>();

    public Changelog(String packageName) {
        this.packageName = packageName;
    }

    public String[] getDistributions() {
        return distributions;
    }

    public void setDistributions(String... distributions) {
        this.distributions = distributions;
    }

    public void validate() {
        if (distributions == null || distributions.length == 0) {
            for (Entry entry : entries) {
                if (entry.distributions == null || entry.distributions.length == 0) {
                    throw new BuildException("distribution not specified");
                }
            }
        }
        // TODO
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public Entry createEntry() {
        Entry entry = new Entry();
        entries.add(entry);
        return entry;
    }

    public String export(Type type) {
        StringBuilder buf = new StringBuilder();
        switch (type) {
            case DEBIAN:
                final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ");
                for (Entry entry : entries) {
                    if (entry.distributions == null || entry.distributions.length == 0) {
                        entry.distributions = distributions;
                    }
                    buf.append(packageName).append(" (").append(entry.version).append(") ");
                    for (int i = 0; i < entry.distributions.length; i++) {
                        buf.append(entry.distributions[i]).append(" ");
                    }
                    buf.append("; urgency=").append(entry.urgency.name().toLowerCase()).append("\n\n");
                    for (String change : entry.changes) {
                        buf.append("  * ").append(change).append("\n");
                    }
                    buf.append("\n -- ").append(entry.maintName).append(" <").append(entry.maintEmail).append("> ")
                            .append(dateFormat.format(entry.date)).append("\n");
                }
        }
        return buf.toString();
    }

    public enum Type {
        DEBIAN
    }

    public class Entry {
        private String version;
        private String[] distributions;
        private Urgency urgency = Urgency.LOW;
        private String[] changes;
        private String maintName;
        private String maintEmail;
        private Date date;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String[] getDistributions() {
            return distributions;
        }

        public void setDistributions(String[] distributions) {
            this.distributions = distributions;
        }

        public Urgency getUrgency() {
            return urgency;
        }

        public void setUrgency(Urgency urgency) {
            this.urgency = urgency;
        }

        public String[] getChanges() {
            return changes;
        }

        public void setChanges(String[] changes) {
            this.changes = changes;
        }

        public void setChanges(String changes) {
            this.changes = new String[]{changes};
        }

        public String getMaintName() {
            return maintName;
        }

        public void setMaintName(String maintName) {
            this.maintName = maintName;
        }

        public String getMaintEmail() {
            return maintEmail;
        }

        public void setMaintEmail(String maintEmail) {
            this.maintEmail = maintEmail;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    public enum Urgency {
        LOW, MEDIUM, HIGH, EMERGENCY, CRITICAL
    }
}
