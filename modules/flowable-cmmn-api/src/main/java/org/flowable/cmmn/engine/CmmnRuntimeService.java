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
package org.flowable.cmmn.engine;

import java.util.Map;

import org.flowable.cmmn.engine.runtime.CaseInstance;
import org.flowable.cmmn.engine.runtime.CaseInstanceQuery;
import org.flowable.cmmn.engine.runtime.MilestoneInstanceQuery;
import org.flowable.cmmn.engine.runtime.PlanItemInstanceQuery;

/**
 * @author Joram Barrez
 */
public interface CmmnRuntimeService {
    
    CaseInstance startCaseInstanceById(String caseDefinitionId);
    
    CaseInstance startCaseInstanceById(String caseDefinitionId, Map<String, Object> variables);
    
    CaseInstance startCaseInstanceByKey(String caseDefinitionKey);
    
    CaseInstance startCaseInstanceByKey(String caseDefinitionKey, Map<String, Object> variables);
    
    void triggerPlanItemInstance(String planItemInstanceId);
    
    void terminateCaseInstance(String caseInstanceId);
    
    Map<String, Object> getVariables(String caseInstanceId);
    
    Object getVariable(String caseInstanceId, String variableName);
    
    void setVariables(String caseInstanceId, Map<String, Object> variables);
    
    void removeVariable(String caseInstanceId, String variableName);
    
    CaseInstanceQuery createCaseInstanceQuery();
    
    PlanItemInstanceQuery createPlanItemQuery();
    
    MilestoneInstanceQuery createMilestoneInstanceQuery();
    
}