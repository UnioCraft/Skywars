package me.uniodex.skywars.utils.packages.pool;

public enum PoolDriver {

    MYSQL("com.mysql.jdbc.jdbc2.optional.MysqlDataSource"),
    SQLITE_XERIAL("org.sqlite.SQLiteDataSource"),
    POSTGRESQL("org.postgresql.ds.PGSimpleDataSource"),
    APACHE_DERBY("org.apache.derby.jdbc.ClientDataSource"),
    FIREBIRD_JAYBIRD("org.firebirdsql.pool.FBSimpleDataSource"),
    H2("org.h2.jdbcx.JdbcDataSource"),
    HSQLDB("org.hsqldb.jdbc.JDBCDataSource"),
    IBM_DB2("com.ibm.db2.jcc.DB2SimpleDataSource"),
    IBM_INFORMIX("com.informix.jdbcx.IfxDataSource"),
    MS_SQL("com.microsoft.sqlserver.jdbc.SQLServerDataSource"),
    MARIADB("org.mariadb.jdbc.MySQLDataSource"),
    ORACLE("oracle.jdbc.pool.OracleDataSource"),
    ORIENTDB("com.orientechnologies.orient.jdbc.OrientDataSource"),
    POSTGRESQL_PGJDBC("com.impossibl.postgres.jdbc.PGDataSource"),
    SAP_MAXDB("com.sap.dbtech.jdbc.DriverSapDB"),
    SYBASE_JCONNECT("com.sybase.jdbc4.jdbc.SybDataSource");

    private String className;

    PoolDriver(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
