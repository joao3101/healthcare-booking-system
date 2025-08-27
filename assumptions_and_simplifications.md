1. Considering created doctors to be available for appointments from the moment they are created, 24/7.

2. Considering created rooms to be available for appointments from the moment they are created, 24/7.

3. Use migrations to create the database schema, to avoid this on the application.

4. Avoid using an init data on the HealthcareBookingSystemApplication, instead have REST APIs to manage doctors and rooms.

5. I didn't use any mappers, instead I've used the constructor to convert between objects layers (input -> domain, domain <-> database object and domain -> output).
This was initially a solution that made sense for me, but after some time, I was thinking twice on it. This is something that when working with Go, it's one of the
firsts things I do, but in Java I was not sure how to do it the best. After some time, I discovered the @Mapping annotation from MapStruct, which is a great tool
for this purpose. I will be using it in the future.
