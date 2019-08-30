<p align="center">
  <img width="300" height="150" src="https://github.com/AmineToualbi/Raven/blob/master/app/src/main/res/drawable/ravenlogo.png">
</p>

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

The app uses Notifications to communicate and interface effectively with the user at all times. When the app is tracking the location, a notification is shown, when a Raven is sent, a notification is shown, when a Raven is deleted, a notification is shown. 

The app also has the option to be turned off. This allows to stop the tracking of the location and save battery when the app is not needed. 

# UI

![alt text](http://image.noelshack.com/fichiers/2019/18/1/1556563468-raven1.png)

![alt text](http://image.noelshack.com/fichiers/2019/18/1/1556563468-raven2.png)

![alt text](http://image.noelshack.com/fichiers/2019/18/1/1556563468-raven3.png)

![alt text](http://image.noelshack.com/fichiers/2019/18/1/1556563468-raven4.png)

![alt text](http://image.noelshack.com/fichiers/2019/18/1/1556563468-raven5.png)

![alt text](http://image.noelshack.com/fichiers/2019/18/1/1556563468-raven6.png)


# Current State

Because Google doesn't anymore allow the use of the SEND_SMS permission in apps published to the Google Play Store, I was not able to publish Raven. 
Because of the lack of funding, I am not able to afford a third-party API such as Twilio to handle text messaging functionalities in my app. 
Maybe in the future, I will implement an authentication system using Firebase. Cloud messaging will allow to directly send a push notification to a targeted phone which solves the issue of sending an SMS. It will require more work and some tweaks in the app.  
Another possibility would be to allow user to select carrier of the phone number to send to & use email to send a SMS. Could use: https://github.com/yesidlazaro/GmailBackground
