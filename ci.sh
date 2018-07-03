cur_dir=$(cd $(dirname $0); pwd)
docker_src=/home/data

docker rm ci2
docker run --mount type=bind,source=$cur_dir,target=$docker_src -it --name ci2 android-ci:v2 \
	bash -c "cd ${docker_src} && export PATH=$PATH:/opt/gradle/gradle-4.4/bin && gradle wrapper && ./gradlew assembleRelease"

