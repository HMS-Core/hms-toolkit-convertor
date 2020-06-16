Convertor
------------------

Convertor is used to implement efficient conversion between the GMS and HMS. It helps developers quickly integrate open capabilities of the HMS by converting the GMS-based application framework to the HMS-based application framework. The code can be converted from GMS-based to HMS-based or both GMS and HMS are supported.

Build
------------------

#### Dependencies

Convertor requires:

- Gradle(>= 5.2.1)
- JDK(>= 8)

Run the **git clone** command to download the code, go to the **Convertor/src** directory, and run the following command: 

```shell
$ ./gradlew -p IDE/convertor-idea buildPlugin
```

After the build is complete, go to the **Convertor/src/IDE/intellij-plugin/build/distributions** directory to view the plugin package in .zip format. 

Install
------------------

Start the IDEA or Android Studio and click the menu bar on the upper left, choose **File -> Settings -> Plugins -> Install Plugin from Disk**

![avatar](https://communityfile-drcn.op.hicloud.com/FileServer/getFile/cmtyPub/011/111/111/0000000000011111111.20200202174355.27600226939014491241655781001918:50510422152457:2800:B7365AA229F3984BF33549A22CB13A9D312E5FD82F0D48ADF1D0102637571B4D.png?needInitFileName=true)

select the ZIP plugin package generated during the build.

tips:

You can debug the plugin in the sandbox using the following command :

```shell
$ ./gradlew -p IDE/convertor-idea runIde
```

Code Contributions
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

Convertor project are licensed under the [Apache License 2.0](LICENSE/LICENSE.txt)

