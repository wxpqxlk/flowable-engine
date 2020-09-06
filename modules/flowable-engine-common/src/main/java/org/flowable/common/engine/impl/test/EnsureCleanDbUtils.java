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
package org.flowable.common.engine.impl.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandConfig;
import org.flowable.common.engine.impl.interceptor.CommandExecutor;
import org.slf4j.Logger;

/**
 * @author Filip Hrisafov
 */
public class EnsureCleanDbUtils {

    protected static final String DB_CHANGELOG_TABLE = "DATABASECHANGELOG";

    public static void assertAndEnsureCleanDb(
            String testName,
            Logger logger, AbstractEngineConfiguration engineConfiguration,
            EnsureCleanDb ensureCleanDb, boolean hasNoException, Command<Void> dropAndRecreateDbCommand) {
        Collection<String> tableNamesExcludedFromDbCleanCheck = new HashSet<>(Arrays.asList(ensureCleanDb.excludeTables()));
        // if the db should not be dropped pass null
        Command<Void> dropAndRecreateDbCommandToUse = ensureCleanDb.dropDb() ? dropAndRecreateDbCommand : null;
        assertAndEnsureCleanDb(testName, logger, engineConfiguration, tableNamesExcludedFromDbCleanCheck, hasNoException, dropAndRecreateDbCommandToUse);
    }

    public static void assertAndEnsureCleanDb(String testName, Logger logger, AbstractEngineConfiguration engineConfiguration,
            Collection<String> tableNamesExcludedFromDbCleanCheck, boolean hasNoException, Command<Void> dropAndRecreateDbCommand) {
        logger.debug("verifying that db is clean after test");
        Map<String, Long> tableCounts = engineConfiguration.getCommandExecutor()
                .execute(commandContext -> engineConfiguration.getTableDataManager().getTableCount());
        StringBuilder outputMessage = new StringBuilder();
        for (String tableName : tableCounts.keySet()) {
            String tableNameWithoutPrefix = tableName.replace(engineConfiguration.getDatabaseTablePrefix(), "");
            if (!tableNamesExcludedFromDbCleanCheck.contains(tableNameWithoutPrefix) && !tableNameWithoutPrefix.contains(DB_CHANGELOG_TABLE)) {
                Long count = tableCounts.get(tableName);
                if (count != 0L) {
                    outputMessage.append("  ").append(tableName).append(": ").append(count).append(" record(s) ");
                }
            }
        }
        if (outputMessage.length() > 0) {
            outputMessage.insert(0, "DB NOT CLEAN for " + testName + ": \n");
            logger.error("\n");
            logger.error(outputMessage.toString());

            logger.info("dropping and recreating db");

            if (dropAndRecreateDbCommand != null) {
                CommandExecutor commandExecutor = engineConfiguration.getCommandExecutor();
                CommandConfig config = new CommandConfig().transactionNotSupported();
                commandExecutor.execute(config, dropAndRecreateDbCommand);
            }

            if (hasNoException) {
                throw new AssertionError(outputMessage.toString());
            }
        } else {
            logger.info("database was clean");
        }
    }

}
