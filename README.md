# Kameleon Documentation

## Setup Instructions

1. **Apply the KSP Plugin**

   Add the following plugin configuration to your build.gradle.kts file:
   ```kotlin
    plugins {
        id("com.google.devtools.ksp") version "<latest_version>"
    }
   ```
2. **Include Kameleon Dependencies**

   Add the required dependencies to your dependencies block:
   ```kotlin
    dependencies {
        implementation("dev.dzgeorgy.kameleon:core:<latest_version>")
        ksp("dev.dzgeorgy.kameleon:compiler:<latest_version>")
    }
   ```


## Usage Guide

### Mapping with MapTo

### Defining Aliases with Alias
