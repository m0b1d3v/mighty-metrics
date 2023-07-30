all:
	cat --number Makefile

build:
	./gradlew assembleDist

clean:
	./gradlew clean

run:
	./gradlew run

tasks:
	./gradlew tasks

test:
	./gradlew test

