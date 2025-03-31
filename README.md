# Simple Secure HTTPS server in Java

This project was a simple HTTPS server in Java made for a programming course in university. During the project I learned to make a Java server as well as some basics of Maven build automation.


## Features

- User authentication that only allows authorized registered users to interact with the API.

- Currently has 4 different endpoints for location information, registration, top 5 visited locations and paths created by users.

- Users can create and post new locations and sightseeing paths with different amounts of information, or view locations or paths that have been posted earlier. This data can also be updated

- Adds weather data for the requested location that is retrieved from another server when user sends a query, and displays it to the user (Feature 5).

- Tracks "visitor" count for the locations, increased when users post a "visit".

- All of the data including userdata and data posted by users is saved in a SQL database, which allows it to remain available across different runs of the program.


## Future improvements
- Currently, only the user's password is saved in encrypted format into the database. However, it can be easily modified to also encrypt the username for extra security.
- Salt could be saved separately for extra security.
- The SQL queries are vulnerable to SQL injection at the moment. This can and should be fixed by modifying the code to use only prepared statements.
- Using ai or other methods to autofill descriptions for the locations.

