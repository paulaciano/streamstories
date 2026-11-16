# ADR-0010: La reactivación de cuentas requiere aprobación de un admin

- **Estado:** Aceptado
- **Fecha:** 2026-11-16

## Contexto

Una cuenta puede quedar inactiva porque la desactiva su titular o porque lo hace un administrador. En ambos casos, volver a habilitarla es una decisión que debe tomar la plataforma, no el usuario por su cuenta.

## Decisión

- Solo un `ADMIN` puede reactivar una cuenta.
- El usuario inactivo puede **solicitar** la reactivación enviando sus credenciales (`POST /auth/reactivation-requests`). Se crea una `ReactivationRequest` en estado `PENDING`, y como máximo puede haber una pendiente por usuario.
- Un admin aprueba o rechaza la solicitud. La aprobación reactiva la cuenta. Cada resolución registra quién la tomó y cuándo.
- Un admin también puede reactivar una cuenta directamente desde la gestión de usuarios.

## Alternativas consideradas

- **Reactivación automática al iniciar sesión:** el usuario podría deshacer una desactivación hecha por un admin.
- **Un estado `PENDING_REACTIVATION` en el usuario:** mezcla dos conceptos y pierde el historial de solicitudes y resoluciones.

## Consecuencias

- ✅ Control total de la plataforma sobre quién vuelve a acceder.
- ✅ Trazabilidad de cada solicitud y de cada decisión.
- ⚠️ Como no hay notificaciones, el usuario se entera de la aprobación cuando vuelve a intentar iniciar sesión.
