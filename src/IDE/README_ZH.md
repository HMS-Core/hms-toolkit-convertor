中文 | [English](https://github.com/HMS-Core/hms-toolkit-convertor/blob/master/src/IDE/README.md)

## 代码结构
```
HMSConvertor
├── hms-plugin                                            // 独立插件工程，通过gradle(w) clean buildPlugin构建完整插件包
│   └── src
│       └── main
│           ├── java                                       // 插件代码目录
│           │   └── com.huawei.hms.convertor               // 暂时无代码
│           └── resources                                  // 资源文件目录
│               ├── META-INF                               // 图标资源
│               │   └── plugin.xml                         // 插件配置文件
│               └── logback.xml                            // 运行日志配置文件      
├── convertor-idea                                         // Convertor idea模块，通过gradle(w) clean jar构建
│   └── src
│       └── main
│           ├── java
│           │   ├── com.huawei.hms.convertor.idea.i18n         // i18n资源
│           │   ├── com.huawei.hms.convertor.idea.listener     // Project生命周期、DocumentListener
│           │   ├── com.huawei.hms.convertor.idea.setting      // 兼容老版本Convertor配置
│           │   ├── com.huawei.hms.convertor.idea.spi          // SPI接口实现类
│           │   ├── com.huawei.hms.convertor.idea.startup      // Project初始化
│           │   ├── com.huawei.hms.convertor.idea.ui           // UI包
│           │   │   ├── common                                 // privacy、notification
│           │   │   ├── actions                                // 菜单响应
│           │   │   ├── analysis                               // 转换分析
│           │   │   ├── recovery                               // 结果恢复
│           │   │   └── result                                 // 结果
│           │   │       ├── searchcombobox                     // 搜索UI组件
│           │   │       ├── difftool                           // Diff Tool UI组件
│           │   │       ├── conversion                         // 转换结果（convert/revert/awareHint/diff/table）
│           │   │       ├── summary                            // 转换摘要信息
│           │   │       ├── xms                                // XMS Adapter Updates
│           │   ├── com.huawei.hms.convertor.idea.util         // 工具类（反射调用封装、GRS调用）
│           │   └── com.huawei.hms.convertor.idea.xmsevent     // XMS增量生成事件调度
│           └── resources                                      // 资源文件
│               ├── icons                                      // 图标
│               ├── messages                                   // i18n资源文件
│               ├── META-INF                                     
│               │   └── services                               // SPI实现类注册
│               └── logback.xml                                // 运行日志配置文件
```
