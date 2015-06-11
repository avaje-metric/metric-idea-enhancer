/*
 * Copyright 2009 Mario Ivankovits
 *
 *     This file is part of Ebean-idea-plugin.
 *
 *     Ebean-idea-plugin is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Ebean-idea-plugin is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Ebean-idea-plugin.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.avaje.idea.metric.plugin;

import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Maintains the per project activate flag and setup the compiler stuff appropriate
 */
@State(name = "avajeMetricsEnhancement", storages = {
  @Storage(id = "avajeMetricsEnhancement", file = StoragePathMacros.WORKSPACE_FILE)
})
public class MetricsActionComponent implements ProjectComponent, PersistentStateComponent<MetricsActionComponent.MetricsEnhancementState> {

  private final Project project;

  private final CompiledFileCollector compiledFileCollector;

  private final MetricsEnhancementState metricsEnhancementState;

  public MetricsActionComponent(Project project) {
    this.project = project;
    this.compiledFileCollector = new CompiledFileCollector();
    this.metricsEnhancementState = new MetricsEnhancementState();
  }

  @Override
  @NotNull
  public String getComponentName() {
    return "Avaje Metrics Action Component";
  }

  @Override
  public void initComponent() {
  }

  @Override
  public void disposeComponent() {
  }

  @Override
  public void projectOpened() {
  }

  @Override
  public void projectClosed() {
    setEnabled(false);
  }

  public boolean isEnabled() {
    return metricsEnhancementState.enabled;
  }

  public void setEnabled(boolean enabled) {
    if (!this.metricsEnhancementState.enabled && enabled) {
      getCompilerManager().addCompilationStatusListener(compiledFileCollector);
    } else if (this.metricsEnhancementState.enabled && !enabled) {
      getCompilerManager().removeCompilationStatusListener(compiledFileCollector);
    }
    this.metricsEnhancementState.enabled = enabled;
  }

  private CompilerManager getCompilerManager() {
    return CompilerManager.getInstance(project);
  }

  @Nullable
  @Override
  public MetricsEnhancementState getState() {
    return metricsEnhancementState;
  }

  @Override
  public void loadState(MetricsEnhancementState ebeanEnhancementState) {
    setEnabled(ebeanEnhancementState.enabled);
    XmlSerializerUtil.copyBean(ebeanEnhancementState, this.metricsEnhancementState);
  }

  public static class MetricsEnhancementState {
    public boolean enabled;
  }
}
