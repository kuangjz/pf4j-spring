package org.pf4j.demo;

import org.pf4j.*;
import org.pf4j.spring.PluginManagerWithContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.*;

public class SpringBootPluginManager implements PluginManagerWithContext {
    private static final Logger log = LoggerFactory.getLogger(SpringBootPluginManager.class);
    private ApplicationContext applicationContext;
    private SystemPluginManager systemPluginManager;
    private ExtendedPluginManager extendedPluginManager;

    public SpringBootPluginManager() {
    }

    @PostConstruct
    public void init() {
        System.out.println(String.format("{%s}'init()\t=========ApplicationContext.class {%s}==========",getClass().getName(),this.getApplicationContext()));
        AbstractAutowireCapableBeanFactory beanFactory = (AbstractAutowireCapableBeanFactory) this.getApplicationContext().getAutowireCapableBeanFactory();

        systemPluginManager = new SystemPluginManager();
        systemPluginManager.setApplicationContext(this.getApplicationContext());

        systemPluginManager.loadPlugins();
        systemPluginManager.startPlugins();
        SpringBootExtensionsInjector systemPluginExtensionsInjector = new SpringBootExtensionsInjector(systemPluginManager, beanFactory);
        systemPluginExtensionsInjector.injectExtensions();

        extendedPluginManager = new ExtendedPluginManager(systemPluginManager);
        extendedPluginManager.setApplicationContext(this.getApplicationContext());
        extendedPluginManager.loadPlugins();
        extendedPluginManager.startPlugins();
        SpringBootExtensionsInjector extendedExtensionsInjector = new SpringBootExtensionsInjector(extendedPluginManager, beanFactory);
        extendedExtensionsInjector.injectExtensions();
    }
    @Override
    public PluginManager getSystemPluginManager(){
        return this.systemPluginManager;
    }
    @Override
    public PluginManager getExtendedPluginManager(){
        return this.extendedPluginManager;
    }

    @Override
    public List<PluginWrapper> getPlugins() {
        List<PluginWrapper> pluginWrapperList = this.systemPluginManager.getPlugins();
        pluginWrapperList.addAll(this.extendedPluginManager.getPlugins());
        return pluginWrapperList;
    }

    @Override
    public List<PluginWrapper> getPlugins(PluginState pluginState) {
        List<PluginWrapper> pluginWrapperList = this.systemPluginManager.getPlugins(pluginState);
        pluginWrapperList.addAll(this.extendedPluginManager.getPlugins(pluginState));
        return pluginWrapperList;
    }

    @Override
    public List<PluginWrapper> getResolvedPlugins() {
        List<PluginWrapper> pluginWrapperList = this.systemPluginManager.getResolvedPlugins();
        pluginWrapperList.addAll(this.extendedPluginManager.getResolvedPlugins());
        return pluginWrapperList;
    }

    @Override
    public List<PluginWrapper> getUnresolvedPlugins() {
        List<PluginWrapper> pluginWrapperList = this.systemPluginManager.getUnresolvedPlugins();
        pluginWrapperList.addAll(this.extendedPluginManager.getUnresolvedPlugins());
        return pluginWrapperList;
    }

    @Override
    public List<PluginWrapper> getStartedPlugins() {
        List<PluginWrapper> pluginWrapperList = this.systemPluginManager.getStartedPlugins();
        pluginWrapperList.addAll(this.extendedPluginManager.getStartedPlugins());
        return pluginWrapperList;
    }

    @Override
    public PluginWrapper getPlugin(String pluginId) {
        PluginWrapper pluginWrapper = this.systemPluginManager.getPlugin(pluginId);
        if(pluginWrapper == null) {
            pluginWrapper = this.extendedPluginManager.getPlugin(pluginId);
        }
        return pluginWrapper;
    }

    @Override
    public void loadPlugins() {

    }

    @Override
    public String loadPlugin(Path pluginPath) {
        return null;
    }

    @Override
    public void startPlugins() {

    }

    @Override
    public PluginState startPlugin(String pluginId) {
        return null;
    }

    @Override
    public void stopPlugins() {

    }

    @Override
    public PluginState stopPlugin(String pluginId) {
        return null;
    }

    @Override
    public boolean unloadPlugin(String pluginId) {
        return false;
    }

    @Override
    public boolean disablePlugin(String pluginId) {
        return false;
    }

    @Override
    public boolean enablePlugin(String pluginId) {
        return false;
    }

    @Override
    public boolean deletePlugin(String pluginId) {
        return false;
    }

    @Override
    public ClassLoader getPluginClassLoader(String pluginId) {
        return null;
    }

    @Override
    public List<Class<?>> getExtensionClasses(String pluginId) {
        return null;
    }

    @Override
    public <T> List<Class<T>> getExtensionClasses(Class<T> type) {
        return null;
    }

    @Override
    public <T> List<Class<T>> getExtensionClasses(Class<T> type, String pluginId) {
        return null;
    }

    @Override
    public <T> List<T> getExtensions(Class<T> type) {
        return null;
    }

    @Override
    public <T> List<T> getExtensions(Class<T> type, String pluginId) {
        return null;
    }

    @Override
    public List getExtensions(String pluginId) {
        return null;
    }

    @Override
    public Set<String> getExtensionClassNames(String pluginId) {
        return null;
    }

    @Override
    public ExtensionFactory getExtensionFactory() {
        return null;
    }

    @Override
    public RuntimeMode getRuntimeMode() {
        return null;
    }

    @Override
    public PluginWrapper whichPlugin(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        List<PluginWrapper> resolvedPlugins = this.getResolvedPlugins();
        for (PluginWrapper plugin : resolvedPlugins) {
            if (plugin.getPluginClassLoader() == classLoader) {
                return plugin;
            }
        }
        return null;
    }

    @Override
    public void addPluginStateListener(PluginStateListener listener) {

    }

    @Override
    public void removePluginStateListener(PluginStateListener listener) {

    }

    @Override
    public void setSystemVersion(String version) {

    }

    @Override
    public String getSystemVersion() {
        return null;
    }

    @Override
    public Path getPluginsRoot() {
        return this.extendedPluginManager.getPluginsRoot();
    }

    @Override
    public VersionManager getVersionManager() {
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
