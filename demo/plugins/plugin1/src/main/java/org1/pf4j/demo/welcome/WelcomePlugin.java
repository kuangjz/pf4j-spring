/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org1.pf4j.demo.welcome;

import org.apache.commons.lang.StringUtils;

import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org1.pf4j.demo.api.Greeting;

/**
 * @author Decebal Suiu
 */
public class WelcomePlugin extends Plugin {

    public WelcomePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println(String.format("{%s}\tWelcomePlugin.start()",getClass().getName()));
        // for testing the development mode
        if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
            System.out.println(String.format("{%s}\t",getClass().getName(),StringUtils.upperCase("WelcomePlugin")));
        }
    }

    @Override
    public void stop() {
        System.out.println(String.format("{%s}\tWelcomePlugin.stop()",getClass().getName()));
    }

    @Extension
    public static class WelcomeGreeting implements Greeting {

        @Override
        public String getGreeting() {
            return "Welcome";
        }

    }

}
