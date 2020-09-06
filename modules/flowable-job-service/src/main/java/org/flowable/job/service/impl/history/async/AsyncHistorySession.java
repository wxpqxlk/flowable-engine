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
package org.flowable.job.service.impl.history.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.flowable.common.engine.impl.cfg.TransactionContext;
import org.flowable.common.engine.impl.context.Context;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.interceptor.CommandContextCloseListener;
import org.flowable.common.engine.impl.interceptor.Session;
import org.flowable.job.service.JobServiceConfiguration;
import org.flowable.job.service.impl.asyncexecutor.AsyncExecutor;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class AsyncHistorySession implements Session {
    
    public static final String TIMESTAMP = "__timeStamp"; // Two underscores to avoid clashes with other fields
    
    protected CommandContext commandContext;
    protected AsyncHistoryListener asyncHistoryListener;
    protected CommandContextCloseListener commandContextCloseListener;
    protected JobServiceConfiguration jobServiceConfiguration;

    // A list of the different types of history for which jobs will be created
    // Note that the ordering of the types is important, as it will define the order of job creation.
    protected List<String> jobDataTypes;
    
    protected TransactionContext transactionContext;
    protected String tenantId;
    protected Map<JobServiceConfiguration, AsyncHistorySessionData> sessionData;

    public AsyncHistorySession(CommandContext commandContext, AsyncHistoryListener asyncHistoryJobListener, JobServiceConfiguration jobServiceConfiguration) {
        this.commandContext = commandContext;
        this.asyncHistoryListener = asyncHistoryJobListener;
        this.jobServiceConfiguration = jobServiceConfiguration;
        
        // A command context close listener is registered to avoid creating the async history data if it wouldn't be needed 
        initCommandContextCloseListener();
        
        if (isAsyncHistoryExecutorEnabled()) {
            // The transaction context is captured now, as it might be gone by the time 
            // the history job entities are created in the command context close listener
            this.transactionContext = Context.getTransactionContext();
        }
    }
    
    public AsyncHistorySession(CommandContext commandContext, AsyncHistoryListener asyncHistoryJobListener, 
            List<String> jobDataTypes, JobServiceConfiguration jobServiceConfiguration) {
        
        this(commandContext, asyncHistoryJobListener, jobServiceConfiguration);
        this.jobDataTypes = jobDataTypes;
    }
    
    protected boolean isAsyncHistoryExecutorEnabled() {
        AsyncExecutor asyncHistoryExecutor = jobServiceConfiguration.getAsyncHistoryExecutor();
        return asyncHistoryExecutor != null && asyncHistoryExecutor.isActive();
    }

    protected void initCommandContextCloseListener() {
        this.commandContextCloseListener = new AsyncHistorySessionCommandContextCloseListener(this, asyncHistoryListener); 
    }
    
    public void addHistoricData(JobServiceConfiguration jobServiceConfiguration, String type, ObjectNode data) {
        addHistoricData(jobServiceConfiguration, type, data, null);
    }

    public void addHistoricData(JobServiceConfiguration jobServiceConfiguration, String type, ObjectNode data, String tenantId) {
        data.put(TIMESTAMP, AsyncHistoryDateUtil.formatDate(jobServiceConfiguration.getClock().getCurrentTime()));
        
        if (sessionData == null) {
            sessionData = new HashMap<>();
            commandContext.addCloseListener(commandContextCloseListener);
        }
        
        AsyncHistorySessionData asyncHistorySessionData = sessionData.get(jobServiceConfiguration);
        if (asyncHistorySessionData == null) {
            asyncHistorySessionData = new AsyncHistorySessionData();
            sessionData.put(jobServiceConfiguration, asyncHistorySessionData); 
        }
        
        if (tenantId != null) {
            this.tenantId = tenantId;
        }
        
        asyncHistorySessionData.addJobData(type, data);
    }
    
    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Map<JobServiceConfiguration, AsyncHistorySessionData> getSessionData() {
        return sessionData;
    }

    public void setSessionData(Map<JobServiceConfiguration, AsyncHistorySessionData> sessionData) {
        this.sessionData = sessionData;
    }

    public List<String> getJobDataTypes() {
        return jobDataTypes;
    }

    public void setJobDataTypes(List<String> jobDataTypes) {
        this.jobDataTypes = jobDataTypes;
    }

    public TransactionContext getTransactionContext() {
        return transactionContext;
    }

    public void setTransactionContext(TransactionContext transactionContext) {
        this.transactionContext = transactionContext;
    }
    
    /**
     * Wrapper for the async history job data, to avoid messing with maps and lists.
     */
    public static class AsyncHistorySessionData { 
        
        protected Map<String, List<ObjectNode>> jobData = new LinkedHashMap<>(); // A map of {type, list of map-data (the historical event)}. Linked because insertion order is important
        
        public Map<String, List<ObjectNode>> getJobData() {
            return jobData;
        }
        public void setJobData(Map<String, List<ObjectNode>> jobData) {
            this.jobData = jobData;
        }
        public void addJobData(String type, ObjectNode data) {
            if (!jobData.containsKey(type)) {
                jobData.put(type, new ArrayList<>(1));
            }
            jobData.get(type).add(data);
        }
        
    }
}
