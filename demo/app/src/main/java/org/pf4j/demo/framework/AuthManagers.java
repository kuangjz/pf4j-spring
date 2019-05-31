package org.pf4j.demo.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org1.pf4j.demo.api.framework.AuthProvider;
import org1.pf4j.demo.api.framework.AuthenticationManager;

/**
 *
 * 认证管理，使用枚举类实现单例模式
 * 所有的认证插件，应该注册到本管理器上，
 * 具体业务逻辑后续补充，此时仅供参考。
 * Component manager for AuthenticationManagers
 *
 * @author sdai
 *
 */
public enum AuthManagers {
    INSTANCE;
    private Map<String, AuthProvider> authProviders;
    private String defaultAuthProvider;
//    private AuthManagerAdapter adapter;

    private AuthManagers() {
        authProviders = new HashMap<>();
        defaultAuthProvider = "viperfish";
//        adapter = new AuthManagerAdapter();
    }

    /**
     * register an authentication provider
     *
     * @param p
     *            the provider to register
     */
    public void registerAuthProvider(AuthProvider p) {
        authProviders.put(p.getName(), p);
        p.registerConfig();
    }

    /**
     * get all the providers
     *
     * @return all of the providers
     */
    public Map<String, AuthProvider> getAuthProviders() {
        return this.authProviders;
    }

    /**
     * get the name of the default authentication provider
     *
     * @return
     */
    public String getDefaultAuthProvider() {
        return defaultAuthProvider;
    }

    /**
     * set the name of the default authentication provider
     *
     * @param defaultAuthProvider
     */
    public void setDefaultAuthProvider(String defaultAuthProvider) {
        this.defaultAuthProvider = defaultAuthProvider;
    }

    /**
     * create a new Authentication Manager that is specified in the
     * configuration
     *
     * @return a new Authentication Manager
     */
    public AuthenticationManager newAuthManager() {
        return newAuthManager();
    }

    /**
     * create a new Authentication Manager based on the name parameter
     *
     * @param instance
     *            the type of the Authentication Manager
     * @return the new instance
     */
    public AuthenticationManager newAuthManager(String instance) {
        AuthenticationManager am = authProviders.get(defaultAuthProvider).newInstance();
        if (am != null) {
            return am;
//            adapter.setMger(am);
//            return adapter;
        }
        for (Entry<String, AuthProvider> i : authProviders.entrySet()) {
            AuthenticationManager result = i.getValue().newInstance(instance);
            if (result != null) {
                return result;
//                adapter.setMger(result);
//                return adapter;
            }
        }
        return null;
    }

    /**
     * create a new Authentication Manager from a specified provider with a
     * specified name
     *
     * @param provider
     *            the chosen provider
     * @param instance
     *            the type of Authentication Manager
     * @return the new Authentication Manager
     */
    public AuthenticationManager newAuthManager(String provider, String instance) {
        return authProviders.get(provider).newInstance(instance);
    }

    /**
     * get an Authentication Manager specified in the configuration
     *
     * @return the authentication manager
     */
    public AuthenticationManager getAuthManager() {
        return getAuthManager(defaultAuthProvider);
    }

    /**
     * get an authentication manager of the specified type
     *
     * @param instance
     *            the type of authentication manager
     * @return the authentication manager
     */
    public AuthenticationManager getAuthManager(String instance) {
        AuthenticationManager am = authProviders.get(defaultAuthProvider).getInstance(instance);
        if (am != null) {
            return am;
//            adapter.setMger(am);
//            return adapter;
        }
        for (Entry<String, AuthProvider> i : authProviders.entrySet()) {
            AuthenticationManager result = i.getValue().getInstance(instance);
            if (result != null) {
                return result;
//                adapter.setMger(result);
//                return adapter;
            }
        }
        return null;
    }

    /**
     * get an authentication manager from a provider, of a specific type
     *
     * @param provider
     *            the provider
     * @param instance
     *            the type of authentication manager
     * @return the authentication manager
     */
    public AuthenticationManager getAuthManager(String provider, String instance) {
        return authProviders.get(provider).getInstance(instance);
    }

    /**
     * clean up
     */
    public void dispose() {
        for (Entry<String, AuthProvider> i : authProviders.entrySet()) {
            i.getValue().dispose();
            System.err.println("disposed " + i.getKey());
        }
        authProviders.clear();
        System.err.println("disposed auth providers");
    }

//    /**
//     * register an observer to be notified when setPassword is called
//     *
//     * @param o
//     */
//    public void registerObserver(Observer<String> o) {
//        adapter.addObserver(o);
//    }
//
//    /**
//     * send password to all observers
//     */
//    public void propagatePassword() {
//        adapter.pushPassword();
//    }

    /**
     * refresh all providers
     */
    public void refreshAll() {
        for (Entry<String, AuthProvider> i : authProviders.entrySet()) {
            i.getValue().refresh();
        }
//        adapter = new AuthManagerAdapter();
        return;
    }
}
