import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.SQLException


fun main() {
    val inventory= Inventory(ID_ARTICULO = 200, NOMBRE="prueba", COMENTARIO="prueba", PRECIO=2.50, ID_TIENDA = 1)
    val c = ConnectionBuilder()
    println("conectando.....")

    // Con esto comprobamos si tenemos conexion a la base de datos. Si han pasado 10 segundos, te retoranra que no se puede conectar a la base de datos
    if (c.connection.isValid(10)) {
        println("Conexión válida")

        c.connection.use {
            val h2DAO = UserDAO(c.connection)

            //h2DAO.dropTable()

            //h2DAO.prepareTable()

            //UpdateInventory hace la suma del 15% a los precios del inventario que sean mayor a 2000 pesos
            println(h2DAO.updateInventory())
            println("---------------------------------------------------------------------------------------")
            //SelectAll mostrara todas las tiendas
            println(h2DAO.selectAllTienda())
            println("---------------------------------------------------------------------------------------")
            //SelectInventory Mostrara el inventario por id de tienda
            println(h2DAO.selectInventory(1))
            println("---------------------------------------------------------------------------------------")
            //SelectAllInventory muestra todos los inventarios
            println(h2DAO.selectAllInventory())
            println("---------------------------------------------------------------------------------------")
        }
    } else
        println("Conexión ERROR")
}


class ConnectionBuilder { //Estos son los datos para la conexion de la base de datos
    lateinit var connection: Connection
    private val jdbcURL = "jdbc:postgresql://localhost:5432/tienda"
    private val jdbcUsername = "usuario"
    private val jdbcPassword = "usuario"


    init {
        try {
            connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword)
            connection.autoCommit=false
        } catch (e: SQLException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

}


class UserDAO(private val c: Connection) {

    companion object { //Este companion object se usara para poner los comandos de la base de datos en variables
        private const val SCHEMA = "default"
        private const val TABLE = "libros"
        private const val TRUNCATE_TABLE_TIENDAS = "TRUNCATE TABLE TIENDAS"
        private const val TRUNCATE_TABLE_INVENTARIOS = "TRUNCATE TABLE INVENTARIOS"
        private const val DROP_TABLE_TIENDAS = "DROP TABLE TIENDAS"
        private const val DROP_TABLE_INVENTARIOS = "DROP TABLE INVENTARIOS"
        private const val CREATE_TABLE_TIENDAS_SQL =
            "CREATE TABLE TIENDAS (ID_TIENDA numeric(10,0) CONSTRAINT PK_ID_TIENDA PRIMARY KEY, NOMBRE_TIENDA VARCHAR(40), DIRECCION_TIENDA VARCHAR(200) );"
        private const val CREATE_TABLE_INVENTARIOS_SQL =
            "CREATE TABLE INVENTARIOS (ID_ARTICULO numeric(10,0) CONSTRAINT PK_ID_ARTICULO PRIMARY KEY, NOMBRE VARCHAR(50) UNIQUE, COMENTARIO VARCHAR(200) NOT NULL, PRECIO numeric(10,2) CHECK(PRECIO>0), ID_TIENDA numeric(10,0) CONSTRAINT FK_ID_TIENDA REFERENCES TIENDAS(ID_TIENDA) );"
        private const val SELECT_ALL_TIENDAS = "select * from TIENDAS"
        private const val SELECT_INVENTARIOS_BY_TIENDA1 = "select ID_ARTICULO, NOMBRE, COMENTARIO, PRECIO, ID_TIENDA from INVENTARIOS where ID_TIENDA = ?"
        private const val SELECT_ALL_INVENTARIOS = "select * from INVENTARIOS"
        private const val UPDATE_INVENTARIOS_SQL = "update INVENTARIOS set PRECIO = PRECIO +(PRECIO * 15/100) where PRECIO > 2000.00;"
    }


    fun prepareTable() { //Funcion que prepara la tabla o si existe la elimina
        val metaData = c.metaData
        val rs = metaData.getTables(null, SCHEMA, TABLE, null)

        if (!rs.next()) createTable() else truncateTable()
    }

    private fun truncateTable() { //Sirve para eliminar los datos de la tabla
        println(TRUNCATE_TABLE_TIENDAS)
        try {
            c.createStatement().use { st ->
                st.execute(TRUNCATE_TABLE_TIENDAS)
            }
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
        println(TRUNCATE_TABLE_INVENTARIOS)
        try {
            c.createStatement().use { st ->
                st.execute(TRUNCATE_TABLE_INVENTARIOS)
            }
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
    }

    fun dropTable() { // Elimina la tabla
        println(DROP_TABLE_TIENDAS)
        try {
            c.createStatement().use { st ->
                st.execute(DROP_TABLE_TIENDAS)
            }
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
        println(DROP_TABLE_INVENTARIOS)
        try {
            c.createStatement().use { st ->
                st.execute(DROP_TABLE_INVENTARIOS)
            }
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
    }

    private fun createTable() { //Crea la tabla
        println(CREATE_TABLE_TIENDAS_SQL)
        try {

            c.createStatement().use { st ->
                st.execute(CREATE_TABLE_TIENDAS_SQL)
            }
            //Commit the change to the database
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
        println(CREATE_TABLE_INVENTARIOS_SQL)
        try {

            c.createStatement().use { st ->
                st.execute(CREATE_TABLE_INVENTARIOS_SQL)
            }
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
    }

    fun selectAllInventory(): List<Inventory> { //Visualiza todos los inventarios

        val inventarios: MutableList<Inventory> = ArrayList()
        try {
            c.prepareStatement(SELECT_ALL_INVENTARIOS).use { st ->
                println(st)
                val rs = st.executeQuery()
                while (rs.next()) {
                    val id = rs.getInt("ID_ARTICULO")
                    val nombre = rs.getString("NOMBRE")
                    val comentario = rs.getString("COMENTARIO")
                    val precio = rs.getDouble("PRECIO")
                    val id_tienda = rs.getInt("ID_TIENDA")
                    inventarios.add(Inventory(id, nombre, comentario,precio, id_tienda))
                }
            }

        } catch (e: SQLException) {
            printSQLException(e)
        }
        return inventarios
    }

    fun selectInventory(id:Int): Inventory?{ //Visualiza los inventarios por id de tienda
        var inventarios: Inventory? = null
        try {
            c.prepareStatement(SELECT_INVENTARIOS_BY_TIENDA1).use { st ->
                st.setInt(1, id)
                println(st)
                val rs = st.executeQuery()

                while (rs.next()) {
                    val id = rs.getInt("ID_ARTICULO")
                    val nombre = rs.getString("NOMBRE")
                    val comentario = rs.getString("COMENTARIO")
                    val precio = rs.getDouble("PRECIO")
                    val id_tienda = rs.getInt("ID_TIENDA")
                    inventarios=Inventory(id, nombre, comentario,precio, id_tienda)
                }
            }

        } catch (e: SQLException) {
            printSQLException(e)
        }
        return inventarios
    }


    fun selectAllTienda(): List<Tienda> { //Visualiza todos las tiendas

        val tiendas: MutableList<Tienda> = ArrayList()
        try {
            c.prepareStatement(SELECT_ALL_TIENDAS).use { st ->
                println(st)
                val rs = st.executeQuery()
                while (rs.next()) {
                    val id = rs.getInt("ID_TIENDA")
                    val nombre = rs.getString("NOMBRE_TIENDA")
                    val direccion = rs.getString("DIRECCION_TIENDA")
                    tiendas.add(Tienda(id, nombre, direccion))
                }
            }

        } catch (e: SQLException) {
            printSQLException(e)
        }
        return tiendas
    }


    fun updateInventory(): Boolean { //Actualiza los inventarios
        var rowUpdated = false

        try {
            c.prepareStatement(UPDATE_INVENTARIOS_SQL).use { st ->
                rowUpdated = st.executeUpdate() > 0
            }
            c.commit()
        } catch (e: SQLException) {
            printSQLException(e)
        }
        return rowUpdated
    }

    private fun printSQLException(ex: SQLException) {
        for (e in ex) {
            if (e is SQLException) {
                e.printStackTrace(System.err)
                System.err.println("SQLState: " + e.sqlState)
                System.err.println("Error Code: " + e.errorCode)
                System.err.println("Message: " + e.message)
                var t = ex.cause
                while (t != null) {
                    println("Cause: $t")
                    t = t.cause
                }
            }
        }
    }


}