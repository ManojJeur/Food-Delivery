# Service Registry

Eureka service registry for the Food Delivery backend. It maintains service
instance registrations for later gateway and service-client discovery; it does
not store business data.

## Requirements

- JDK 17 or newer
- Maven 3.6.3 or newer

## Run

From this directory:

```powershell
mvn spring-boot:run
```

Open the Eureka dashboard at <http://localhost:8761>. No services will appear
until applications are configured as Eureka clients.

The port defaults to `8761` and can be overridden with the `SERVER_PORT`
environment variable.
