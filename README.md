# Simple Secure HTTPS server in Java

This project was a simple HTTPS server in Java made for a programming course in university.

## Features
-Java HTTPS server backend

-User authentication that only allows authorized registered users to interact with the API

-Currently has 4 different endpoints for location information, registration, top 5 visited locations and paths created by users.

-Adds weather data for the requested location that is retrieved from another server when user sends a query, and displays it to the user

-All of the data including userdata and data posted by users is saved in a SQL database.

-SSL/TLS...

## Future improvements
- Currently, only the user's password is saved in encrypted format into the database. However, it can be easily modified to also encrypt the username for extra security.
- The SQL queries are vulnerable to SQL injection at the moment. This can and should be fixed by modifying the code to use only prepared statements.


Additional features:
Feature 5: Attach weather when sightseeing information is requested,

Feature 6: User can create sightseeing paths with custom tour message,

Feature 7: Sightseeing information can be updated,

Feature 8: Server tracks how many times sightseeing location is "visited"
