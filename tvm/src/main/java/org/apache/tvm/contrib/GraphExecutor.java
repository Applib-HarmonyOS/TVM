/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tvm.contrib;

import org.apache.tvm.Device;
import org.apache.tvm.Function;
import org.apache.tvm.Module;
import org.apache.tvm.TVMValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GraphExecutor {

  private GraphExecutor() {
    /* Do nothing */
  }

  /**
   * Create a runtime executor module given a graph and module.
   * @param graphJson The graph deployed in json format output by compiler.
   * @param libmod The module of the corresponding function.
   * @param dev The local or remote device to deploy the module.
   * @return Runtime graph module that can be used to execute the graph.
   */
  public static GraphModule create(String graphJson, Module libmod, Device dev) {
    Function fcreate = Function.getFunction("tvm.graph_executor.create");
    if (fcreate == null) {
      throw new TVMRuntimeException("Cannot find global function tvm.graph_executor.create."
          + "Did you compile tvm_runtime with correct version?");
    }
    Module graphModule = fcreate.pushArg(graphJson)
        .pushArg(libmod).pushArg(dev.deviceType).pushArg(dev.deviceId)
        .invoke().asModule();

    return new GraphModule(graphModule, dev);
  }

}
