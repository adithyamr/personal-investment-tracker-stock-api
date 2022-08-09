# personal-investment-tracker-stock-api

docker pull mongo
docker run -p 27017:27017 -d --name mongodb -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=Admin@123 mongo
docker exec -it mongodb bash
mongo -u admin -p Admin@123
use admin
db.createUser({user: "stock", pwd: "Stock@123",roles: [{ role: 'readWrite', db:'stock'}]})
use stock