package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONObject;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    @FunctionName("EventGridUserEventReceiver")
    public void run(
            @EventGridTrigger(name = "event") String event,
            final ExecutionContext context) {
        
        // Log para ver el evento recibido
        context.getLogger().info("Evento recibido: " + event);
        try {
            // Parsear el evento JSON
            JSONObject eventJson = new JSONObject(event);
            String eventType = eventJson.getString("eventType");
            JSONObject eventData = eventJson.getJSONObject("data");
            
            context.getLogger().info("Tipo de Evento: " + eventType);
            context.getLogger().info("Datos del Evento: " + eventData.toString());
            
            // Obtener el ID desde el objeto data
            String userId = eventData.getString("id");
            context.getLogger().info("ID del Usuario: " + userId);
            
            switch (eventType) {
                case "UserDeleted":
                    context.getLogger().info("Eliminando usuario...");
                    try (Connection conn = OracleDBConnection.getConnection()) {
                        String sql = "DELETE FROM USUARIO WHERE ID_USUARIO = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            // Convertir el ID de string a entero para la consulta SQL
                            stmt.setInt(1, Integer.parseInt(userId));
                            int rowsAffected = stmt.executeUpdate();
                            context.getLogger().info("Filas afectadas: " + rowsAffected);
                        }
                    } catch (SQLException e) {
                        context.getLogger().severe("Error al eliminar usuario: " + e.getMessage());
                    }
                    break;
                default:
                    context.getLogger().warning("Tipo de evento no reconocido: " + eventType);
                    break;
            }
        } catch (Exception e) {
            context.getLogger().severe("Error al procesar el evento: " + e.getMessage());
            e.printStackTrace(); // Esto ayudará a ver el error completo en los logs
        }
    }
}
