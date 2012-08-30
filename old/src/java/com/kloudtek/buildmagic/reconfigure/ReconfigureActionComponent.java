/*
 * Copyright (c) $today.year.jGuild International Ltd
 */

package com.kloudtek.buildmagic.reconfigure;

import org.apache.tools.ant.taskdefs.condition.ConditionBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class ReconfigureActionComponent extends ConditionBase {
    private ArrayList<ReconfigureActionComponent> components;
    protected Boolean strict;

    public Boolean isStrict() {
        return strict;
    }

    public void setStrict(final Boolean strict) {
        this.strict = strict;
    }

    @NotNull
    protected List<ReconfigureActionComponent> getComponents() {
        initComponentsList();
        return components;
    }

    @NotNull
    @SuppressWarnings({"unchecked"})
    protected <X extends ReconfigureActionComponent> List<X> getEnabledComponents(Class<X> componentClass) {
        if (components == null) {
            return Collections.emptyList();
        }
        ArrayList<X> list = new ArrayList<X>();
        for (ReconfigureActionComponent component : components) {
            if (componentClass.isInstance(component) && component.isEnabled()) {
                list.add((X) component);
            }
        }
        return list;
    }

    protected <X extends ReconfigureActionComponent> X addComponent(X component) {
        getComponents().add(component);
        return component;
    }

    private void initComponentsList() {
        if (components == null) {
            components = new ArrayList<ReconfigureActionComponent>();
        }
    }

    public boolean isEnabled() {
        return ReconfigureAction.evalConditions(getConditions());
    }
}
