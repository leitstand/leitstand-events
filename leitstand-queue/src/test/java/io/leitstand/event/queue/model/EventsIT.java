/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
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
