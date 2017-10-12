device = $(shell adb devices | grep '[0-9]' | head -1 | cut -d'	' -f1)
packageName = $(shell find */src -name AndroidManifest.xml | xargs xmllint -xpath 'string(//manifest/@package)')
mainActivityName = $(shell find */src -name AndroidManifest.xml | xargs sed -e 's/android://g' | xmllint -xpath 'string(//activity[descendant::action[@name="android.intent.action.MAIN"]]/@name)' - )

appID = $(shell grep applicationId */build.gradle | head -1 | sed 's/.*"\(.*\)".*/\1/')
appName = app
apkPath = $(appName).apk
adb = adb -s $(device)

default: build install start

build:
	./build.sh 2>/dev/null

install:
	$(adb) install -r $(apkPath)

start:
	$(adb) shell am start -n $(packageName)/$(packageName)$(mainActivityName)
