#!/bin/sh

sdk=$ANDROID_HOME

outputDirForGeneratedSourceFiles='generated_java_sources'
outputDirForBytecode='java_virtual_machine_bytecode'
outputDexFilepath='classes.dex'

filepathOfAPK="app.apk"
filepathOfUnalignedAPK="${filepathOfAPK}.unaligned"


# Use the latest build tools version...
buildTools=$sdk/build-tools/$(ls $sdk/build-tools | sort -n | tail -1)

# ...and the latest platform version.
platform=$sdk/platforms/$(ls $sdk/platforms | sort -n |tail -1) 


androidLib=$platform/android.jar

_aapt=$buildTools/aapt
_dx=$buildTools/dx

manifestFilepath=$(find */src/main -name AndroidManifest.xml)
resourcesFilepath=$(find */src/main -name res)

main() {
	makeOutputDirs && \
	generateJavaFileForAndroidResources && \
	compileJavaSourceFilesToJavaVirtualMachineBytecode && \
	translateJavaVirtualMachineMBytecodeToAndroidRuntimeBytecode && \
	createUnalignedAndroidApplicationPackage && \
	addAndroidRuntimeBytecodeToAndroidApplicationPackage && \
	signAndroidApplicationPackageWithDebugKey && \
	alignUncompressedDataInZipFileToFourByteBoundariesForFasterMemoryMappingAtRuntime && \
    cleanup
}

makeOutputDirs() {
	mkdir -p "$outputDirForBytecode" "$outputDirForGeneratedSourceFiles"
}

generateJavaFileForAndroidResources() {
	# aapt package
	#
	#   Package the android resources.  It will read assets and resources that are
	#   supplied with the -M -A -S or raw-files-dir arguments.  The -J -P -F and -R
	#   options control which files are output.
	#
	#	-f  force overwrite of existing files
	#   -m  make package directories under location specified by -J
	#   -J  specify where to output R.java resource constant definitions
	#	-M  specify full path to AndroidManifest.xml to include in zip
	#	-S  directory in which to find resources.  Multiple directories will be scanned
	#       and the first match found (left to right) will take precedence.
	#   -I  add an existing package to base include set
	$_aapt package -f -m -J "$outputDirForGeneratedSourceFiles" -M "$manifestFilepath" -S "$resourcesFilepath" -I "$androidLib"
}

compileJavaSourceFilesToJavaVirtualMachineBytecode() {
	javac \
		-classpath "$androidLib" \
		-sourcepath "*/src/main/java:$outputDirForGeneratedSourceFiles" \
		-d "$outputDirForBytecode" \
		-target 1.7 \
		-source 1.7 \
		$(find */src/main/java -name '*.java') \
		$(find $outputDirForGeneratedSourceFiles -name '*.java')
}

translateJavaVirtualMachineMBytecodeToAndroidRuntimeBytecode() {
	$_dx --dex --output="$outputDexFilepath" "$outputDirForBytecode"
}

createUnalignedAndroidApplicationPackage() {
	$_aapt package -f -M "$manifestFilepath" -S "$resourcesFilepath" -I "$androidLib" -F "$filepathOfUnalignedAPK"
}

addAndroidRuntimeBytecodeToAndroidApplicationPackage() {
	( $_aapt add "$filepathOfUnalignedAPK" "$outputDexFilepath" ) 1>&2
}

signAndroidApplicationPackageWithDebugKey() {
	( jarsigner -keystore "$HOME/.android/debug.keystore" -storepass 'android' "$filepathOfUnalignedAPK" androiddebugkey ) 1>&2
}

alignUncompressedDataInZipFileToFourByteBoundariesForFasterMemoryMappingAtRuntime() {
	$buildTools/zipalign -f 4 "$filepathOfUnalignedAPK" "$filepathOfAPK"
}

cleanup() {
	trash "$outputDirForBytecode" "$outputDirForGeneratedSourceFiles" "$filepathOfUnalignedAPK" "$outputDexFilepath"
}

main