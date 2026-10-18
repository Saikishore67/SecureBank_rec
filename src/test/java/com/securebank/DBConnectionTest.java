
package com.securebank;

import com.securebank.util.DBConnection;
import java.sql.Connection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DBConnectionTest {

    @Test
    void shouldConnectToDatabase() throws Exception {
        try (Connection connection = DBConnection.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        }
    }
}
