package ai.tonic.fabricate.tools;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Configuration class that loads environment variables from .env file or system environment.
 * Follows the same pattern as the JavaScript implementation.
 */
public class EnvConfig {
    private final Dotenv dotenv;
    
    public EnvConfig() {
        // Load .env file if it exists, otherwise use system environment
        // Look for .env file in the current directory (project root)
        this.dotenv = Dotenv.configure()
                .directory("./")  // Look in current directory (project root)
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        
        // Debug: Environment variables are now loading correctly from .env file
    }
    
    /**
     * Gets the Fabricate API URL from FABRICATE_URI_BASE, defaulting to production if not set.
     */
    public String getFabricateApiUrl() {
        String uriBase = getEnvVar("FABRICATE_URI_BASE", "https://fabricate.tonic.ai");
        
        // Remove trailing slash if present and append /api/v1
        String cleanBase = uriBase.replaceAll("/$", "");
        return cleanBase + "/api/v1";
    }
    
    /**
     * Gets the Fabricate API key (required).
     */
    public String getFabricateApiKey() {
        String apiKey = getEnvVar("FABRICATE_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("FABRICATE_API_KEY environment variable is required");
        }
        return apiKey;
    }
    
    /**
     * Gets the workspace name (required).
     */
    public String getWorkspace() {
        String workspace = getEnvVar("WORKSPACE");
        if (workspace == null || workspace.trim().isEmpty()) {
            throw new IllegalStateException("WORKSPACE environment variable is required");
        }
        return workspace;
    }
    
    /**
     * Gets the database name (required).
     */
    public String getDatabase() {
        String database = getEnvVar("DATABASE");
        if (database == null || database.trim().isEmpty()) {
            throw new IllegalStateException("DATABASE environment variable is required");
        }
        return database;
    }
    
    /**
     * Gets the entity/table name (required).
     */
    public String getEntity() {
        String entity = getEnvVar("ENTITY");
        if (entity == null || entity.trim().isEmpty()) {
            throw new IllegalStateException("ENTITY environment variable is required");
        }
        return entity;
    }
    
    /**
     * Gets the format (always "jsonl" for this tool).
     */
    public String getFormat() {
        return "jsonl";
    }
    
    /**
     * Validates that required environment variables are set and configuration is valid.
     */
    public void validate() {
        // Ensure required variables are set
        getFabricateApiKey();
        getWorkspace();
        getDatabase();
        getEntity();
    }
    
    /**
     * Gets an environment variable with no default value.
     */
    private String getEnvVar(String key) {
        return getEnvVar(key, null);
    }
    
    /**
     * Gets an environment variable with a default value.
     */
    private String getEnvVar(String key, String defaultValue) {
        return dotenv.get(key, defaultValue);
    }
    
    /**
     * Prints the current configuration (excluding sensitive information).
     */
    public void printConfig() {
        System.out.println("Configuration:");
        
        String uriBase = getEnvVar("FABRICATE_URI_BASE", "https://fabricate.tonic.ai");
        boolean isCustomInstance = !uriBase.equals("https://fabricate.tonic.ai");
        
        System.out.println("  URI Base: " + uriBase + (isCustomInstance ? " (custom instance)" : " (production)"));
        System.out.println("  API URL: " + getFabricateApiUrl());
        System.out.println("  Workspace: " + getWorkspace());
        System.out.println("  Database: " + getDatabase());
        System.out.println("  Entity: " + getEntity());
        System.out.println("  Format: " + getFormat());
        System.out.println("  API Key: " + (getFabricateApiKey().length() > 10 ? 
            getFabricateApiKey().substring(0, 10) + "..." : "***"));
    }
}
