# Gateway-service

## Översikt

En mikrotjänst som fungerar som huvudingång mellan frontend-applikationen och webbshoppens backendtjänster. Gatewayn
vidarebefordrar anrop till auth-service, product-service och order-service.

När en användare loggar in tar gatewayn emot en JSON Web Token (JWT) från auth-service och sparar den i en
HttpOnly-cookie. Det gör att frontend kan autentisera användaren utan att läsa eller lagra token direkt. Gatewayn är en
del av ett större projekt för att skapa en webbshop. Länkar till de relaterade programmen och deras GitHub-repositories
finns längre ner.

## Teknisk specifikation

Teknikstack: Java JDK 17, Maven, Spring Boot Web MVC, Spring Boot Security, Java HttpClient och Springdoc OpenAPI.
Databas: Ingen. Gateway-service är tillståndslös och lagrar ingen applikationsdata.
Port (localhost): 9003.
CI/CD-workflow: Java CI med Maven och Docker.

## Miljövariabler

- `SERVER_PORT`
- `AUTH_SERVER_URL`
- `PRODUCT_SERVICE_URL`
- `ORDER_SERVICE_URL`
- `COOKIE_SECURE`
- `COOKIE_SAME_SITE`

## Start

Se till att auth-service, product-service och order-service är startade, eller att deras adresser har konfigurerats med
miljövariablerna ovan.

**I terminalen**

Windows: `mvnw.cmd spring-boot:run`

macOS/Linux: `./mvnw spring-boot:run`

**IntelliJ IDEA**

1. Ladda ner projektet från GitHub och öppna det i IntelliJ.
2. Kontrollera att beroendena i `pom.xml` har lästs in korrekt och synkroniserats med projektet.
3. Konfigurera miljövariablerna om de andra tjänsterna inte körs på sina lokala standardportar.
4. Tryck på startknappen och kontrollera att huvudmetoden i `GatewayServiceApplication` är vald.

## Repositories

Auth-service: https://github.com/katttiger/auth-service

Product-service: https://github.com/Joajak92/product-service

Order-service: https://github.com/klasen98/projekt-order-service

E-mail-service: https://github.com/joel-schou/email-service

Frontend: https://github.com/Joajak92/webshop-frontend
