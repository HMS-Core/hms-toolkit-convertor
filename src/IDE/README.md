English | [中文](https://github.com/HMS-Core/hms-toolkit-convertor/blob/master/src/IDE/README_ZH.md)

## code structure
```
HMSConvertor
├── intellij-plugin                                                // A stand-alone plug-in project, using gradle(w) clean buildPlugin to build
│   └── src
│       └── main
│           └── resources                                          // Resource file directory
│               ├── META-INF
│               │   └── plugin.xml                                 // Plug-in configuration file
│               └── logback.xml                                    // Run log configuration file
├── convertor-idea                                                 // Convertor idea module, using gradle(w) clean jar to build
│   └── src
│       └── main
│           ├── java
│           │   ├── com.huawei.hms.convertor.idea.i18n             // I18n resources
│           │   ├── com.huawei.hms.convertor.idea.listener         // Project lifecycle, and document change
│           │   ├── com.huawei.hms.convertor.idea.setting          // Compatible with older versions of the Convertor configuration
│           │   ├── com.huawei.hms.convertor.idea.spi              // SPI interface implementation class
│           │   ├── com.huawei.hms.convertor.idea.startup          // Project initialization
│           │   ├── com.huawei.hms.convertor.idea.ui               // UI package
│           │   │   ├── actions                                    // Menu response
│           │   │   ├── analysis                                   // Conversion analysis
│           │   │   ├── common                                     // Privacy and notification
│           │   │   ├── javadoc                                    // API details
│           │   │   ├── recovery                                   // Results recovery
│           │   │   └── result                                     // Results
│           │   │       ├── conversion                             // Conversion results(convert/revert/hint/diff/table)
│           │   │       ├── difftool                               // Diff tool UI components
│           │   │       ├── export                                 // Export analysis result
│           │   │       ├── searchcombobox                         // Search UI components
│           │   │       ├── summary                                // Conversion summary information
│           │   │       ├── xms                                    // XMS adapter updates
│           │   ├── com.huawei.hms.convertor.idea.util             // Utility class(reflection call wrapper, and GRS call, etc.)
│           └── resources                                          // Resources
│               ├── icons                                          // Icons
│               ├── messages                                       // I18n resources
│               ├── META-INF
│               │   └── services                                   // SPI implements class registration
├── convertor-core                                                 // Convertor core module, using gradle(w) clean jar to build
│   └── src
│       └── main
│           ├── java
│           │   ├── com.huawei.hms.convertor.core.bi               // BI service
│           │   │   ├── bean                                       // BI bean
│           │   │   ├── enumration                                 // BI enum
│           │   ├── com.huawei.hms.convertor.core.config           // Config cache
│           │   ├── com.huawei.hms.convertor.core.engine           // Engine layer
│           │   │   └── fixbot                                     // Fixbot engine
│           │   │       └── model                                  // Fixbot model
│           │   │           ├── api                                // Fixbot api model and analyse result
│           │   │           ├── clazz                              // Fixbot class model
│           │   │           ├── field                              // Fixbot field model
│           │   │           ├── kit                                // Kit model and statistics result
│           │   │           ├── method                             // Fixbot method model
│           │   │           ├── project                            // Project statistics result
│           │   │       ├── util                                   // Fixbot util
│           │   │   ├── xms                                        // XMS constant
│           │   ├── com.huawei.hms.convertor.core.event            // Event queue
│           │   │   └── context                                    // Event context
│           │   │       ├── project                                // Project event context, listener and consumer
│           │   │   └── handler                                    // Event handler
│           │   │       ├── project                                // Project event handler(convert/revert/edit/save/recovery)
│           │   ├── com.huawei.hms.convertor.core.kits             // Kit constant
│           │   ├── com.huawei.hms.convertor.core.mapping          // Mapping generator
│           │   ├── com.huawei.hms.convertor.core.plugin           // Plugin SPI
│           │   ├── com.huawei.hms.convertor.core.project          // Project biz
│           │   │   ├── backup                                     // Project backup and recovery
│           │   │   ├── base                                       // Project SPI
│           │   │   ├── convert                                    // Code convert SPI and gradle sync SPI
│           │   ├── com.huawei.hms.convertor.core.result           // Result
│           │   │   ├── conversion                                 // Conversion item and cache
│           │   │   ├── diff                                       // XMS diff result
│           │   │   ├── summary                                    // Summary cache
│           │   ├── com.huawei.hms.convertor.openapi               // Open API
│           │   │   ├── result                                     // Open API error code and result
│           │   ├── com.huawei.hms.convertor.util                  // Utility class(executor service builder, and input stream processor, etc.)
│           └── resources                                          // Resources
│               ├── mapping                                        // Mapping config
├── convertor-mapping                                              // Convertor mapping module, using gradle(w) clean jar to build
│   └── src
│       └── main
│           ├── java
│           │   ├── com.huawei.hms.convertor.constants             // Constants
│           │   ├── com.huawei.hms.convertor.g2h                   // G2H mapping
│           │   │   └── map                                        // G2H mapping model
│           │   │       └── auto                                   // G2H mapping auto model
│           │   │       └── desc                                   // G2H mapping desc model
│           │   │       └── extension                              // G2H mapping extension model
│           │   │       └── manual                                 // G2H mapping processor
│           │   │   ├── processor                                  // BI enum
│           │   ├── com.huawei.hms.convertor.handler               // Mapping handler
│           │   ├── com.huawei.hms.convertor.json                  // Middle json model
│           │   ├── com.huawei.hms.convertor.util                  // Utility class(kit mapping, and file util, etc.)
│           │── resources                                          // Resources
│           │   ├── json                                           // Middle json config
│           │   │   └── account                                    // Account middle json
│           │   │       └── 17.0.0                                 // Account 17.0.0 middle json
│           │   │           └── gms                                // Account 17.0.0 middle json
│           │   │   └── ...                                        // Middle json for other kits
```
