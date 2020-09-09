# HMS Toolkit Convertor

[![License](https://img.shields.io/badge/Docs-hmsguides-brightgreen)](https://developer.huawei.com/consumer/cn/doc/development/Tools-Guides/overview-0000001050060881) ![Apache-2.0](https://img.shields.io/badge/license-Apache-blue)

中文 | [English](https://github.com/HMS-Core/hms-toolkit-convertor)

## 内容列表

 * [简介](#简介)
 * [构建](#构建)
 * [安装](#安装)
 * [贡献代码](#贡献代码)
 * [许可证](#许可证)

## 简介

Convertor工具是为开发者提供的代码转换工具，支持Java和Kotlin工程。可以帮助开发者将应用程序调用GMS（Google Mobile Services）相关的API接口自动转换为HMS相对应的API接口，实现快速转换和集成HMS的能力。HMS Convertor提供如下功能：

- New Conversion：实现应用调用GMS的API接口到HMS对应API接口的自动转换。

- Open Last Conversion：打开上一次转换结果。

- Save All：即时保存当前工程以及转换信息。

- Restore Project：使用备份文件恢复工程。

## 构建

#### 依赖

基本要求:

- Gradle(>= 5.2.1)
- JDK(>= 8)

运行 **git clone**命令下载代码，切换至**Convertor/src**文件夹，运行以下命令：

```shell
$ ./gradlew -p IDE/convertor-plugin buildPlugin
```

构建完成后，切换至 **Convertor/src/IDE/intellij-plugin/build/distributions** 文件夹获取.zip格式压缩包。 

## 安装

启动IDEA或者Android Studio单击左上菜单栏，选择 **File -> Settings -> Plugins -> Install Plugin from Disk**

![avatar](https://communityfile-drcn.op.hicloud.com/FileServer/getFile/cmtyPub/011/111/111/0000000000011111111.20200202174355.27600226939014491241655781001918:50510422152457:2800:B7365AA229F3984BF33549A22CB13A9D312E5FD82F0D48ADF1D0102637571B4D.png?needInitFileName=true)

选中构建产生的ZIP压缩包。

提示：

您可以使用以下命令在沙箱中调试插件 :

```shell
$ ./gradlew -p IDE/convertor-plugin runIde
```

获取更详细的信息，可以点击[使用说明](https://developer.huawei.com/consumer/cn/doc/development/Tools-Guides/overview-0000001050060881)

## 代码贡献

要对Convertor项目贡献代码，请遵循以下步骤：

  1. 从GitHub上**Fork**本项目； 
  2. **Clone**项目代码至你的机器；
  3. **Commit**你的个人分支上的变更； 
  4. **Push**你的变更至你fork的远程代码仓；
  5. 提交一个**Pull request**确保我们可以审视你的变更；

注意：在发出拉取请求之前，一定要将“上游”的最新数据合并!

## 技术支持
如果您对HMS Core还处于评估阶段，可在[Reddit社区](https://www.reddit.com/r/HMSCore/)获取关于HMS Core的最新讯息，并与其他开发者交流见解。

如果您对使用HMS示例代码有疑问，请尝试：
- 开发过程遇到问题上[Stack Overflow](https://stackoverflow.com/questions/tagged/huawei-mobile-services)，在[huawei-mobile-services]标签下提问，有华为研发专家在线一对一解决您的问题。
- 到[华为开发者论坛](https://developer.huawei.com/consumer/cn/forum/blockdisplay?fid=18) HMS Core板块与其他开发者进行交流。

如果您在尝试示例代码中遇到问题，请向仓库提交[issue](https://github.com/HMS-Core/hms-toolkit-convertor/issues)，也欢迎您提交[Pull Request](https://github.com/HMS-Core/hms-toolkit-convertor/pulls)。

## 许可证

此示例代码已获得[Apache License version 2.0](https://github.com/HMS-Core/hms-toolkit-convertor/blob/master/LICENSE)
