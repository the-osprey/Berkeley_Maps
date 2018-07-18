# Berkeley_Maps

Project created for my datastructures and algorithms class. It includes a Google Maps-like application that lets you navigate Berkeley. For example, you can click and find how to navigate the Cal campus, route yourself from Soda Hall to Cheesboard (major key), and also search local businesses. The data for this comes from OpenStreetMap, so some data points may be missing, and roads incorrect. For example, you will notice one way roads are not included because some sections are wrong on the site. This led to some bad pathmaking, so it was not included (so don't use this to drive on Durant!). 

Specifically, I implemented the backend for this project. @Source majorly to J Hug, A Yao, and others who implemented the frontend javascript file that actually runs the application, and also gave impetus to complete this!

### Some interesting files to check out:

#### Handling roads using graph theory/datastructure. 
Navigate to src/main/java and look at GraphDB.java (main file), GraphBuildingHandler.java and GraphDBLauncher.java. These files convert intersection (vertex) and road (edge) data from XML files into a graph! If you check the implementation, you can see how verteces, adjacent intersections, and other datasets are handled with datastructures such as stacks and hashaps. This allows for the implementation of an A* algorithm for pathfinding in router.java. Thus, we can find our way very quickly. The program ran quite fast too, about .5x faster than n the average. The graph also handles connections with buildings so we can go place to place.

#### Rastering image files onto longitutde and latitude data, zoom data
Since we clearly can't just use one giant image to display on the map application, we have to stitch a set of images together, depending on the user's current zoom level. This is implemented in src/main/java/Router.java using some geometry (including circular distance because the earth is not flat) and implementing some important methods to calculate the correct images to use. 

#### Tests
An app of this scale obviously needs tests. And lots of them. Find them in src/test/java. 

#### Running the files
My implementation uses a set of over a thousand images, making it infeasible to upload to github. Additionally, I developed this through Intellij while using maven to support it. Thus, it's not possible to run the files from github themselves. *however*, to get around this, simply go here: http://grigomaps.herokuapp.com/map.html. Hosted on Heroku, it runs the program for you without worrying about downloading the images, maven, etc. One drawback is it runs quite a bit slower--particularly the rasterer--due to the webhosting server being free. 
