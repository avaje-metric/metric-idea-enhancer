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

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ActionRunner;
import org.avaje.metric.agent.Transformer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.IllegalClassFormatException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This task actually hand all successfully compiled classes over to the Ebean weaver.
 *
 * @author Mario Ivankovits, mario@ops.co.at
 * @author yevgenyk - Updated 28/04/2014 for IDEA 13
 */
public class MetricsEnhancementTask {

  private static final int DEBUG = 1;

  private final CompileContext compileContext;

  private final Map<String, File> compiledClasses;

  public MetricsEnhancementTask(CompileContext compileContext, Map<String, File> compiledClasses) {
    this.compileContext = compileContext;
    this.compiledClasses = compiledClasses;
  }

  public void process() {
    try {
      ActionRunner.runInsideWriteAction(
        new ActionRunner.InterruptibleRunnable() {
          @Override
          public void run() throws Exception {
            doProcess();
          }
        }
      );
    } catch (Exception e) {
      e.printStackTrace();
      String msg = Arrays.toString(e.getStackTrace());
      compileContext.addMessage(CompilerMessageCategory.ERROR, e.getClass().getName() + ":" + e.getMessage() + msg, null, -1, -1);
    }
  }


  private void doProcess() throws IOException, IllegalClassFormatException {

    compileContext.addMessage(CompilerMessageCategory.INFORMATION, "Avaje metrics enhancement started v4 ...", null, -1, -1);


    IdeaClassBytesReader classBytesReader = new IdeaClassBytesReader(compileContext, compiledClasses);

    IdeaClassLoader cl = new IdeaClassLoader(Thread.currentThread().getContextClassLoader(), classBytesReader);

    final Transformer transformer = new Transformer("debug=" + DEBUG, cl, classBytesReader);

    transformer.setLogout(new PrintStream(new ByteArrayOutputStream()) {
      @Override
      public void print(String message) {
        compileContext.addMessage(CompilerMessageCategory.INFORMATION, message, null, -1, -1);
      }

      @Override
      public void println(String message) {
        compileContext.addMessage(CompilerMessageCategory.INFORMATION, message, null, -1, -1);
      }
    });

    ProgressIndicator progressIndicator = compileContext.getProgressIndicator();
    progressIndicator.setIndeterminate(true);
    progressIndicator.setText("Avaje metrics enhancement");

    InputStreamTransform isTransform = new InputStreamTransform(transformer, cl);//this.getClass().getClassLoader());

    for (Entry<String, File> entry : compiledClasses.entrySet()) {
      String className = entry.getKey();
      File file = entry.getValue();

      progressIndicator.setText2(className);

      byte[] transformed = isTransform.transform(className, file);
      if (transformed != null) {
        VirtualFile outputFile = VfsUtil.findFileByIoFile(file, true);
        if (outputFile == null) {
          compileContext.addMessage(CompilerMessageCategory.ERROR, "Avaje metrics - outputFile not found writing " + className, null, -1, -1);
        } else {
          outputFile.setBinaryContent(transformed);
        }
      }
    }

    compileContext.addMessage(CompilerMessageCategory.INFORMATION, "Avaje metrics enhancement done!", null, -1, -1);
  }
}
