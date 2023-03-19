# AsterioTest
This is a simple test API to demonstrate java-backend development skills. 
This application allows you to create, edit, delete, read text AD banners. 

The central entity of the application is the banner, each banner has a name, banner text, banner price and a list of thematic categories that determine what the banner belongs to.
The category contains the name and parameter identifier for the http request.
Each banner request is logged in the request log, one banner cannot be shown twice to a client with the same user-agent and ip.
The priority property of the banner is the price, the most expensive banners are shown first.

## Installing

To install the application on a working machine, you need a MySQL database, 
the parameters for connecting to the database are described in the file application.propeties in the project folder.
You can deploy the project using your preferred IDE with the .jar file packaging feature, or by using Apache Maven to create a .jar file. \
Note that the application uses **Java 17**


Only /bid address is open, to work with the rest of the API requires authorization.

**WARNING: To simplify testing, the API access restriction via Spring security is disabled in the shipped version, to enable this feature, you must edit the source code in the com.example.asteriotest.config.WebSecurityConfig package**.
**To set the limiting functions, uncomment lines 25,26 and at the same time comment out line 27**

## Usage 
The application is based on REST, 
to use its functions you will need to send http requests with prepared data in JSON format or with parameters http-request.

The main API operations are adding, editing, deleting, reading. Operations for banners and categories are separated.

###Category-request
#### Add category: 
A banner cannot be created without a category. To add a category, send a request to ```banners/addBanner``` along with JSON. 

JSON example:
```
{
    "name": "rock music",
    "requestId":"rockmusic"
}
```

#### Update category:

The category can be changed if required. To change the category you need to send a request to ``` /categories/update ``` with JSON. \
Category properties can be changed, it is important to pay attention to the **ID field**, based on it determines which record in the database will be changed.

JSON example:
```
{
    "id":1,
    "name":"rock music artist",
    "requestId": "rockmusic-artist"
}
```
#### Delete category:
The banner can be removed by querying ```/categories/delete/{id}?cascadeRemove={boolean-value}``` \
**Warning**: The **cascadeRemove** parameter controls whether all associated banners will be deleted along with the category

cascadeRemove=**true** - all related banners will be deleted

cascadeRemove=**false** - the deletion will stop if it turns out that the category is associated with a banner

#### Search category:

Categories can be searched using the request ```/categories/search?name={category}```, request is not case sensitive

###Banner-request
Request for banners are mostly similar to category queries.

#### Add a banner: 

To create a banner, send an http request to ```/banners/addBanner``` with JSON

JSON example:
```
{
    "nameBanner": "Rock music banner",
    "text": "Some text for banner. There may be a lot of text.",
    "price":1440.2,
    "categories": [
        {
        "id":1,
        "id":3   
        }
    ]
}
```

####Update banner
Send a request to ```/banners/update``` along with JSON

JSON example:
```
{
    "banner": {
        "id": 2,
        "nameBanner": "Music banner",
        "text": "Some new text for music banner",
        "price":1131
    },
    "categoriesId": [1,2]
}
```

####Delete banner
Removing banner using the query  ```/banners/delete/{id}```

####Search banner
Search for a banner using the request ```/banners/search?name={name}```. The query parameter is not case-sensitive.


### View banners
Banners can be viewed using the query ```/bid?cat={firstCategory}&cat={secondCategory}```
Warning: Once a banner has been viewed, **it will not be displayed again for a user with the same IP and User-Agent**.\
Warning: If several banners match the request parameters, **the banner with the highest price** will be returned.