# OCR-Spring-Backend

## Running with Apptainer

We have provided an `Apptainer.def` file to easily build and run this backend application using Apptainer.

Since your server may not have `fakeroot` configured, we will compile the app *outside* the Apptainer build step using a temporary container, and then pack the compiled jar into the final image.

### 1. Compile the Application
Run this command to compile the Spring Boot application using a temporary Maven container:
```bash
apptainer exec docker://maven:3.9-eclipse-temurin-17 bash -c "cd ocrspring && mvn clean package -DskipTests"
```

### 2. Build the Apptainer Image
Now build the image (`ocrspring.sif`) from the definition file. This will just copy the `.jar` you built in step 1 into a lightweight Java environment:
```bash
apptainer build ocrspring.sif Apptainer.def
```

### 3. Run the Application
Once the image is built, you can run it directly:
```bash
apptainer run ocrspring.sif
```
By default, the application runs on port `8080`.
