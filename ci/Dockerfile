FROM javiersantos/android-ci:27.0.3
LABEL maintainer="Coding Android"

RUN curl -L https://services.gradle.org/distributions/gradle-4.4-bin.zip > /gradle.zip && \
	mkdir /opt/gradle && \
	unzip /gradle.zip -d /opt/gradle && \
	export PATH=$PATH:/opt/gradle/gradle-4.4/bin && \
	rm -rf /gradle.zip
	


