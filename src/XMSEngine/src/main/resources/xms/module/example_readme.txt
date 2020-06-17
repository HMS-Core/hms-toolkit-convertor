Overview
This document is provided for Android SDK developers. More information can be obtained from the following URL：https://developer.huawei.com/consumer/en/doc/development/Tools-Guides

Table of Contents


1、In order to ensure the normal use of the function, it is strongly not recommended to modify the xms code that has been generated.

2、The following HMS kit is used in the xms code

Kit Name 		Version		 Description
Account Kit     4.0.1.300    implementation 'com.huawei.hms:hwid:4.0.1.300'
Push Kit		4.0.0.303	 implementation 'com.huawei.hms:push:4.0.1.300'


3、SDK release guide

3.1 Install
	3.1.1 Method 1: Via gradle
		Please add dependency to the XXX Services library by adding the following dependency to your dependencies block of app's build.gradle file:
		
		dependencies {
			Implementation ‘com.huawei.xms……’
		}
	
	3.1.2 Method 2: Via Maven
		You must be using android-maven-plugin 4.1.0 or higher.
		In your pom.xml add the following:

		<dependencies>
			<dependency>
				<groupId>com.braintreepayments.api</groupId>
				<artifactId>braintree</artifactId>
				<version>[2.0.0,)</version>
				<type>aar</type>
				<configuration>
					<manifestMergeLibraries>true</manifestMergeLibraries>
				</configuration>
			</dependency>
		</dependencies>
	
	3.1.3 Method 2: Manual
		1.Download our SDK distribution from here：
		2.Place XXX.jar into your project's "libs" folder.
		3.Add the following to your module's build.gradle under 'dependencies':

		dependencies {
			 implementation fileTree(dir: 'libs', include: ['*.jar'])
			 /** Any other dependencies here */
		}

3.2 Edit Manifest
	
	<application>
		...
		android:networkSecurityConfig="@xml/network_security_config">
		...
	</application>
	
3.3 Add HMS Library
	1. Please add dependency to the HMS library by adding the following dependecy to your dependencies block of xmsadapter's build.gradle file:
	
	apply plugin: 'com.android.application'
	...

	dependencies {
		api 'com.huawei.hms...'
	}

	2. The following table shows a list of the kits that included in our SDK.
	// show a table about API version Required for Each Kit
	
	Include all the above dependencies into your app build.gradle file.
	
4、Anti-aliasing configuration
	Add the following to your ProGuard configuration:
	// some examples for ProGuard information

