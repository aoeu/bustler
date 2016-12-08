device = $(shell adb devices | grep '[0-9]' | head -1 | cut -d'	' -f1)
packageName = $(shell find */src -name AndroidManifest.xml | xargs xmllint -xpath 'string(//manifest/@package)')
mainActivityName = $(shell find */src -name AndroidManifest.xml | xargs sed -e 's/android://g' | xmllint -xpath 'string(//activity[descendant::action[@name="android.intent.action.MAIN"]]/@name)' - )

appName = app
apkPath = $(appName)/build/outputs/apk/$(appName)-debug.apk
adb = adb -s $(device)

default: assemble install start

assemble:
	(./gradlew assembleDebug 2>&1 | grep -v '^:.*:.*' | grep -v 'incubating') 1>&2

compile:
	(./gradlew compileDebugJavaWithJavac 2>&1 | grep -v '^:.*:.*' | grep -v 'incubating') 1>&2

install:
	$(adb) install -r $(apkPath)

start:
	$(adb) shell am start -n $(packageName)/$(mainActivityName)
