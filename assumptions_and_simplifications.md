1. Considering created doctors to be available for appointments from the moment they are created, 24/7.

2. Considering created rooms to be available for appointments from the moment they are created, 24/7.

3. Use migrations to create the database schema, to avoid this on the application.

4. Avoid using an init data on the HealthcareBookingSystemApplication, instead have REST APIs to manage doctors and rooms.

5. I didn't use any mappers, instead I've used the constructor to convert between objects layers (input -> domain, domain <-> database object and domain -> output).
This was initially a solution that made sense for me, but after some time, I was thinking twice on it. This is something that when working with Go, it's one of the
firsts things I do, but in Java I was not sure how to do it the best. After some time, I discovered the @Mapping annotation from MapStruct, which is a great tool
for this purpose. I will be using it in the future.

6. Use H2 database for development and testing, and PostgreSQL for production.

7. Not using any kind of authentication or authorization, as this was not a requirement and I assume it's not the focus of this project. But for a production environment, 
I would use Spring Security with JWT.

8. The DoctorService.java and RoomService.java does almost the exact same thing, but for two different domains. I could refactor this to avoid code duplication. 

9. Not added Swagger.

10. Could improve observability by adding metrics and traces.

11. I've added some PII to the logs. In a production environment, I would use a something that allows me to mask PII.
