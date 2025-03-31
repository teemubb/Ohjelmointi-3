## This jar contains the weather server given by the course's teacher. 

### You can spin it off with a following command:

```sh
java -jar .\weatherserver-http.jar 4001 
```

-  where 4001 is the port number. Note that the server listens http, so no keystores are needed.


Send a message with curl to http://localhost:portnumber/weather and you recieve short reply instructing what you need to send to the weather server in order to get weather information.


For example:
```sh
curl.exe http://localhost:4001/weather -k -H "Content-Type:application/xml" -v
```


> NOTE: Weather server only listens POST commands and specific XML messages:
```sh
<coordinates>
    <latitude>28.23333</latitude>
    <longitude>10.23344</longitude>
</coordinates>
```