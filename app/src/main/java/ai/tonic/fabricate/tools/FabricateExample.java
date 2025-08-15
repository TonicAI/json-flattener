package ai.tonic.fabricate.tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Example class demonstrating how to use FabricateClient to download data
 * and then flatten it using JsonFlattener. Configuration is loaded from
 * environment variables or .env file.
 */
public class FabricateExample {
    
    public static void main(String[] args) {
        FabricateClient client = null;
        try {
            // Load configuration from environment variables
            EnvConfig config = new EnvConfig();
            config.validate();
            config.printConfig();
            
            // Allow entity override from command line
            String entity = args.length > 0 ? args[0] : config.getEntity();
            if (entity == null || entity.trim().isEmpty()) {
                System.err.println("Error: ENTITY must be provided either as environment variable or command line argument");
                System.err.println("Usage: java FabricateExample [entity-name]");
                System.err.println("Or set ENTITY in environment variables/.env file");
                System.exit(1);
            }
            
            String outputPath = String.format("data/%s.jsonl", entity);
            
            // Download data from Fabricate API
            client = new FabricateClient(config.getFabricateApiKey(), config.getFabricateApiUrl());
            String downloadedFile = client.downloadJsonlData(
                config.getWorkspace(), 
                config.getDatabase(), 
                entity, 
                outputPath
            );
            
            // Flatten the downloaded data
            JsonFlattener flattener = new JsonFlattener();
            List<Map<String, Object>> flattenedData = flattener.processJsonlFile(downloadedFile);
            
            // Print the flattened result
            System.out.println("\nFlattened JSONL data:");
            System.out.println(flattener.toPrettyJson(flattenedData));
            
        } catch (IllegalStateException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
