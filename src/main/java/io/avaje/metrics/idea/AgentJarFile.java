package io.avaje.metrics.idea;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Utility to find the agent jar to use.
 */
class AgentJarFile {

  private static final Logger log = Logger.getInstance(AgentJarFile.class);

  /**
   * Return true if the agentPath exists as a file.
   */
  static boolean exists(String agentPath) {
    return agentPath != null && new File(agentPath).exists();
  }

  /**
   * Find the metric-agent jar file to use.
   */
  static File find(String currentAgentPath) {

    File userDevDir = userHomeDevDirectory();

    if (userDevDir.exists() && userDevDir.isDirectory()) {
      // look here and use this agent if it exists (as a mechanism to override the agent)
      log.debug("Looking for metrics-agent jar in " + userDevDir);
      File agentJar = findInDirectory(userDevDir);
      if (agentJar != null && agentJar.exists()) {
        return agentJar;
      }
    }

    if (exists(currentAgentPath)) {
      return new File(currentAgentPath);
    }

    return findPluginAgent();
  }

  /**
   * Return the ebean-agent jar file in the plugin directory.
   */
  @Nullable
  private static File findPluginAgent() {
    File pluginLib = new File(new File(PathManager.getPluginsPath()), "avaje-metrics-plugin/lib");
    if (!pluginLib.exists()) {
      log.error("Directory does not exist config/plugins/avaje-metrics-plugin/lib ? at absolute: " + pluginLib.getAbsolutePath());
      return null;
    }
    return findInDirectory(pluginLib);
  }

  @NotNull
  private static File userHomeDevDirectory() {
    final String userHome = System.getProperty("user.home");
    return new File(userHome + "/.localdev");
  }

  @Nullable
  private static File findInDirectory(File lib) {

    File[] files = lib.listFiles();
    if (files != null) {
      for (File file : files) {
        String name = file.getName();
        if (name.startsWith("metrics-agent-") && name.endsWith(".jar")) {
          // find the first, there should only ever be one
          return file;
        }
      }
    }
    return null;
  }

}
