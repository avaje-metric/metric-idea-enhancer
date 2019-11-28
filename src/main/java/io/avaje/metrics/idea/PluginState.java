package io.avaje.metrics.idea;

public class PluginState {

  /**
   * Flag set true when plugin enabled on the project.
   */
  public boolean enabled;

  /**
   * The absolute path to the metrics-agent-x.jar file.
   */
  public String agentPath;
}
