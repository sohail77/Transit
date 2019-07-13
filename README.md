**Assignmnet3 Transit App**

**Introduction:**

This app fetches real time data from a GTFS feed. The data fetched in this application is from the Halifax Transit data. This application uses gtfs-realtime-bindings library to fetch the data.
All the data fetched is shown on a Google map by placing markers where each marker represents a bus. The data of these buses change every 15 secs. The user can also filter the buses by selecting them from the list.

**Basic Features**

* Shows all the buses on the map that refreshes every 15 secs, each marker on the map shows the bus number to the user.

* Moves the map camera to point the user's location on button click, the user's location is shown with a blue dot indicator.
* The application stores the state of the map when the app is closed.
* This application allows the user to pan the map freely and also zoom in and out using two finger.


<img src="/images/all_buses.png" alt="drawing" width="200"/>


<img src="/images/current_location.png" alt="drawing" width="200"/>


**Exceptional Featires**
I have implemented 3 exceptional which are listed below.

*  **Filter buses** This feature allows the user to filter the buses by checking the busses from the list. If the user doesn't check any bus from the list, then all the buses will be displayed.
*  **Extra bus info** Using this feature the user can click on the bus in the map and a small pop up will show the user the bus number, congestion level (the congestion level is almost always "unknown" in the incoming data) and stop status (the stop status is almost always "in transit" in the incoming data).
*  **Live user tracking** This feature will follow the user where ever he goes. This feature will move the map camera with respect to the user.

<img src="/images/filter.png" alt="drawing" width="200"/>


<img src="/images/custom_info.png" alt="drawing" width="200"/>


**Polishing**

Apart from the above mentioned I have also taken care some edge cases like, when there is no internet connection, when the user denies location permission, when the location is turned OFF etc.


**References**

[1]Gtfs.org, 2019. [Online]. Available: https://gtfs.org/reference/realtime/v2/#message-vehicleposition. [Accessed: 13- Jul- 2019].

[2]"MobilityData/gtfs-realtime-bindings", GitHub, 2019. [Online]. Available: https://github.com/MobilityData/gtfs-realtime-bindings/tree/master/java. [Accessed: 13- Jul- 2019].

[3]W. requestPermissions?, S. Jha, S. Jha, W. WANG and D. Rawson, "What is the difference between shouldShowRequestPermissionRationale and requestPermissions?", Stack Overflow, 2019. [Online]. Available: https://stackoverflow.com/questions/41310510/what-is-the-difference-between-shouldshowrequestpermissionrationale-and-requestp/41312851. [Accessed: 13- Jul- 2019].

[4]A. Android, I. Bykov, G. Shur and A. Tech, "Adding multiple markers in Google Maps API v2 Android", Stack Overflow, 2019. [Online]. Available: https://stackoverflow.com/questions/30569854/adding-multiple-markers-in-google-maps-api-v2-android. [Accessed: 13- Jul- 2019].

[5]"Google Maps Android Custom Info Window Example", Zoftino.com, 2019. [Online]. Available: https://www.zoftino.com/google-maps-android-custom-info-window-example. [Accessed: 13- Jul- 2019].



 