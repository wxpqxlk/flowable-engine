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
import java.util.List;

import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.interceptor.Session;
import org.flowable.common.engine.impl.interceptor.SessionFactory;
import org.flowable.job.service.JobServiceConfiguration;

public class AsyncHistorySessionFactory implements SessionFactory {

    protected AsyncHistoryListener asyncHistoryListener;
    protected List<String> registeredJobDataTypes = new ArrayList<>();
    protected JobServiceConfiguration jobServiceConfiguration;

    @Override
    public Class<?> getSessionType() {
        return AsyncHistorySession.class;
    }

    @Override
    public Session openSession(CommandContext commandContext) {
        return new AsyncHistorySession(commandContext, asyncHistoryListener, registeredJobDataTypes, jobServiceConfiguration);
    }
    
    public void registerJobDataTypes(List<String> registeredJobDataTypes) {
        this.registeredJobDataTypes.addAll(registeredJobDataTypes);
    }
    
    public AsyncHistoryListener getAsyncHistoryListener() {
        return asyncHistoryListener;
    }

    public void setAsyncHistoryListener(AsyncHistoryListener asyncHistoryListener) {
        this.asyncHistoryListener = asyncHistoryListener;
    }

    public List<String> getRegisteredJobDataTypes() {
        return registeredJobDataTypes;
    }

    public void setRegisteredJobDataTypes(List<String> registeredJobDataTypes) {
        this.registeredJobDataTypes = registeredJobDataTypes;
    }

    public JobServiceConfiguration getJobServiceConfiguration() {
        return jobServiceConfiguration;
    }

    public void setJobServiceConfiguration(JobServiceConfiguration jobServiceConfiguration) {
        this.jobServiceConfiguration = jobServiceConfiguration;
    }
}
