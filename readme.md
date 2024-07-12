This project creates a rest controller that has an endpoint that returns a paramaterised
number of Person objects as a list.

There are two other rest controllers that either call the first controller using webclient or restTemplate
These can be called as follows:

curl http://localhost:8080/webClient/50    

curl http://localhost:8080/restTemplate/50     

The number is the number of simultaneous requests