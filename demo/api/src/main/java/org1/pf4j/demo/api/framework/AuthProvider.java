package org1.pf4j.demo.api.framework;

import org.pf4j.ExtensionPoint;

/**
 * 所有的认证插件服务提供者扩展点接口
 */

public interface AuthProvider extends Provider<AuthenticationManager>, ExtensionPoint {

}
