[![Build](https://github.com/applibgroup/TVM/actions/workflows/main.yml/badge.svg)](https://github.com/applibgroup/TVM/actions/workflows/main.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=applibgroup_TVM&metric=alert_status)](https://sonarcloud.io/dashboard?id=applibgroup_TVM)

# Source
This library has borrowed all major source codes from official TVM repo(https://github.com/apache/tvm).
Actual code has been modified to make it adaptable in HMOS Dev environment.

## Integration

1. For using TVM module in sample app, include the source code and add the below dependencies in entry/build.gradle to generate hap/support.har.
```
 implementation project(path: ':tvm')
```
2. For using TVM module in separate application using har file, add the har file in the entry/libs folder and add the dependencies in entry/build.gradle file.
```
 implementation fileTree(dir: 'libs', include: ['*.har'])
```
3. For using TVM module from a remote repository in separate application, add the below dependencies in entry/build.gradle file.
```
implementation 'dev.applibgroup:tvm:1.0.0'
```


## License

	TVM is licensed under the Apache-2.0 license.

