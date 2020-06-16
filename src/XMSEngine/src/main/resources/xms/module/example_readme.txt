概述
本文档为Android SDK开发者提供使用。更丰富的信息可从如下网址获取：https://developer.huawei.com/consumer/en/doc/development/Tools-Guides

目录


1、为保证功能的正常使用，强烈不建议修改已经生成的xms代码。

2、xms代码中使用到了以下HMS kit

Kit Name 		Version		 Description
Account Kit     4.0.1.300    implementation 'com.huawei.hms:hwid:4.0.1.300'
Push Kit		4.0.0.303	 implementation 'com.huawei.hms:push:4.0.1.300'


3、SDK发布向导

3.1 安装
	3.1.1 方式一： 通过gradle
		Please add dependency to the XXX Services library by adding the following dependency to your dependencies block of app's build.gradle file:
		
		dependencies {
			Implementation ‘com.huawei.xms……’
		}
	
	3.1.2 方式二：通过Maven
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
	
	3.1.3 方式三：手动
		1.Download our SDK distribution from here：
		2.Place XXX.jar into your project's "libs" folder.
		3.Add the following to your module's build.gradle under 'dependencies':

		dependencies {
			 implementation fileTree(dir: 'libs', include: ['*.jar'])
			 /** Any other dependencies here */
		}

3.2 编辑修改Manifest
	
	<application>
		...
		android:networkSecurityConfig="@xml/network_security_config">
		...
	</application>
	
3.3 增加HMS Library
	1. Please add dependency to the HMS library by adding the following dependecy to your dependencies block of xmsadapter's build.gradle file:
	
	apply plugin: 'com.android.application'
	...

	dependencies {
		api 'com.huawei.hms...'
	}

	2. The following table shows a list of the kits that included in our SDK.
	// show a table about API version Required for Each Kit
	
	Include all the above dependencies into your app build.gradle file.
	
4、反混淆配置
	Add the following to your ProGuard configuration:
	// some examples for ProGuard information

