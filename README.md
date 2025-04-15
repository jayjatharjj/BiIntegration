# Data Integration Platform

A web-based application for bidirectional data ingestion between ClickHouse database and Flat File platform.

## Features

- Bidirectional data transfer between ClickHouse and Flat Files
- Support for CSV, TSV, and JSON file formats
- Schema discovery and column selection
- Progress tracking and status monitoring
- User-friendly interface with modern design
- Secure JWT authentication for ClickHouse
- File preview functionality
- Error handling and reporting

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Node.js 16 or higher
- npm 8 or higher
- ClickHouse server (for testing with example datasets)

## Project Structure

```
.
├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml 
├── frontend/               # Vue.js frontend
│   ├── public/
│   ├── src/
│   ├── package.json
│   └── vue.config.js
└── README.md
```

## Setup Instructions

### 1. Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Configure the application:
   - Edit `src/main/resources/application.properties`:
     ```properties
     # Server Configuration
     server.port=8080
     
     # ClickHouse Configuration
     clickhouse.host=localhost
     clickhouse.port=8123
     clickhouse.database=default
     clickhouse.user=default
     clickhouse.password=
     
     # File Upload Configuration
     file.upload-dir=./uploads
     file.max-size=10MB
     ```

3. Build the application:
   ```bash
   mvn clean package
   ```

### 2. Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Configure the development server:
   - Edit `vue.config.js` if needed:
     ```javascript
     module.exports = {
       devServer: {
         port: 8081,
         proxy: {
           '/api': {
             target: 'http://localhost:8080',
             changeOrigin: true
           }
         }
       }
     }
     ```

## Running the Application

### 1. Start the Backend

1. Run the Spring Boot application:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. Verify the backend is running:
   - Open `http://localhost:8080/api/health` in your browser
   - You should see a success message

### 2. Start the Frontend

1. Start the development server:
   ```bash
   cd frontend
   npm run serve
   ```

2. Access the application:
   - Open `http://localhost:8081` in your browser
   - The application interface should load

## Configuration

### ClickHouse Configuration

1. Basic Configuration:
   - Host: Your ClickHouse server address
   - Port: ClickHouse HTTP port (default: 8123)
   - Database: Target database name
   - User: ClickHouse username
   - JWT Token: Authentication token (if required)

2. Example Datasets:
   - The application supports ClickHouse example datasets
   - Available datasets: `uk_price_paid`, `ontime`
   - To use example datasets, ensure they are loaded in your ClickHouse instance

### File Configuration

1. Supported Formats:
   - CSV (Comma-Separated Values)
   - TSV (Tab-Separated Values)
   - JSON (JavaScript Object Notation)

2. File Settings:
   - Delimiter: Character used to separate fields
   - Quote Character: Character used for text fields
   - Header Row: Whether the file includes column headers

## Usage Guide

### 1. ClickHouse to File Export

1. Select Source:
   - Choose "ClickHouse" as the source type
   - Configure connection parameters
   - Click "Test Connection"

2. Configure Export:
   - Click "Load Tables" to see available tables
   - Select a table from the dropdown
   - Choose columns to export
   - Enter SQL query (optional)
   - Click "Start Ingestion"

3. Monitor Progress:
   - View transfer progress in the dialog
   - Check record count and status
   - Download the exported file when complete

### 2. File to ClickHouse Import

1. Select Source:
   - Choose "Flat File" as the source type
   - Select file format (CSV, TSV, JSON)

2. Configure Import:
   - Upload the source file
   - Set file format parameters
   - Enter target table name
   - Select columns to import
   - Click "Start Ingestion"

3. Monitor Progress:
   - View transfer progress in the dialog
   - Check record count and status
   - Verify data in ClickHouse when complete

## Testing with Example Datasets

### 1. Using uk_price_paid Dataset

1. Connect to ClickHouse:
   ```
   Source Type: ClickHouse
   Connection Type: HTTP
   Host: localhost
   Port: 8123
   Database: default
   User: default
   ```

2. Export Data:
   ```
   Table: uk_price_paid
   Columns: price, date, town, county
   Query: SELECT price, date, town, county FROM uk_price_paid LIMIT 1000
   ```

### 2. Using ontime Dataset

1. Connect to ClickHouse:
   ```
   Source Type: ClickHouse
   Connection Type: HTTP
   Host: localhost
   Port: 8123
   Database: default
   User: default
   ```

2. Export Data:
   ```
   Table: ontime
   Columns: FlightDate, Origin, Dest, DepDelay
   Query: SELECT FlightDate, Origin, Dest, DepDelay FROM ontime LIMIT 1000
   ```

## Troubleshooting

### Common Issues

1. Connection Issues:
   - Verify ClickHouse server is running
   - Check network connectivity
   - Validate credentials
   - Ensure correct port is open

2. File Import Issues:
   - Check file format matches selection
   - Verify delimiter settings
   - Ensure header row is correctly specified
   - Check file size limits

3. Performance Issues:
   - Reduce batch size for large datasets
   - Optimize SQL queries
   - Check network bandwidth
   - Monitor system resources

### Error Messages

1. Connection Errors:
   - "Connection refused": Check server status and port
   - "Authentication failed": Verify credentials
   - "Database not found": Check database name

2. File Errors:
   - "Invalid file format": Check file type and settings
   - "File too large": Reduce file size or adjust limits
   - "Invalid delimiter": Check delimiter settings

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
