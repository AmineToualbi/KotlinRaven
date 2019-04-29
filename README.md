# Raven

Raven is meant to be a non-invasive GPS monitoring app targeted towards parents and children. 

The theme of the app was inspired from Game of Thrones.

In the wake of controversies around technology and ethics, Raven provides a way to safely ensure someone has reached a destination with no data being stored anywhere but locally on the phone. 


# Functionality

The user is able to create Ravens stored locally on the phone. A Raven is comprised of a name, a phone number, an address, and a message. 

When a Raven is saved, it is displayed on the main screen of the app with a total of three possible ravens saved at once. 

The Ravens are stored in a database created using SQLite. The database is accessed using two libraries allowing a more efficient and simpler use, Room to provide a layer of abstraction to SQLite and Dagger for dependency injection. 

The Play Services API is used for tracking the location of the user and for geocoding (address -> coordinates and vice-versa). 

A Foreground Service is used to allow for work in the background fo the app. Mainly, the app tracks the location in the background and sends a message when the user reached the given location. On the side, the Ravens are also deleted and manipulated in the background. 

# UI

![alt text](http://image.noelshack.com/fichiers/2019/18/1/1556563468-raven1.png)

![alt text](https://image.noelshack.com/fichiers/2018/33/7/1534705148-screenshot-1.png)

![alt text](https://image.noelshack.com/fichiers/2018/33/7/1534705148-screenshot-3.png)



