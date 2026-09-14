rem 실행 중인 컨테이너 중지

cd docker-compose/mysql
docker compose stop
cd ../../

cd docker-compose/redis
docker compose stop
cd ../../
