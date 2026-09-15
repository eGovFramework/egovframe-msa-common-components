rem 중지된 컨테이너 다시 시작

cd docker-compose/mysql
docker compose start
cd ../../

cd docker-compose/redis
docker compose start
cd ../../
