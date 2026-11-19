# ADR-0005: Autenticación stateless con JWT y refresh tokens rotativos

- **Estado:** Aceptado
- **Fecha:** 2026-11-16

## Contexto

La API la consumen clientes HTTP de distintos tipos y no hay sesión de navegador. Hace falta autenticación sin estado en el servidor, con roles `USER` y `ADMIN`, y una cuenta desactivada tiene que perder el acceso de inmediato.

## Decisión

- **Access token JWT** firmado con HS256, de 15 minutos de vida, validado con [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).
- **Refresh token** opaco, de 7 días de vida. En la base solo se guarda su hash. **Se rota** en cada uso y, si se detecta su reutilización, se revocan todos los tokens del usuario.
- Un filtro verifica en cada request que el usuario siga `ACTIVE`.
- Un rol por usuario. Cada cuenta corresponde a un espectador, sin perfiles múltiples.

## Alternativas consideradas

- **Sesión con cookie:** requiere protección CSRF y estado en el servidor, y es menos natural para una API.
- **Un único access token de larga duración:** no se puede revocar y es un riesgo si se filtra.
- **Proveedor de identidad externo** ([Keycloak](https://www.keycloak.org/), Auth0): agrega infraestructura que hoy no hace falta. Como el resource server es estándar, migrar más adelante solo implicaría cambiar el emisor de los tokens.
- **RS256 (clave asimétrica):** aporta valor cuando terceros validan los tokens. Con un único emisor y verificador alcanza con HS256.

## Consecuencias

- ✅ Es un estándar de la industria, fácil de testear y sin sesión en el servidor.
- ✅ La desactivación tiene efecto inmediato.
- ⚠️ Verificar el estado de la cuenta agrega una consulta liviana por request, que se puede cachear.
- ⚠️ El secreto HS256 debe tener al menos 256 bits y llegar solo por variable de entorno.
