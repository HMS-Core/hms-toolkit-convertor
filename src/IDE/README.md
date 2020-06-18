# Convertor


## code structure
```
HMSConvertor
├── hms-plugin                                            // A stand-alone plug-in project, using gradle(w) clean buildPlugin to build                                                             // the complete plug-in package
│   └── src
│       └── main
│           ├── java                                       // Plug-in code directory
│           │   └── com.huawei.hms.convertor               // Currently no code
│           └── resources                                  // Resource file directory
│               ├── META-INF                               // Icon resources
│               │   └── plugin.xml                         // Plug-in configuration file
│               └── logback.xml                            // Run log configuration file      
├── convertor-idea                                         // Convertor idea module, built from gradle (w) clean jar
│   └── src
│       └── main
│           ├── java
│           │   ├── com.huawei.hms.convertor.idea.i18n         // i18n resources
│           │   ├── com.huawei.hms.convertor.idea.listener     // Project life cycle、DocumentListener
│           │   ├── com.huawei.hms.convertor.idea.setting      // Compatible with older versions of the Convertor configuration
│           │   ├── com.huawei.hms.convertor.idea.spi          // SPI interface implementation class
│           │   ├── com.huawei.hms.convertor.idea.startup      // Project initialization
│           │   ├── com.huawei.hms.convertor.idea.ui           // UI package
│           │   │   ├── common                                 // Privacy、notification
│           │   │   ├── actions                                // Menu response
│           │   │   ├── analysis                               // Transformation analysis
│           │   │   ├── recovery                               // Results recovery
│           │   │   └── result                                 // Results
│           │   │       ├── searchcombobox                     // Search UI components
│           │   │       ├── difftool                           // Diff Tool UI components
│           │   │       ├── conversion                         // Conversion results(convert/revert/awareHint/diff/table)
│           │   │       ├── summary                            // Transform summary information
│           │   │       ├── xms                                // XMS Adapter Updates
│           │   ├── com.huawei.hms.convertor.idea.util         // Utility class (reflection call wrapper, GRS call)
│           │   └── com.huawei.hms.convertor.idea.xmsevent     // XMS increments generate event scheduling
│           └── resources                                      // Resources
│               ├── icons                                      // Icons
│               ├── messages                                   // i18n resources
│               ├── META-INF                                     
│               │   └── services                               // SPI implements class registration
│               └── logback.xml                                // Run log configuration file
```
