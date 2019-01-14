device = $(shell adb devices | grep '[0-9]' | head -1 | cut -d'	' -f1)
packageName = $(shell find . -name AndroidManifest.xml | xargs xmllint -xpath 'string(//manifest/@package)')
mainActivityName = $(shell find . -name AndroidManifest.xml | xargs sed -e 's/android://g' | xmllint -xpath 'string(//activity[descendant::action[@name="android.intent.action.MAIN"]]/@name)' - )

appID = $(shell grep 'package="[a-z.]*"' AndroidManifest.xml | sed 's/^.*package="\(.*\)"$/\1/')
appName = app
apkPath = $(appName).apk
adb = adb -s $(device)

default: build install start

build:
	build -sdk $(HOME)/android -manifest AndroidManifest.xml -java java -xml xml

install:
	$(adb) install -r $(apkPath)

start:
	#$(adb) shell am start -n aoeu.bustler/aoeu.bustler.Main
	$(adb) shell am start -n $(packageName)/$(packageName)$(mainActivityName)

stop:
	$(adb) shell am force-stop aoeu.bustler

restart: stop start

emulator:
	$(ANDROID_HOME)/emulator/emulator -avd Nexus_5X_API_28_x86

grant:
	$(adb) shell pm grant aoeu.bustler android.permission.WRITE_EXTERNAL_STORAGE
	$(adb) shell pm grant aoeu.bustler android.permission.READ_EXTERNAL_STORAGE



