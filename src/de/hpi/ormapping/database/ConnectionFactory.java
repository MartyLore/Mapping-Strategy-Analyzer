package de.hpi.ormapping.database;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.sql.DataSource;

public class ConnectionFactory {

	public ConnectionFactory() {

	}

	public static List<String> getRegisteredDatabases() throws Exception {

		List<String> dbs = new ArrayList<>();

		Context initContext = new InitialContext();
		Context envContext = (Context) initContext.lookup("java:/comp/env");
		NamingEnumeration<NameClassPair> list = envContext.list("jdbc");
		while (list.hasMore()) {
			dbs.add(list.next().getName());
		}

		return dbs;
	}

	public static Connection getConnection(String database) throws Exception {
		Connection cn = null;

		Context initContext = new InitialContext();
		Context envContext = (Context) initContext.lookup("java:/comp/env");
		DataSource ds = (DataSource) envContext.lookup("jdbc/" + database);
		cn = ds.getConnection();

		return cn;
	}
}
