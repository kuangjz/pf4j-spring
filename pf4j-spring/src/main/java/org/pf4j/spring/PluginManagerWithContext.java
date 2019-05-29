package org.pf4j.spring;

import org.pf4j.PluginManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public interface PluginManagerWithContext extends PluginManager, ApplicationContextAware {
    public ApplicationContext getApplicationContext();
    public PluginManager getSystemPluginManager();
    public PluginManager getExtendedPluginManager();
}
