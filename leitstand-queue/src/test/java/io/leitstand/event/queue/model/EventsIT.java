/*
 * Copyright 2020 RtBrick Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.leitstand.event.queue.model;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import io.leitstand.testing.it.JpaIT;

public class EventsIT extends JpaIT {

	@Override
	protected Properties getConnectionProperties() {
		try {
			Properties properties = new Properties();
			properties.load(ClassLoader.getSystemResourceAsStream("events-it.properties"));
			return properties;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@Override
	protected void initDatabase(DataSource ds) throws SQLException {
		try(Connection c = ds.getConnection()){
			c.createStatement().execute("CREATE SCHEMA BUS");
			c.createStatement().execute("CREATE SCHEMA LEITSTAND");
		}
	}

	@Override
	protected String getPersistenceUnitName() {
		return "events";
	}
	
}
