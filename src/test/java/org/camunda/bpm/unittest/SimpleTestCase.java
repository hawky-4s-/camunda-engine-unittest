/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.unittest;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.junit.Assert.assertTrue;

/**
 * @author Daniel Meyer
 * @author Martin Schimak
 */
public class SimpleTestCase {

  private static final Logger LOGGER = Logger.getLogger(SimpleTestCase.class.getName());

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Test
  @Deployment(resources = {"testProcess.bpmn"})
  public void shouldExecuteProcess() {
    // Given we create a new process instance
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("testProcess");
    // Then it should be active
    assertThat(processInstance).isActive();
    // And it should be the only instance
    assertThat(processInstanceQuery().count()).isEqualTo(1);
    // And there should exist just a single task within that process instance
    assertThat(task(processInstance)).isNotNull();

    // When we complete that task
    complete(task(processInstance));
    // Then the process instance should be ended
    assertThat(processInstance).isEnded();

    // get user ops
    List<UserOperationLogEntry> list = rule.getHistoryService().createUserOperationLogQuery().list();

    for (UserOperationLogEntry userOperationLogEntry : list) {
      System.out.println(userOperationLogEntry.toString());
    }
    assertThat(list).hasSize(2);

    boolean taskCreateFound = false;
    for (UserOperationLogEntry userOperationLogEntry : list) {
      if (userOperationLogEntry.getEntityType().equalsIgnoreCase(EntityTypes.TASK)) {
        assertThat(userOperationLogEntry.getOperationType()).isEqualToIgnoringCase(UserOperationLogEntry.OPERATION_TYPE_CREATE);
        taskCreateFound = true;
      }
    }

    assertTrue("No create task user operation log entry found!", taskCreateFound);
  }

}
