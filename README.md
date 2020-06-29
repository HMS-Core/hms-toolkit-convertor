# HMS Toolkit Convertor
------------------
[![License](https://img.shields.io/badge/Docs-hmsguides-brightgreen)](https://developer.huawei.com/consumer/en/doc/development/Tools-Guides/05673260) ![Apache-2.0](https://img.shields.io/badge/license-Apache-blue)

English | [中文](https://github.com/HMS-Core/hms-toolkit-convertor/blob/master/README_ZH.md)

## Table of Contents

 * [Introduction](#introduction)
 * [Build](#build)
 * [Install](#install)
 * [How to Use](#how-to-use)
 * [Code Contributions](#code-contributions)
 * [License](#license)
 
## introduction
------------

Convertor is a code conversion tool supporting Java and Kotlin projects. It helps developers to automatically convert GMS APIs called by apps into corresponding HMS APIs, implementing quick conversion and HMS integration. The HMS Convertor provides the following functions:

- New Conversion: Automatically converts GMS APIs invoked by an app to HMS APIs.

- Open Last Conversion: Opens the last conversion result.

- Save All: Save the corrent project and conversion information.

- Restore Project: Restores a project with the backup file.

Build
------------------

#### Dependencies

Convertor requires:

- Gradle(>= 5.2.1)
- JDK(>= 8)

Run the **git clone** command to download the code, go to the **Convertor/src** directory, and run the following command: 

```shell
$ ./gradlew -p IDE/intellij-plugin buildPlugin
```

After the build is complete, go to the **Convertor/src/IDE/intellij-plugin/build/distributions** directory to view the plugin package in .zip format. 

## Install
------------------

Start the IDEA or Android Studio and click the menu bar on the upper left, choose **File -> Settings -> Plugins -> Install Plugin from Disk**

![avatar](https://communityfile-drcn.op.hicloud.com/FileServer/getFile/cmtyPub/011/111/111/0000000000011111111.20200202174355.27600226939014491241655781001918:50510422152457:2800:B7365AA229F3984BF33549A22CB13A9D312E5FD82F0D48ADF1D0102637571B4D.png?needInitFileName=true)

select the ZIP plugin package generated during the build.

tips:

You can debug the plugin in the sandbox using the following command:

```shell
$ ./gradlew -p IDE/intellij-plugin runIde
```

How to use
------------------

For details, you can click: https://developer.huawei.com/consumer/en/doc/development/Tools-Guides/90419706

## Code Contributions
------------------

To make a contribution to Convertor project, follow these steps.

 1. **Fork** the repo on GitHub 
 2. **Clone** the project to your own machine
 3. **Commit** changes to your own branch 
 4. **Push** your work back up to your fork
 5. Submit a **Pull request** so that we can review your changes

NOTE: Be sure to merge the latest from "upstream" before making a pull request!

License
------------------

HMS Toolkit Convertor is licensed under the [Apache License version 2.0](https://github.com/HMS-Core/hms-toolkit-convertor/blob/master/LICENSE)

