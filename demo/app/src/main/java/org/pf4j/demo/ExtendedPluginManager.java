package org.pf4j.demo;

import org.pf4j.*;
import org.pf4j.spring.SingletonSpringExtensionFactory;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;

public class ExtendedPluginManager extends SpringPluginManager {
    private static final Logger log = LoggerFactory.getLogger(ExtendedPluginManager.class);
    private PluginManager systemPluginManager;

    public ExtendedPluginManager(PluginManager _systemPluginManager){
        super();
        this.systemPluginManager = _systemPluginManager;
    }
    public ExtendedPluginManager() {
    }

    public ExtendedPluginManager(Path pluginsRoot) {
        super(pluginsRoot);
    }

    @Override
    protected ExtensionFactory createExtensionFactory() {
        return new SingletonSpringExtensionFactory(this);
    }
    /**
     * 如果系统插件中已包含同名插件，则该插件为不合法插件
     *
     * @param descriptor the plugin descriptor to validate
     * @throws PluginException if validation fails
     */
    @Override
    protected void validatePluginDescriptor(PluginDescriptor descriptor) throws PluginException {
        super.validatePluginDescriptor(descriptor);
        if(Objects.nonNull(systemPluginManager) && Objects.nonNull(systemPluginManager.getPlugin(descriptor.getPluginId()))){
            PluginWrapper pluginWrapper = systemPluginManager.getPlugin(descriptor.getPluginId());
            System.out.println(String.format("{%s}'s SystemPlugin which already loaded , Ignore ExtendedPlugin {%s} ",descriptor.getPluginId(),descriptor.toString()));
            throw new PluginAlreadyLoadedException(pluginWrapper.getPluginId(),pluginWrapper.getPluginPath());
        }
    }
}
