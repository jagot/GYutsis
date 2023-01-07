# Inspired by https://stackoverflow.com/a/4020275

SOURCES := $(wildcard src/*.java)
CLASSES := $(patsubst src/%.java,bin/classes/%.class,$(SOURCES))

TARGET = ./bin/GYutsis.jar

all: $(TARGET)

$(TARGET): $(CLASSES)
	cd bin/classes && jar -c -v -f ../GYutsis.jar -e GYutsis *.class

bin/classes/%.class: src/%.java bin/.dirstamp
	javac -sourcepath ./src -d ./bin/classes $<

.PHONY: clean
clean:
	rm -rf bin

bin/.dirstamp:
	mkdir -p bin/classes
	touch $@
