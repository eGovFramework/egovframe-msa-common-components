rem 컨테이너 생성 및 백그라운드 시작

cd docker-compose/mysql
docker compose up -d
cd ../../

cd docker-compose/redis
docker compose up -d
cd ../../
