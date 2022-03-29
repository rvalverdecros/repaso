import java.sql.DriverManager

fun main(){
    val jdbcUrl = "jdbc:postgresql://localhost:5432/tienda"

    val connection = DriverManager
        .getConnection(jdbcUrl, "usuario", "usuario")

    println(connection.isValid(0))
}