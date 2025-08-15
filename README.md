# Ostia - JSON Flattening Tool

A Java application that converts nested JSON structures into a flat representation with detailed structure markers.

## Two Modes of Operation

### Mode 1: Transform Local Files

Process existing JSONL (JSON Lines) files on your filesystem.

### Mode 2: Generate & Transform with Fabricate

Connect to Fabricate API to generate synthetic data, then automatically flatten it.

## Features

- **Dual Operation Modes**: Transform local files OR generate data via Fabricate API
- **JSONL Processing**: Reads JSONL files line by line
- **Intelligent Flattening**: Converts nested JSON objects and arrays to flat structures
- **Structure Markers**: Adds detailed markers to indicate object and array boundaries
- **Dot Notation**: Supports nested objects (e.g., `location.city`)
- **Array Indexing**: Handles arrays with indexing (e.g., `nicknames[0]`, `nicknames[1]`)
- **Unique IDs**: Generates unique identifiers for each flattened record
- **Environment Configuration**: Easy setup with `.env` files
- **Local Development**: Support for local Fabricate instances

## Output Format

The tool converts JSON data into a structured format with the following markers:

- **Objects**: Start with `"value": "Structure"` and end with `"value": "EndStructure"`
- **Arrays**: Start with the array length as value and end with `"value": "EndArray"`
- **Primitives**: Direct key-value pairs

### Example

Input JSONL:

```json
{
  "name": "John",
  "location": { "city": "New York" },
  "nicknames": ["Jon-boy", "Johnny"]
}
```

Output:

```json
[
  {
    "fields": [
      { "key": "name", "value": "John" },
      { "key": "location", "value": "Structure" },
      { "key": "location.city", "value": "New York" },
      { "key": "location.", "value": "EndStructure" },
      { "key": "nicknames", "value": "2" },
      { "key": "nicknames[0]", "value": "Jon-boy" },
      { "key": "nicknames[1]", "value": "Johnny" },
      { "key": "nicknames.", "value": "EndArray" }
    ]
  }
]
```

## Getting Started

### Prerequisites

- Java 17 or later
- Gradle (or use the included Gradle wrapper)

### Build the Project

```bash
./gradlew build
```

## Quick Start

**Transform a local file:**

```bash
./gradlew run --args="path/to/file.jsonl"
```

**Generate data with Fabricate:**

```bash
# Set up .env file first (see Configuration section)
./gradlew runFabricate -Pargs="users"
```

## Usage

### Mode 1: Transform Local Files

Process existing JSONL files on your filesystem:

```bash
./gradlew run --args="path/to/your/file.jsonl"
```

**Example:**

```bash
./gradlew run --args="/Users/john/data/customers.jsonl"
```

This mode requires:

- ✅ A valid JSONL file path
- ❌ No Fabricate API configuration needed

### Mode 2: Generate & Transform with Fabricate

Generate synthetic data via Fabricate API and automatically flatten it:

```bash
# Uses entity from .env file
./gradlew runFabricate

# Override entity via command line
./gradlew runFabricate -Pargs="customers"
```

This mode requires:

- ✅ Fabricate API configuration (see Configuration section below)
- ✅ Valid API key and workspace access
- ❌ No local files needed

## Configuration (Mode 2 Only)

⚠️ **Note**: Configuration is only required for **Mode 2** (Fabricate API integration). **Mode 1** (local file processing) doesn't need any configuration.

The application supports loading configuration from environment variables or a `.env` file for Fabricate API integration.

### Environment Variables

Create a `.env` file in the project root or set these environment variables:

```bash
# Required
FABRICATE_API_KEY=sk-your-api-key-here
WORKSPACE=your-workspace-name
DATABASE=your-database-name

# Optional
FABRICATE_URI_BASE=http://localhost:3000  # defaults to https://fabricate.mockaroo.com
ENTITY=users                              # can be provided via command line
```

### Setup Instructions

1. **Copy the example configuration:**

   ```bash
   cp env.example .env
   ```

2. **Edit `.env` with your actual values:**

   ```bash
   FABRICATE_API_KEY=sk-abc123xyz...
   WORKSPACE=my-workspace
   DATABASE=my-database
   ENTITY=users
   ```

3. **Run the Fabricate integration:**

   ```bash
   # Uses ENTITY from .env
   ./gradlew runFabricate

   # Override entity via command line
   ./gradlew runFabricate -Pargs="orders"
   ```

### Local Development (Mode 2)

For testing **Mode 2** against a local Fabricate instance:

1. **Set the URI base in your `.env` file:**

   ```bash
   FABRICATE_URI_BASE=http://localhost:3000
   ```

2. **The application automatically appends `/api/v1`** to create the full API URL
3. **If not set, defaults to production:** `https://fabricate.mockaroo.com`

Example local configuration:

```bash
FABRICATE_URI_BASE=http://localhost:3000
FABRICATE_API_KEY=your-local-api-key
WORKSPACE=local-workspace
DATABASE=test-database
ENTITY=users
```

### Using Gradle (if installed globally)

1. **Build the project:**

   ```bash
   gradle build
   ```

2. **Run the application:**
   ```bash
   gradle run --args="path/to/your/file.jsonl"
   ```

## Project Structure

```
<root>/
├── app/
│   ├── src/main/java/ai/tonic/fabricate/tools/
│   │   └── App.java              # Main application logic
│   └── build.gradle              # App dependencies and configuration
├── data/
│   └── example.jsonl             # Sample input file
├── gradle/
│   └── wrapper/                  # Gradle wrapper files
├── gradlew                       # Gradle wrapper script (Unix)
├── gradlew.bat                   # Gradle wrapper script (Windows)
├── settings.gradle               # Project settings
└── README.md                     # This file
```

## Dependencies

- **Jackson Databind**: For JSON parsing and processing
- **OkHttp**: HTTP client for Fabricate API calls
- **Dotenv Java**: Environment variable loading from .env files
- **JUnit**: For testing (test framework)
- **Guava**: Utility libraries

## Input File Format

The application expects JSONL (JSON Lines) format where each line contains a valid JSON object:

```jsonl
{"field1": "value1", "nested": {"field2": "value2"}}
{"field1": "value3", "array": ["item1", "item2"]}
```

## Development

### Core Components

- **`App.java`**: Mode 1 entry point (local file processing)
- **`FabricateExample.java`**: Mode 2 entry point (Fabricate API integration)
- **`JsonFlattener.java`**: Core flattening logic (used by both modes)
- **`FabricateClient.java`**: Fabricate API communication (Mode 2 only)
- **`EnvConfig.java`**: Environment configuration (Mode 2 only)

### Key Methods (JsonFlattener)

- `processJsonlFile()`: Reads and processes JSONL files
- `flattenJsonNode()`: Converts JSON nodes to flattened structure
- `flattenNode()`: Recursively processes nested objects and arrays

## Error Handling

The application will display error messages for:

- File not found
- Invalid JSON syntax
- IO errors during file reading
