/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhdan.flink;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;

/**
 * Skeleton for a Flink Streaming Job.
 *
 * <p>For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class StreamingSqlJob {

	public static void main(String[] args) throws Exception {

	    EnvironmentSettings settings = EnvironmentSettings
				.newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();

		// set up the streaming execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		final StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

		tableEnv.registerFunction("timestampModifier", new TimestampModifier());

        //source connector.version=universal
        tableEnv.sqlUpdate("CREATE TABLE start_log_source(" +
                "   mid_id VARCHAR, " +
                "   user_id INT, " +
                "   version_code VARCHAR, " +
                "   version_name VARCHAR, " +
                "   lang VARCHAR, " +
                "   source VARCHAR, " +
                "   os VARCHAR, " +
                "   area VARCHAR, " +
                "   model VARCHAR, " +
                "   brand VARCHAR, " +
                "   sdk_version VARCHAR, " +
                "   gmail VARCHAR, " +
                "   height_width VARCHAR, " +
                "   app_time BIGINT, " +
                "   network VARCHAR, " +
                "   lng FLOAT, " +
                "   lat FLOAT " +
                ") WITH (" +
                "   'connector.type' = 'kafka', " +
                "   'connector.version' = '0.11', " +
                "   'connector.topic' = 'start_log', " +
				"   'connector.properties.2.key' = 'group.id', " +
				"   'connector.properties.2.value' = 'testGroup', " +
                "   'connector.startup-mode' = 'earliest-offset', " +
                "   'connector.properties.0.key' = 'zookeeper.connect', " +
                "   'connector.properties.0.value' = 'localhost:2181', " +
                "   'connector.properties.1.key' = 'bootstrap.servers', " +
                "   'connector.properties.1.value' = 'localhost:9092', " +
                "   'update-mode' = 'append', " +
                "   'format.type' = 'json', " +
                "   'format.derive-schema' = 'true' " +
                ")");

        //sink
        String sinkSql = "CREATE TABLE start_log_sink ( " +
                "    mid_id VARCHAR, " +
                "    user_id INT, " +
                "    event_time_test TIMESTAMP " +
                ") WITH ( " +
                "    'connector.type' = 'jdbc', " +
                "    'connector.url' = 'jdbc:mysql://localhost:3306/flink_test', " +
                "    'connector.table' = 'start_log_to_mysql', " +
                "    'connector.username' = 'root', " +
                "    'connector.password' = 'Aa123456', " +
                "    'connector.write.flush.max-rows' = '1' " +
                ")";

        tableEnv.sqlUpdate(sinkSql);


        String insertSql =
                "insert into start_log_sink " +
                "select mid_id, user_id, timestampModifier(app_time) as app_time " +
                "from start_log_source";

        tableEnv.sqlUpdate(insertSql);



         //Table result = tableEnv.sqlQuery("select mid_id, user_id, timestampModifier(app_time)  from start_log_source");
         //tableEnv.toAppendStream(result, Row.class).print();

        // execute program
		env.execute("Flink Streaming Java Sql API Skeleton");
	}

	public static class TimestampModifier extends ScalarFunction {

		public TimestampModifier() {

		}

		public long eval(long t) {
			return t;
		}

		@Override
		public TypeInformation<?> getResultType(Class<?>[] signature) {
			return Types.SQL_TIMESTAMP;
		}
	}



}


