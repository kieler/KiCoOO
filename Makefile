
SOURCES = de/cau/cs/kieler/kicooo/model/State \
		de/cau/cs/kieler/kicooo/model/Transition \
		de/cau/cs/kieler/kicooo/model/Region \
		de/cau/cs/kieler/kicooo/Main \
		mjson/Json

.PHONY: all clean

all: build/jar/kicooo.jar

clean:
	rm -rf build

build/jar/kicooo.jar: $(foreach src, $(SOURCES), build/classes/$(src).class) MANIFEST.MF
	mkdir -p build/jar
	jar cfm build/jar/kicooo.jar MANIFEST.MF -C build/classes . -C . sctx_schema.json

build/classes/%.class: src/%.java
	mkdir -p build/classes
	javac -cp src -d build/classes src/$*.java