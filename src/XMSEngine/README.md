# XMSEngine


## code structure
```
XMSEngine
└─src
    └─main
        ├─java
        │  └─com
        │      └─huawei
        │          └─generator
        │              ├─ast
        │              │  └─custom
        │              ├─build
        │              ├─classes
        │              ├─g2x
        │              │  ├─po
        │              │  │  ├─kit
        │              │  │  ├─map
        │              │  │  │  ├─auto
        │              │  │  │  ├─convertor
        │              │  │  │  ├─extension
        │              │  │  │  └─manual
        │              │  │  └─summary
        │              │  └─processor
        │              │      ├─map
        │              │      └─module
        │              ├─gen
        │              │  └─classes
        │              ├─json
        │              │  └─meta
        │              ├─method
        │              │  ├─builder
        │              │  ├─call
        │              │  ├─component
        │              │  ├─exception
        │              │  ├─factory
        │              │  ├─gen
        │              │  │  └─routing
        │              │  ├─param
        │              │  ├─returns
        │              │  └─value
        │              ├─mirror
        │              └─utils
        └─resources
            ├─mirror
            ├─tables
            └─xms
                ├─code
                │  ├─g
                │  ├─gh
                │  └─z
                ├─common
                │  ├─android
                │  └─gson
                ├─g2x_config
                ├─json
                │  ├─account
                │  │  └─gms
                │  ├─ads
                │  │  ├─gms
                │  │  └─installreferrer
                │  ├─analytics
                │  │  └─gms
                │  ├─awareness
                │  │  └─gms
                │  ├─firebase
                │  │  └─common
                │  ├─framework
                │  │  └─gms
                │  ├─game
                │  │  └─gms
                │  ├─health
                │  │  └─gms
                │  ├─identity
                │  │  └─gms
                │  ├─location
                │  │  └─gms
                │  ├─maps
                │  │  └─gms
                │  ├─ml
                │  │  ├─firebase
                │  │  └─gms
                │  ├─nearby
                │  │  └─gms
                │  ├─panorama
                │  │  └─gms
                │  ├─push
                │  │  ├─firebase
                │  │  └─gms
                │  ├─safety
                │  │  └─gms
                │  ├─site
                │  │  └─gms
                │  └─wallet
                │      └─gms
                ├─maputil
                ├─patch
                │  ├─account
                │  │  ├─gh
                │  │  └─xh
                │  ├─ads
                │  │  ├─gh
                │  │  └─xh
                │  ├─analytics
                │  │  ├─gh
                │  │  └─xh
                │  ├─awareness
                │  │  ├─gh
                │  │  └─xh
                │  ├─framework
                │  │  ├─gh
                │  │  └─xh
                │  ├─game
                │  │  ├─gh
                │  │  └─xh
                │  ├─health
                │  │  ├─gh
                │  │  └─xh
                │  ├─identity
                │  │  ├─gh
                │  │  └─xh
                │  ├─maps
                │  │  ├─gh
                │  │  └─xh
                │  ├─ml
                │  │  ├─gh
                │  │  └─xh
                │  ├─nearby
                │  │  ├─gh
                │  │  └─xh
                │  ├─push
                │  │  ├─gh
                │  │  └─xh
                │  ├─safety
                │  │  ├─gh
                │  │  └─xh
                │  └─wallet
                │      ├─gh
                │      └─xh
                ├─static
                │  ├─account
                │  │  ├─gh
                │  │  └─xh
                │  ├─awareness
                │  │  ├─gh
                │  │  └─xh
                │  ├─fido
                │  │  ├─gh
                │  │  └─xh
                │  ├─framework
                │  │  ├─g
                │  │  ├─gh
                │  │  ├─xg
                │  │  └─xh
                │  ├─maps
                │  │  └─xapi
                │  ├─ml
                │  │  └─gms
                │  │      ├─gh
                │  │      └─xh
                │  └─push
                │      ├─g
                │      ├─gh
                │      ├─xapi
                │      ├─xg
                │      └─xh
                └─unsupport
                    ├─iap
                    │  ├─billing
                    │  └─IInAppBillingService.aidl
                    └─site
                        ├─gms
                        └─places
```