GRADLE=./gradlew

BINNAME=com.spruceid.mobilesdkexample

# If you have more than one android device, you can set it by exporting its serial number as ANDROID_DEVICE
# in your shell.  This will attempt to infer which to use, assuming nothing is specified.

ifneq ($(ANDROID_DEVICE),)
ADB_DEVICE_ARG=-s $(ANDROID_DEVICE)
else
# You can think of NR as the line number (sort of) here (it's actually "number of records seen so far").
# We have to do $$1 is the make-escaped version of $1, which is the first column.
# So, this is just taking row 2, column 1 from 'adb devices'; we do row 2 to skip the header line.
ADB_DEVICE_ARG=-s $(shell adb devices | awk 'NR==2{print $$1}')
endif

all: install

.phony: help
help: #@ Makefile help.
	@grep -E '^[a-zA-Z_-]+:.*?#@ .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS=":.*?#@ "};{printf "%-16s %s\n", $$1, $$2}'

.phony: clean
clean: #@ Clean the build.
	@$(GRADLE) clean

# Throws an exception for some reason I haven't looked into yet.
.phony: build
build: #@ Make the build; currently not working, use 'install' instead.
	@$(GRADLE) build

.phony: lint
lint: #@ Lint the build.
	@$(GRADLE) lintDebug

.phony: install
install: #@ Install the build to all devices.
	@$(GRADLE) installDebug

.phony: run
run: stop #@# Run the build, engage the logger.  Must have been installed first.
	@adb $(ADB_DEVICE_ARG) shell monkey -p $(BINNAME) 1
	@sleep 1 # TODO: loop until pidof gives us a valid number
	@adb $(ADB_DEVICE_ARG) logcat --pid=`adb $(ADB_DEVICE_ARG) shell pidof $(BINNAME)`

.phony: stop
stop: #@ Stop the running build.
	@adb $(ADB_DEVICE_ARG) shell am force-stop $(BINNAME)
