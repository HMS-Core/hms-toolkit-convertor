# XMSEngine


## code structure
```
XMSEngine
└─src
    ├─main
        ├─java
        │  └─com
        │      └─huawei
        │          ├─generator
        │          │  ├─ast
        │          │  │  └─custom
        │          │  ├─build
        │          │  ├─classes
        │          │  ├─g2x
        │          │  │  ├─po
        │          │  │  │  ├─kit
        │          │  │  │  ├─map
        │          │  │  │  │  ├─auto
        │          │  │  │  │  ├─convertor
        │          │  │  │  │  ├─extension
        │          │  │  │  │  └─manual
        │          │  │  │  └─summary
        │          │  │  └─processor
        │          │  │      ├─javadoc
        │          │  │      ├─map
        │          │  │      └─module
        │          │  ├─gen
        │          │  │  └─classes
        │          │  ├─json
        │          │  │  └─meta
        │          │  ├─method
        │          │  │  ├─builder
        │          │  │  ├─call
        │          │  │  ├─component
        │          │  │  ├─exception
        │          │  │  ├─factory
        │          │  │  ├─gen
        │          │  │  │  └─routing
        │          │  │  ├─param
        │          │  │  ├─returns
        │          │  │  └─value
        │          │  ├─mirror
        │          │  └─utils
        │          └─inquiry
        │              ├─docs
        │              ├─exception
        │              └─utils
        └─resources
            ├─javadoc-json
            │  └─xms
            │      ├─analytics
            │      │  ├─17.2.1
            │      │  │  └─gms
            │      │  └─17.4.3
            │      │      └─gms
            │      ├─framework
            │      │  └─17.1.0
            │      │      └─gms
            │      ├─location
            │      │  └─17.0.0
            │      │      └─gms
            │      ├─maps
            │      │  └─17.0.0
            │      │      └─gms
            │      ├─push
            │      │  ├─20.0.1
            │      │  │  ├─firebase
            │      │  │  └─gms
            │      │  └─20.2.0
            │      │      ├─firebase
            │      │      └─gms
            │      └─wallet
            │          └─18.0.0
            │              └─gms
            ├─javadoc-web
            ├─mirror
            ├─tables
            └─xms
                ├─agc-json
                │  ├─firebase-auth
                │  │  └─19.3.0
                │  ├─firebase-core
                │  │  └─19.3.0
                │  ├─firebase-crash
                │  │  └─17.0.0-beta02
                │  ├─firebase-dynamiclinks
                │  │  └─19.1.0
                │  ├─firebase-functions
                │  │  └─19.0.2
                │  ├─firebase-inappmessaging
                │  │  └─19.0.7
                │  ├─firebase-perf
                │  │  └─19.0.7
                │  └─firebase-remoteconfig
                │      └─19.1.3
                ├─code
                │  ├─g
                │  ├─gh
                │  ├─h
                │  └─z
                ├─common
                │  ├─android
                │  └─gson
                ├─g2x_config
                ├─json
                │  ├─account
                │  │  ├─17.0.0
                │  │  │  └─gms
                │  │  └─18.0.0
                │  │      └─gms
                │  ├─ads
                │  │  ├─18.3.0
                │  │  │  ├─gms
                │  │  │  └─installreferrer
                │  │  └─19.1.0
                │  │      ├─gms
                │  │      └─installreferrer
                │  ├─analytics
                │  │  ├─17.2.1
                │  │  │  └─gms
                │  │  └─17.4.3
                │  │      └─gms
                │  ├─awareness
                │  │  └─17.1.0
                │  │      └─gms
                │  ├─firebase
                │  │  └─17.0.0
                │  │      └─common
                │  ├─framework
                │  │  └─17.1.0
                │  │      └─gms
                │  ├─game
                │  │  ├─18.0.1
                │  │  │  └─gms
                │  │  └─19.0.0
                │  │      └─gms
                │  ├─health
                │  │  └─18.0.0
                │  │      └─gms
                │  ├─identity
                │  │  └─17.0.0
                │  │      └─gms
                │  ├─location
                │  │  └─17.0.0
                │  │      └─gms
                │  ├─maps
                │  │  └─17.0.0
                │  │      └─gms
                │  ├─mlfirebase
                │  │  └─24.0.1
                │  │      └─firebase
                │  ├─mlgms
                │  │  ├─19.0.0
                │  │  │  └─gms
                │  │  └─20.1.0
                │  │      └─gms
                │  ├─nearby
                │  │  └─17.0.0
                │  │      └─gms
                │  ├─panorama
                │  │  └─17.0.0
                │  │      └─gms
                │  ├─push
                │  │  ├─20.0.1
                │  │  │  ├─firebase
                │  │  │  └─gms
                │  │  └─20.2.0
                │  │      ├─firebase
                │  │      └─gms
                │  ├─safety
                │  │  └─17.0.0
                │  │      └─gms
                │  ├─site
                │  │  └─17.0.0
                │  │      └─gms
                │  └─wallet
                │      └─18.0.0
                │          └─gms
                ├─module
                │  └─gradle
                │      └─wrapper
                ├─patch
                │  ├─account
                │  │  ├─17.0.0
                │  │  │  ├─gh
                │  │  │  ├─h
                │  │  │  └─xh
                │  │  └─18.0.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─ads
                │  │  ├─18.3.0
                │  │  │  ├─gh
                │  │  │  ├─h
                │  │  │  └─xh
                │  │  └─19.1.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─analytics
                │  │  ├─17.2.1
                │  │  │  ├─gh
                │  │  │  ├─h
                │  │  │  └─xh
                │  │  └─17.4.3
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─awareness
                │  │  └─17.1.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─fido
                │  │  ├─gh
                │  │  ├─h
                │  │  └─xh
                │  ├─firebase-auth
                │  │  └─19.3.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─firebase-core
                │  │  └─19.3.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─firebase-dynamiclinks
                │  │  └─19.1.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─firebase-functions
                │  │  └─19.0.2
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─firebase-inappmessaging
                │  │  └─19.0.7
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─firebase-perf
                │  │  └─19.0.7
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─firebase-remoteconfig
                │  │  └─19.1.3
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─framework
                │  │  └─17.1.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─game
                │  │  └─18.0.1
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─health
                │  │  └─18.0.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─identity
                │  │  └─17.0.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─location
                │  │  └─17.0.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─maps
                │  │  └─17.0.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─mlgms
                │  │  ├─19.0.0
                │  │  │  ├─gh
                │  │  │  ├─h
                │  │  │  └─xh
                │  │  └─20.1.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─nearby
                │  │  └─17.0.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─panorama
                │  │  └─17.0.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─push
                │  │  ├─20.0.1
                │  │  │  ├─gh
                │  │  │  ├─h
                │  │  │  └─xh
                │  │  └─20.2.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─safety
                │  │  └─17.0.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  └─wallet
                │      └─18.0.0
                │          ├─gh
                │          ├─h
                │          └─xh
                ├─scripts
                ├─shield-fido
                │  └─gms
                ├─special_json
                │  └─push
                │      ├─20.0.1
                │      │  └─firebase
                │      └─20.2.0
                │          └─firebase
                ├─static
                │  ├─account
                │  │  ├─17.0.0
                │  │  │  ├─gh
                │  │  │  ├─h
                │  │  │  └─xh
                │  │  └─18.0.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─awareness
                │  │  └─17.1.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─fido
                │  │  ├─gh
                │  │  ├─h
                │  │  └─xh
                │  ├─firebase-core
                │  │  └─19.3.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─firebase-inappmessaging
                │  │  └─19.0.7
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─framework
                │  │  └─17.1.0
                │  │      ├─g
                │  │      ├─gh
                │  │      ├─h
                │  │      ├─xg
                │  │      └─xh
                │  ├─identity
                │  │  └─17.0.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─maps
                │  │  └─17.0.0
                │  │      └─xapi
                │  ├─mlfirebase
                │  │  └─24.0.1
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  ├─mlgms
                │  │  ├─19.0.0
                │  │  │  ├─gh
                │  │  │  ├─h
                │  │  │  └─xh
                │  │  └─20.1.0
                │  │      ├─gh
                │  │      ├─h
                │  │      └─xh
                │  └─push
                │      ├─20.0.1
                │      │  ├─g
                │      │  ├─gh
                │      │  ├─h
                │      │  ├─xapi
                │      │  ├─xg
                │      │  └─xh
                │      └─20.2.0
                │          ├─g
                │          ├─gh
                │          ├─h
                │          ├─xapi
                │          ├─xg
                │          └─xh
                ├─test
                ├─unsupport
                │  ├─iap
                │  │  ├─billing
                │  │  └─IInAppBillingService.aidl
                │  └─site
                │      ├─gms
                │      └─places
                └─xmsaux
                    ├─scripts
                    ├─src
                    │  └─main
                    │      └─java
                    │          └─org
                    │              └─xms
                    │                  └─adapter
                    │                      └─utils
                    ├─xapi
                    │  └─src
                    │      └─main
                    │          └─java
                    │              └─org
                    │                  └─xms
                    │                      ├─f
                    │                      │  └─messaging
                    │                      └─g
                    │                          └─maps
                    ├─xg
                    │  └─src
                    │      └─main
                    │          └─java
                    │              └─org
                    │                  └─xms
                    │                      ├─f
                    │                      │  └─messaging
                    │                      └─g
                    │                          └─maps
                    └─xh
                        └─src
                            └─main
                                └─java
                                    └─org
                                        └─xms
                                            ├─f
                                            │  └─messaging
                                            └─g
                                                └─maps
```